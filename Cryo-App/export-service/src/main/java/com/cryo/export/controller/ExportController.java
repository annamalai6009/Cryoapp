package com.cryo.export.controller;

import com.cryo.export.service.ExportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/export")
public class  ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/freezers/{freezerId}/csv")
    public ResponseEntity<byte[]> exportToCsv(
            @PathVariable("freezerId") String freezerId,   // 👈 String
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        byte[] csvData = exportService.exportToCsv(freezerId, fromDateTime, toDateTime, authorization);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData(
                "attachment",
                String.format("freezer_%s_%s_to_%s.csv", freezerId, from, to)
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    @GetMapping("/freezers/{freezerId}/pdf")
    public ResponseEntity<byte[]> exportToPdf(
            @PathVariable("freezerId") String freezerId,   // 👈 String
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        byte[] pdfData = exportService.exportToPdf(freezerId, fromDateTime, toDateTime, authorization);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(
                "attachment",
                String.format("freezer_%s_%s_to_%s.pdf", freezerId, from, to)
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
    }
}
