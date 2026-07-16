package com.remediar.back_remediar.controller;

import com.remediar.back_remediar.model.Solicitacao;
import com.remediar.back_remediar.model.SolicitacaoDoacao;
import com.remediar.back_remediar.model.SolicitacaoPedido;
import com.remediar.back_remediar.model.dto.StatusRequestDTO;
// import com.remediar.back_remediar.model.dto.notificacoes.NotificationRequestDto;
// import com.remediar.back_remediar.model.dto.notificacoes.TipoCanal;
import com.remediar.back_remediar.model.dto.solicitacoes.*;
import com.remediar.back_remediar.model.dto.usuarios.FuncionarioIdRequestDTO;
import com.remediar.back_remediar.model.mapper.SolicitacaoDoacaoMapper;
import com.remediar.back_remediar.model.mapper.SolicitacaoPedidoMapper;
// import com.remediar.back_remediar.producer.NotificationProducer;
import com.remediar.back_remediar.service.SolicitacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/solicitacoes")
@Tag(name = "Solicitações", description = "Operações relacionadas à solicitações.")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;
    private final SolicitacaoPedidoMapper solicitacaoPedidoMapper;
    private final SolicitacaoDoacaoMapper solicitacaoDoacaoMapper;
    // private final NotificationProducer notificationProducer;

    public SolicitacaoController(SolicitacaoService solicitacaoService, SolicitacaoPedidoMapper solicitacaoPedidoMapper, SolicitacaoDoacaoMapper solicitacaoDoacaoMapper/*, NotificationProducer notificationProducer*/) {
        this.solicitacaoService = solicitacaoService;
        this.solicitacaoPedidoMapper = solicitacaoPedidoMapper;
        this.solicitacaoDoacaoMapper = solicitacaoDoacaoMapper;
        // this.notificationProducer = notificationProducer;
    }



    @Secured({"ADMIN", "FUNCIONARIO"})
    @GetMapping("/{id}")
    @Operation(summary = "Buscar solicitação por ID.", description = "Retorna uma solicitação com base no ID fornecido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação encontrada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada.")
    })
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        Solicitacao solicitacao = solicitacaoService.findById(id);

        if (solicitacao instanceof SolicitacaoPedido pedido) {
            return ResponseEntity.ok(solicitacaoPedidoMapper.toPedidoResponseDTO(pedido));
        } else if (solicitacao instanceof SolicitacaoDoacao doacao) {
            return ResponseEntity.ok(solicitacaoDoacaoMapper.toDoacaoResponseDTO(doacao));
        } else {
            throw new IllegalStateException("Tipo de solicitação desconhecido: " + solicitacao.getClass());
        }
    }

    @Operation(summary = "Listar todos os pedidos.", description = "Retorna uma lista paginada de todos os pedidos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso.")
    })
    @GetMapping("/pedidos")
    public ResponseEntity<Page<PedidoResponseDTO>> findAllPedidos(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String nomeSolicitante,
            @RequestParam(required = false) String medicamento,
            @RequestParam(required = false) String dataSolicitacaoInicio,
            @RequestParam(required = false) String dataSolicitacaoFim,
            @RequestParam(required = false) String status,
            Pageable pageable
    ) {
        Page<PedidoResponseDTO> solicitacoes = solicitacaoService.findAllPedidos(
                pageable, id, nomeSolicitante, medicamento, dataSolicitacaoInicio, dataSolicitacaoFim, status);
        return ResponseEntity.ok(solicitacoes);
    }



    @Secured({"ADMIN", "FUNCIONARIO"})
    @Operation(summary = "Criar uma solicitação de pedido.", description = "Cria uma nova solicitação com base nos dados enviados no corpo da requisição.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Solicitação criada com sucesso.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos enviados na requisição.",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/pedidos")
    public ResponseEntity<PedidoResponseDTO> createSolicitacaoPedido(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados da solicitação de pedido.",
                required = true,
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PedidoRequestDTO.class)))
            @RequestBody PedidoRequestDTO obj
    ) {
        PedidoResponseDTO pedido = solicitacaoService.criarSolicitacaoPedido(obj);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(pedido.solicitacao().id())
                .toUri();


        // NotificationRequestDto notificacao = new NotificationRequestDto(
        //         pedido.solicitacao().usuario().id(),
        //         "no-reply@remediar.com",
        //         pedido.solicitacao().usuario().telefone(),
        //         "📦 Pedido recebido com sucesso!",
        //         "Sua solicitação de pedido do medicamento " + pedido.item().nomeComercialOrPrincipioAtivo() +  " foi registrada com sucesso. Em breve será processada. Código: " + pedido.solicitacao().id(),
        //         TipoCanal.WHATSAPP,
        //         pedido.solicitacao().id(),
        //         "Pedido"
        // );

        // notificationProducer.sendNotification(notificacao);

        return ResponseEntity.created(uri).body(pedido);
    }



    @PatchMapping("pedidos/{id}")
    @Operation(summary = "Atualizar um pedido.", description = "Atualiza os dados de uma solicitação com base no ID fornecido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação atualizada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada.")
    })
    public ResponseEntity<PedidoResponseDTO> updateSolicitacao(
            @PathVariable UUID id,
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados da solicitação a serem atualizados",
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PedidoPatchRequestDTO.class)))
            @RequestBody PedidoPatchRequestDTO obj
    ) {
        PedidoResponseDTO updatedSolicitacao = solicitacaoService.updateSolicitacaoPedido(id, obj);
        return ResponseEntity.ok(updatedSolicitacao);
    }



    @Secured({"ADMIN", "FUNCIONARIO"})
    @Operation(summary = "Listar todos os pedidos.", description = "Retorna uma lista paginada de todos os pedidos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso.")
    })
    @GetMapping("/doacoes")
    public ResponseEntity<Page<DoacaoResponseDTO>> findAllDoacoes(Pageable pageable) {
        Page<DoacaoResponseDTO> solicitacoes = solicitacaoService.findAllDoacoes(pageable);
        return ResponseEntity.ok(solicitacoes);
    }



    @Secured({"ADMIN", "FUNCIONARIO"})
    @Operation(summary = "Criar uma solicitação de pedido.", description = "Cria uma nova solicitação com base nos dados enviados no corpo da requisição.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Solicitação criada com sucesso.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos enviados na requisição.",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/doacoes")
    public ResponseEntity<DoacaoResponseDTO> createSolicitacaoDoacao(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados da solicitação de doação.",
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DoacaoRequestDTO.class)))
            @RequestBody DoacaoRequestDTO obj
    ) {
        DoacaoResponseDTO doacao = solicitacaoService.criarSolicitacaoDoacao(obj);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(doacao.solicitacao().id())
                .toUri();
        return ResponseEntity.created(uri).body(doacao);
    }



    @Secured({"ADMIN", "FUNCIONARIO"})
    @PatchMapping("doacoes/{id}/itens")
    @Operation(summary = "Atualizar itens de uma doação.", description = "Atualiza os itens de uma solicitação de doação com base no ID fornecido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    public ResponseEntity<DoacaoResponseDTO> userUpdatesDoacao(
            @PathVariable UUID id,
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Itens da solicitação a serem atualizados",
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DoacaoPatchRequestDTO.class)))
            @RequestBody DoacaoPatchRequestDTO obj
    ) {
        DoacaoResponseDTO updatedSolicitacao = solicitacaoService.userUpdatesDoacao(id, obj);
        return ResponseEntity.ok(updatedSolicitacao);
    }



    @Secured({"ADMIN", "FUNCIONARIO"})
    @Operation(summary = "Lista toads as solicitações de pedido por status.", description = "Retorna uma lista de solicitações do tipo Pedido de acordo com status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de solicitações do tipo pedido retornada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Nenhuma solicitação encontrada.")
    })
    @GetMapping("/pesquisar/pedidos/status/{status}")
    public ResponseEntity<Page<PedidoResponseDTO>> findAllSolicitacaoPedidoByStatusAtual(@PathVariable String status, Pageable pageable) {
        Page<PedidoResponseDTO> solicitacoes = solicitacaoService.findAllSolicitacaoPedidoByStatusAtual(status, pageable);
        return ResponseEntity.ok(solicitacoes);
    }



    @Secured({"ADMIN", "FUNCIONARIO"})
    @Operation(summary = "Lista toads as solicitacoes de doacao por status.", description = "Retorna uma lista de solicitações do tipo Doação de acordo com status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de solicitações do tipo doação retornada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Nenhuma solicitação encontrada.")
    })
    @GetMapping("/pesquisar/doacoes/status/{status}")
    public ResponseEntity<Page<DoacaoResponseDTO>> findAllSolicitacaoDoacaoByStatusAtual(@PathVariable String status, Pageable pageable) {
        Page<DoacaoResponseDTO> solicitacoes = solicitacaoService.findAllSolicitacaoDoacaoByStatusAtual(status, pageable);
        return ResponseEntity.ok(solicitacoes);
    }


    @Secured({"ADMIN", "FUNCIONARIO"})
    @Operation(summary = "Listar todas as solicitações de Pedido de um usuário.", description = "Retorna uma lista de solicitações do tipo Pedido de um usuário com base no ID do usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de solicitações do tipo pedido retornada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado.")
    })
    @GetMapping("/pesquisar/pedidos/autorId/{id}")
    public ResponseEntity<List<PedidoResponseDTO>> findAllSolicitacaoPedidoByAutorId(@PathVariable Long id) {


        List<PedidoResponseDTO> solicitacoes = solicitacaoService.findAllSolicitacaoPedidoByAutorId(id);
        return ResponseEntity.ok(solicitacoes);
    }



    @Secured({"ADMIN", "FUNCIONARIO"})
    @Operation(summary = "Listar todas as solicitações de Doação de um usuário.", description = "Retorna uma lista de solicitações do tipo Doação de um usuário com base no ID do usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de solicitações do tipo doação retornada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado.")
    })
    @GetMapping("/pesquisar/doacoes/autorId/{id}")
    public ResponseEntity<List<DoacaoResponseDTO>> findAllSolicitacaoDoacaoByAutorId(@PathVariable Long id) {
        List<DoacaoResponseDTO> solicitacoes = solicitacaoService.findAllSolicitacaoDoacaoByAutorId(id);
        return ResponseEntity.ok(solicitacoes);
    }



    @Secured({"ADMIN", "FUNCIONARIO"})
    @Operation(summary = "Listar todas as solicitações de um funcionário responsável atual.", description = "Retorna uma lista de solicitações de um funcionário responsável atual com base no ID do funcionário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de solicitações do funcionário retornada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado.")
    })
    @GetMapping("/pesquisar/funcionarioId/{id}")
    public ResponseEntity<List<Record>> findAllSolicitacaoByFuncionarioResponsavelAtualId(@PathVariable Long id) {
        List<Record> solicitacoes = solicitacaoService.findAllSolicitacaoByFuncionarioResponsavelAtualId(id);
        return ResponseEntity.ok(solicitacoes);
    }



    @Secured({"ADMIN", "FUNCIONARIO"})
    @Operation(summary = "Listar todas as solicitações de um funcionário responsável atual com status específico.", description = "Retorna uma lista de solicitações de um funcionário responsável atual com base no ID do funcionário e status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de solicitações do funcionário retornada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado.")
    })
    @GetMapping("/pesquisar/funcionarioId/{id}/status/{status}")
    public ResponseEntity<List<Record>> findAllSolicitacaoByFuncionarioResponsavelAtualIdAndStatus(@PathVariable Long id, @PathVariable String status) {
        List<Record> solicitacoes = solicitacaoService.findAllSolicitacaoByFuncionarioResponsavelAtualIdAndStatus(id, status);
        return ResponseEntity.ok(solicitacoes);
    }



    @Secured({"ADMIN", "FUNCIONARIO"})
    @PatchMapping("/{solicitacaoId}/funcionario")
    @Operation(summary = "Atualizar funcionário responsável atual de uma solicitação.", description = "Atualiza o funcionário responsável atual de uma solicitação com base nos IDs fornecidos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funcionário responsável atualizado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Solicitação ou funcionário não encontrado.")
    })
    public ResponseEntity<Void> updateFuncionarioResponsavelAtual(
            @PathVariable UUID solicitacaoId,
            @RequestBody FuncionarioIdRequestDTO obj
    ) {
        solicitacaoService.updateFuncionarioResponsavelAtual(solicitacaoId, obj.funcionarioId());
        return ResponseEntity.ok().build();
    }



    @Secured({"ADMIN", "FUNCIONARIO"})
    @PatchMapping("/{solicitacaoId}/status")
    @Operation(summary = "Atualizar status de uma solicitação.", description = "Atualiza o status de uma solicitação com base no status fornecido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada.")
    })
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID solicitacaoId,
            @RequestBody StatusRequestDTO obj
    ) {
        solicitacaoService.updateStatus(solicitacaoId, obj.status());
        return ResponseEntity.ok().build();
    }


    @Secured({"ADMIN", "FUNCIONARIO"})
    @PatchMapping("/{solicitacaoId}/confirmar")
    @Operation(summary = "Confirmar uma solicitação.", description = "Confirma uma solicitação com base no ID fornecido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação confirmada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada.")
    })
    public ResponseEntity<Void> aprovarSolicitacao(
            @PathVariable UUID solicitacaoId, @RequestBody FuncionarioIdRequestDTO obj
    ) {
        solicitacaoService.aprovarSolicitacao(solicitacaoId, obj.funcionarioId());
        return ResponseEntity.ok().build();
    }




    @Secured({"ADMIN", "FUNCIONARIO"})
    @PatchMapping("/{solicitacaoId}/cancelar")
    @Operation(summary = "Cancelar uma solicitação.", description = "Cancela uma solicitação com base no ID fornecido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação cancelada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada.")
    })
    public ResponseEntity<Void> cancelarSolicitacao(
            @PathVariable UUID solicitacaoId, @RequestBody FuncionarioIdRequestDTO obj
    ) {
        solicitacaoService.cancelarSolicitacao(solicitacaoId, obj.funcionarioId());
        return ResponseEntity.ok().build();
    }




    @Secured({"ADMIN", "FUNCIONARIO"})
    @PatchMapping("/{solicitacaoId}/separar")
    @Operation(summary = "Separar uma solicitação.", description = "Separa uma solicitação com base no ID fornecido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação separada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada.")
    })
    public ResponseEntity<Void> separarSolicitacao(
            @PathVariable UUID solicitacaoId, @RequestBody FuncionarioIdRequestDTO obj
    ) {
        solicitacaoService.separarSolicitacao(solicitacaoId, obj.funcionarioId());
        return ResponseEntity.ok().build();
    }



    @Secured({"ADMIN", "FUNCIONARIO"})
    @PatchMapping("/{solicitacaoId}/pronto-para-retirada")
    @Operation(summary = "Pronto para Retirada", description = "Modifica status de uma solicitação para PRONTO_PARA_RETIRADA com base no ID fornecido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação alterada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada.")
    })
    public ResponseEntity<Void> prontoParaRetiradaSolicitacao(
            @PathVariable UUID solicitacaoId, @RequestBody FuncionarioIdRequestDTO obj
    ) {
        solicitacaoService.prontoParaRetirarSolicitacao(solicitacaoId, obj.funcionarioId());
        return ResponseEntity.ok().build();
    }



    @Secured({"ADMIN", "FUNCIONARIO"})
    @PatchMapping("/{solicitacaoId}/finalizar")
    @Operation(summary = "Finaliza solicitação", description = "Finaliza uma solicitação com base no ID fornecido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação finalizada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada.")
    })
    public ResponseEntity<Void> concluirSolicitacao(
            @PathVariable UUID solicitacaoId, @RequestBody FuncionarioIdRequestDTO obj
    ) {
        solicitacaoService.concluirSolicitacao(solicitacaoId, obj.funcionarioId());
        return ResponseEntity.ok().build();
    }

    @Secured({"ADMIN", "FUNCIONARIO"})
    @GetMapping("/estatisticas/pedidos-concluidos")
    @Operation(summary = "Obter estatísticas de pedidos concluídos", description = "Retorna a quantidade total de medicamentos pedidos em solicitações concluídas. Pode filtrar por usuário específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos.")
    })
    public ResponseEntity<EstatisticasPedidoDTO> getEstatisticasPedidosConcluidos(
            @RequestParam(required = false) Long usuarioId
    ) {
        EstatisticasPedidoDTO estatisticas = solicitacaoService.getEstatisticasPedidosConcluidos(usuarioId);
        return ResponseEntity.ok(estatisticas);
    }
}
