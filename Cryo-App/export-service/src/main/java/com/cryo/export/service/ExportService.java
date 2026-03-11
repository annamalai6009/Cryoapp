package com.cryo.export.service;

import com.cryo.export.dto.FreezerConfigDto;
import com.cryo.export.dto.FreezerReadingExportDto;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FreezerDataService freezerDataService;

    public ExportService(FreezerDataService freezerDataService) {
        this.freezerDataService = freezerDataService;
    }

    // ✅ HELPER: Check Alert Status with Dynamic Thresholds
    private boolean isRedAlert(BigDecimal temp, BigDecimal min, BigDecimal max) {
        if (temp == null || min == null || max == null) return false;
        // Logic: Alert if Temp is LOWER than Min OR HIGHER than Max
        // Example: Temp -90 < -85 (True/Alert) OR Temp -20 > -75 (True/Alert)
        return temp.compareTo(min) < 0 || temp.compareTo(max) > 0;
    }

    public byte[] exportToCsv(String freezerId,
                              LocalDateTime from,
                              LocalDateTime to,
                              String authorizationHeader) {

        // 1. Fetch Readings
        List<FreezerReadingExportDto> readings =
                freezerDataService.getFreezerReadings(freezerId, from, to, authorizationHeader);

        // 2. ✅ Fetch Dynamic Config (Thresholds)
        FreezerConfigDto config = freezerDataService.getFreezerConfig(freezerId, authorizationHeader);

        // Fallback defaults just in case service call fails
        BigDecimal minThreshold = (config != null) ? config.getMinThreshold() : new BigDecimal("-85");
        BigDecimal maxThreshold = (config != null) ? config.getMaxThreshold() : new BigDecimal("-75");

        try (StringWriter writer = new StringWriter();
             CSVPrinter csvPrinter = new CSVPrinter(
                     writer,
                     CSVFormat.DEFAULT.withHeader(
                             "Timestamp", "Temperature", "Freezer On", "Door Open", "Red Alert"))) {

            for (FreezerReadingExportDto reading : readings) {
                String ts = (reading.getTimestamp() != null) ?
                        reading.getTimestamp().format(DATE_FORMATTER) : "";

                // ✅ Use Helper with Dynamic Values
                boolean isRedAlert = isRedAlert(reading.getTemperature(), minThreshold, maxThreshold);

                csvPrinter.printRecord(
                        ts,
                        reading.getTemperature(),
                        Boolean.TRUE.equals(reading.getFreezerOn()) ? "Yes" : "No",
                        Boolean.TRUE.equals(reading.getDoorOpen()) ? "Yes" : "No",
                        isRedAlert ? "Yes" : "No"
                );
            }

            csvPrinter.flush();
            return writer.toString().getBytes();
        } catch (IOException e) {
            logger.error("Error generating CSV export", e);
            throw new RuntimeException("Failed to generate CSV export", e);
        }
    }

    public byte[] exportToPdf(String freezerId,
                              LocalDateTime from,
                              LocalDateTime to,
                              String authorizationHeader) {

        List<FreezerReadingExportDto> readings =
                freezerDataService.getFreezerReadings(freezerId, from, to, authorizationHeader);

        // ✅ Fetch Dynamic Config (Thresholds)
        FreezerConfigDto config = freezerDataService.getFreezerConfig(freezerId, authorizationHeader);

        BigDecimal minThreshold = (config != null) ? config.getMinThreshold() : new BigDecimal("-85");
        BigDecimal maxThreshold = (config != null) ? config.getMaxThreshold() : new BigDecimal("-75");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            // ... (Font and Title setup remains same) ...
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph("Freezer Monitoring Report", titleFont);
            title.setSpacingAfter(10);
            document.add(title);

            // ✅ OPTIONAL: Add Threshold Info to PDF Header
            Paragraph freezerInfo = new Paragraph(
                    String.format("Freezer ID: %s\nRange: %s to %s\nDate: %s to %s\n",
                            freezerId,
                            minThreshold, maxThreshold,
                            from.format(DATE_FORMATTER),
                            to.format(DATE_FORMATTER)),
                    FontFactory.getFont(FontFactory.HELVETICA, 10)
            );
            freezerInfo.setSpacingAfter(15);
            document.add(freezerInfo);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 2, 2, 2, 2});

            table.addCell(new Phrase("Timestamp", headerFont));
            table.addCell(new Phrase("Temperature", headerFont));
            table.addCell(new Phrase("Freezer On", headerFont));
            table.addCell(new Phrase("Door Open", headerFont));
            table.addCell(new Phrase("Red Alert", headerFont));

            for (FreezerReadingExportDto reading : readings) {
                String ts = (reading.getTimestamp() != null) ?
                        reading.getTimestamp().format(DATE_FORMATTER) : "";

                // ✅ Use Helper with Dynamic Values
                boolean isRedAlert = isRedAlert(reading.getTemperature(), minThreshold, maxThreshold);

                table.addCell(new Phrase(ts, cellFont));
                table.addCell(new Phrase(
                        reading.getTemperature() != null ? reading.getTemperature().toString() : "",
                        cellFont
                ));
                table.addCell(new Phrase(Boolean.TRUE.equals(reading.getFreezerOn()) ? "Yes" : "No", cellFont));
                table.addCell(new Phrase(Boolean.TRUE.equals(reading.getDoorOpen()) ? "Yes" : "No", cellFont));
                table.addCell(new Phrase(isRedAlert ? "Yes" : "No", cellFont));
            }

            document.add(table);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating PDF export", e);
            throw new RuntimeException("Failed to generate PDF export", e);
        }
    }
}