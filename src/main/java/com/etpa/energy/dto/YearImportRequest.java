package com.etpa.energy.dto;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;

public record YearImportRequest(@NotEmpty String meterId, int year, @NotEmpty List<MeterReadingDTO> readings) {

}
