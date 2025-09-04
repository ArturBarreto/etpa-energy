package com.etpa.energy.dto;

import java.math.BigDecimal;
import java.time.Month;
import jakarta.validation.constraints.NotNull;

public record MeterReadingDTO(@NotNull Month month, @NotNull BigDecimal reading) {

}
