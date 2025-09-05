package com.etpa.energy.web;

import com.etpa.energy.dto.ConsumptionResponse;
import com.etpa.energy.dto.MeterReadingDTO;
import com.etpa.energy.service.ConsumptionService;
import com.etpa.energy.service.MeterReadingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Month;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({MeterReadingController.class, ConsumptionController.class})
class ControllersWebTest {

    @Autowired MockMvc mvc;

    @MockBean MeterReadingService meterReadingService;
    @MockBean ConsumptionService consumptionService;

    @Test
    void import_year_returns_201() throws Exception {
        Mockito.doNothing().when(meterReadingService)
                .importYear(Mockito.eq("0001"), Mockito.eq(2024), Mockito.anyList());

        // Controller expects POST /readings with YearImportRequest body
        String body = """
            {
              "meterId": "0001",
              "year": 2024,
              "readings": [
                {"month":"JANUARY","reading":10},
                {"month":"FEBRUARY","reading":20},
                {"month":"MARCH","reading":30},
                {"month":"APRIL","reading":40},
                {"month":"MAY","reading":50},
                {"month":"JUNE","reading":60},
                {"month":"JULY","reading":70},
                {"month":"AUGUST","reading":80},
                {"month":"SEPTEMBER","reading":90},
                {"month":"OCTOBER","reading":100},
                {"month":"NOVEMBER","reading":110},
                {"month":"DECEMBER","reading":120}
              ]
            }
            """;

        mvc.perform(post("/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated()); // 201 CREATED
    }

    @Test
    void get_monthly_consumption() throws Exception {
        Mockito.when(consumptionService.getMonthly("0001", 2024, Month.MARCH))
                .thenReturn(new ConsumptionResponse("0001", 2024, Month.MARCH, new BigDecimal("20")));

        mvc.perform(get("/consumption/0001/2024/MARCH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meterId", is("0001")))
                .andExpect(jsonPath("$.year", is(2024)))
                .andExpect(jsonPath("$.month", is("MARCH")))
                .andExpect(jsonPath("$.consumption", is(20)));
    }

    @Test
    void list_year_readings_returns_sorted_array() throws Exception {
        Mockito.when(meterReadingService.getYear("0001", 2024))
                .thenReturn(List.of(
                        new MeterReadingDTO(Month.JANUARY, new BigDecimal("10")),
                        new MeterReadingDTO(Month.FEBRUARY, new BigDecimal("20"))
                ));

        mvc.perform(get("/readings/0001/2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month", is("JANUARY")))
                .andExpect(jsonPath("$[0].reading", is(10)))
                .andExpect(jsonPath("$[1].month", is("FEBRUARY")))
                .andExpect(jsonPath("$[1].reading", is(20)));
    }

    @Test
    void import_year_with_duplicate_month_returns_400() throws Exception {
        // Make the service throw as if it detected a duplicate month
        Mockito.doThrow(new IllegalArgumentException("Duplicate month: JANUARY"))
                .when(meterReadingService).importYear(Mockito.eq("0001"), Mockito.eq(2024), Mockito.anyList());

        String body = """
        {
          "meterId": "0001",
          "year": 2024,
          "readings": [
            {"month":"JANUARY","reading":10},
            {"month":"JANUARY","reading":20}, // duplicate
            {"month":"MARCH","reading":30}
          ]
        }
        """;

        mvc.perform(post("/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest()); // 400
    }

    @Test
    void import_year_with_bad_month_enum_returns_400() throws Exception {
        String body = """
        {
          "meterId": "0001",
          "year": 2024,
          "readings": [
            {"month":"SMARCH","reading":10} // invalid enum
          ]
        }
        """;

        mvc.perform(post("/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest()); // 400 from Jackson enum binding
    }
}
