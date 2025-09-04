package com.etpa.energy.dto;

import java.math.BigDecimal;
import java.time.Month;

public record MeterReadingDTO(String meterId, int year, Month month, BigDecimal reading) {

}
