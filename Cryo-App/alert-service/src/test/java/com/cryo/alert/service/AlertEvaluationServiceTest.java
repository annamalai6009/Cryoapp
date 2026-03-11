package com.cryo.alert.service;

import com.cryo.alert.dto.FreezerDto;
import com.cryo.alert.dto.FreezerReadingDto;
import com.cryo.alert.entity.Alert;
import com.cryo.alert.entity.FreezerAlertState;
import com.cryo.alert.repository.AlertRepository;
import com.cryo.alert.repository.FreezerAlertStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertEvaluationServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private FreezerAlertStateRepository stateRepository;

    @InjectMocks
    private AlertEvaluationService alertService;

    @Captor
    private ArgumentCaptor<Alert> alertCaptor;

    @Captor
    private ArgumentCaptor<FreezerAlertState> stateCaptor;

    private FreezerDto freezer;
    private FreezerReadingDto reading;

    // Test Data
    private static final String FREEZER_ID = "DL2025001";
    private static final String OWNER_ID = "C00001";

    // CUSTOMER SETTINGS for this test (Range: -50.00 to -30.00)
    // Anything WARMER than -30 is bad. Anything COLDER than -50 is bad.
    private static final BigDecimal MIN_THRESHOLD = new BigDecimal("-50.00");
    private static final BigDecimal MAX_THRESHOLD = new BigDecimal("-30.00");

    @BeforeEach
    void setUp() {
        freezer = new FreezerDto(FREEZER_ID, "Lab Freezer A");
        reading = new FreezerReadingDto(FREEZER_ID, null, true, false, LocalDateTime.now());
    }

    @Test
    void testEvaluate_SafeTemperature_NoAlert() {
        // Temp is -40 (Safe, right in the middle of -50 and -30)
        BigDecimal temp = new BigDecimal("-40.00");

        when(stateRepository.findById(FREEZER_ID)).thenReturn(Optional.empty());

        Alert result = alertService.evaluateTemperatureAlert(
                FREEZER_ID, OWNER_ID, temp, freezer, reading, MIN_THRESHOLD, MAX_THRESHOLD
        );

        assertNull(result, "Should not return an alert for safe temperature");
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void testEvaluate_HighTemperature_TriggersAlert() {
        // Temp is -20 (Unsafe, Warmer than Max -30)
        BigDecimal temp = new BigDecimal("-20.00");

        // Mock state: New device, no previous state
        when(stateRepository.findById(FREEZER_ID)).thenReturn(Optional.empty());
        // Mock save to return the object passed to it
        when(alertRepository.save(any(Alert.class))).thenAnswer(i -> i.getArgument(0));

        Alert result = alertService.evaluateTemperatureAlert(
                FREEZER_ID, OWNER_ID, temp, freezer, reading, MIN_THRESHOLD, MAX_THRESHOLD
        );

        assertNotNull(result, "Should return an alert object");
        assertEquals(Alert.AlertType.RED_ALERT, result.getAlertType());
        assertEquals(temp, result.getTemperature());

        // Verify State Updated to ACTIVE
        verify(stateRepository).save(stateCaptor.capture());
        FreezerAlertState savedState = stateCaptor.getValue();
        assertTrue(savedState.getActive());
        assertFalse(savedState.getAcknowledged());
    }

    @Test
    void testEvaluate_LowTemperature_TriggersAlert() {
        // Temp is -60 (Unsafe, Colder than Min -50)
        BigDecimal temp = new BigDecimal("-60.00");

        when(stateRepository.findById(FREEZER_ID)).thenReturn(Optional.empty());
        when(alertRepository.save(any(Alert.class))).thenAnswer(i -> i.getArgument(0));

        Alert result = alertService.evaluateTemperatureAlert(
                FREEZER_ID, OWNER_ID, temp, freezer, reading, MIN_THRESHOLD, MAX_THRESHOLD
        );

        assertNotNull(result);
        verify(alertRepository).save(any(Alert.class));
    }

    @Test
    void testEvaluate_AlreadyActive_NoSpam() {
        // Temp is still -20 (Unsafe)
        BigDecimal temp = new BigDecimal("-20.00");

        // State is ALREADY ACTIVE (Alert was sent previously)
        FreezerAlertState activeState = new FreezerAlertState(FREEZER_ID);
        activeState.setActive(true);
        activeState.setAcknowledged(false);

        when(stateRepository.findById(FREEZER_ID)).thenReturn(Optional.of(activeState));

        // Call logic
        Alert result = alertService.evaluateTemperatureAlert(
                FREEZER_ID, OWNER_ID, temp, freezer, reading, MIN_THRESHOLD, MAX_THRESHOLD
        );

        // Should return NULL (Silence) because we already alerted
        assertNull(result, "Should not trigger duplicate alert if already active");
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void testEvaluate_Recovery_ResetsState() {
        // Temp returns to -40 (Safe midpoint)
        BigDecimal temp = new BigDecimal("-40.00");

        // State was previously ACTIVE
        FreezerAlertState activeState = new FreezerAlertState(FREEZER_ID);
        activeState.setActive(true);

        when(stateRepository.findById(FREEZER_ID)).thenReturn(Optional.of(activeState));

        Alert result = alertService.evaluateTemperatureAlert(
                FREEZER_ID, OWNER_ID, temp, freezer, reading, MIN_THRESHOLD, MAX_THRESHOLD
        );

        assertNull(result);

        // Verify State is Reset (Active = False)
        verify(stateRepository).save(stateCaptor.capture());
        FreezerAlertState savedState = stateCaptor.getValue();
        assertFalse(savedState.getActive(), "State should be reset to inactive after recovery");
    }
}