package com.remediar.back_remediar.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record ItemEstoqueDTO(
        Long id,
        String nomeComercial,
        String principioAtivo,
        String apresentacao,
        Long produtoId,

        @JsonFormat(pattern = "dd/MM/yyyy")
        @JsonProperty("dataValidade")
        LocalDate dataValidade,
        int quantidade
) {
}
