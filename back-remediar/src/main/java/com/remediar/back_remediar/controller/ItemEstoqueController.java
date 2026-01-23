package com.remediar.back_remediar.controller;

import com.remediar.back_remediar.model.ItemEstoque;
import com.remediar.back_remediar.model.dto.ItemEstoqueCreateDTO;
import com.remediar.back_remediar.model.dto.ItemEstoqueDTO;
import com.remediar.back_remediar.model.dto.ItemEstoqueUpdateDTO;
import com.remediar.back_remediar.model.dto.MedicamentoDTO;
import com.remediar.back_remediar.model.mapper.ItemEstoqueMapper;
import com.remediar.back_remediar.service.ItemEstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/itens-estoque")
@Tag(name = "Itens de Estoque", description = "Operações relacionadas a itens no estoque")
public class ItemEstoqueController {

    private final ItemEstoqueService itemEstoqueService;
    private final ItemEstoqueMapper itemEstoqueMapper;

    public ItemEstoqueController(ItemEstoqueService itemEstoqueService, ItemEstoqueMapper itemEstoqueMapper) {
        this.itemEstoqueService = itemEstoqueService;
        this.itemEstoqueMapper = itemEstoqueMapper;
    }

    @Operation(summary = "Buscar item de estoque por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemEstoqueDTO.class))),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ItemEstoqueDTO> findById(@PathVariable Long id) {
        ItemEstoque itemEstoque = itemEstoqueService.findById(id);
        return ResponseEntity.ok(itemEstoqueMapper.toDTO(itemEstoque));
    }

    @Operation(summary = "Listar todos os itens de estoque")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<Page<ItemEstoqueDTO>> findAll(Pageable pageable) {
        Page<ItemEstoqueDTO> itensEstoque = itemEstoqueService.findAll(pageable);
        return ResponseEntity.ok(itensEstoque);
    }

    @Operation(summary = "Listar itens de estoque por ID do estoque")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping("/estoqueId/{estoqueId}")
    public ResponseEntity<Page<ItemEstoqueDTO>> findByEstoqueId(@PathVariable Long estoqueId, Pageable pageable) {
        Page<ItemEstoqueDTO> itensEstoque = itemEstoqueService.findByEstoqueId(estoqueId, pageable);
        return ResponseEntity.ok(itensEstoque);
    }

    @Operation(summary = "Excluir item de estoque por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemEstoqueService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Atualizar um item de estoque existente", description = "Atualiza os dados de um item do estoque com base no ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item de estoque atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemEstoqueDTO.class))),
            @ApiResponse(responseCode = "404", description = "Item de estoque não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ItemEstoqueDTO> atualizar(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados do item de estoque",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ItemEstoqueUpdateDTO.class)))
            @RequestBody ItemEstoqueUpdateDTO obj) {

                ItemEstoqueDTO itemEstoque = itemEstoqueService.atualizar(id, obj);
        return ResponseEntity.ok(itemEstoque);
    }

    @Operation(summary = "Criar um novo item de estoque", description = "Cadastra um novo item de estoque no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item Estoque criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemEstoqueCreateDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    @PostMapping()
    public ResponseEntity<ItemEstoqueDTO> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do item de estoque a ser criado",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ItemEstoqueCreateDTO.class)))
            @RequestBody ItemEstoqueCreateDTO obj) {

                ItemEstoqueDTO itemEstoque = itemEstoqueService.criar(obj);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(itemEstoque.id())
                .toUri();

        return ResponseEntity.created(uri).body(itemEstoque);
    }
}
