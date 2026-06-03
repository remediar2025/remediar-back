package com.remediar.back_remediar.repository;

import com.remediar.back_remediar.model.Estoque;
import com.remediar.back_remediar.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.remediar.back_remediar.model.ItemEstoque;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ItemEstoqueRepository extends JpaRepository<ItemEstoque, Long> {

    ItemEstoque findByProdutoAndEstoqueAndDataValidade(Produto produto, Estoque estoque, LocalDate localDate);
    
    Page<ItemEstoque> findByEstoqueId(Long estoqueId, Pageable pageable);

    Optional<ItemEstoque> findByProduto(Produto produto);

}