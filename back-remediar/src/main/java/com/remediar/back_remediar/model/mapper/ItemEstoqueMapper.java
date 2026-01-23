package com.remediar.back_remediar.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.remediar.back_remediar.model.ItemEstoque;
import com.remediar.back_remediar.model.Medicamento;
import com.remediar.back_remediar.model.dto.ItemEstoqueDTO;

@Mapper(componentModel = "spring")
public interface ItemEstoqueMapper {

    default ItemEstoqueDTO toDTO(ItemEstoque entity) {
        if (entity == null || entity.getProduto() == null) {
            return null;
        }

        String principioAtivo = null;
        if (entity.getProduto() instanceof Medicamento) {
            principioAtivo = ((Medicamento) entity.getProduto()).getPrincipioAtivo();
        }

        return new ItemEstoqueDTO(
                entity.getId(),
                entity.getProduto().getNomeComercial(),
                principioAtivo,
                entity.getProduto().getApresentacao(),
                entity.getProduto().getId(),
                entity.getDataValidade(),
                entity.getQuantidade()
        );
    }

    //@Mapping(source = "nomeComercialOrPrincipioAtivo", target = "produto.id")
    ItemEstoque toEntity(ItemEstoqueDTO dto);
}
