package com.remediar.back_remediar.model.dto.solicitacoes;

import com.remediar.back_remediar.model.enums.Status;

import java.time.LocalDate;
import java.util.UUID;

public record PedidoFiltroDTO(
        UUID id,
        String nomeSolicitante,
        String medicamento,
        LocalDate dataSolicitacaoInicio,
        LocalDate dataSolicitacaoFim,
        Status status
) {
}
