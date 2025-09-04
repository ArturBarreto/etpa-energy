package com.etpa.energy.dto;

import java.util.List;


// For batch import of one meter-year (12 readings)
public record ImportRequest(String meterId, String profileCode, int year, List<MeterReadingDTO> readings) {

}