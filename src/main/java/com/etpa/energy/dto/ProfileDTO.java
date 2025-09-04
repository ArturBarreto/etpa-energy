package com.etpa.energy.dto;

import java.util.List;

public record ProfileDTO(String code, String description, List<FractionDTO> fractions) {

}
