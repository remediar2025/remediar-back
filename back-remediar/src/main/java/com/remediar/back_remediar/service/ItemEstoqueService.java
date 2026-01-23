package com.remediar.back_remediar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.remediar.back_remediar.model.Estoque;
import com.remediar.back_remediar.model.ItemEstoque;
import com.remediar.back_remediar.model.Produto;
import com.remediar.back_remediar.model.dto.ItemEstoqueCreateDTO;
import com.remediar.back_remediar.model.dto.ItemEstoqueDTO;
import com.remediar.back_remediar.model.dto.ItemEstoqueUpdateDTO;
import com.remediar.back_remediar.model.mapper.ItemEstoqueMapper;
import com.remediar.back_remediar.repository.EstoqueRepository;
import com.remediar.back_remediar.repository.ItemEstoqueRepository;
import com.remediar.back_remediar.repository.ProdutoRepository;

@Service
public class ItemEstoqueService {

    private final ItemEstoqueRepository itemEstoqueRepository;

    private final ItemEstoqueMapper itemEstoqueMapper;

    private final ProdutoRepository produtoRepository;

    private final EstoqueRepository estoqueRepository;

    public ItemEstoqueService(ItemEstoqueRepository itemEstoqueRepository, ItemEstoqueMapper itemEstoqueMapper, ProdutoRepository produtoRepository, EstoqueRepository estoqueRepository){
        this.itemEstoqueRepository = itemEstoqueRepository;
        this.itemEstoqueMapper = itemEstoqueMapper;
        this.produtoRepository = produtoRepository;
        this.estoqueRepository = estoqueRepository;
    }
    
    @Transactional(readOnly = true)
    public ItemEstoque findById(Long id) {
        return itemEstoqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item de estoque não encontrado."));
    }

    @Transactional(readOnly = true)
    public Page<ItemEstoqueDTO> findAll(Pageable pageable) {
        return itemEstoqueRepository.findAll(pageable)
                .map(itemEstoqueMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ItemEstoqueDTO> findByEstoqueId(Long estoqueId, Pageable pageable) {
        return itemEstoqueRepository.findByEstoqueId(estoqueId, pageable)
                .map(itemEstoqueMapper::toDTO);
    }

    @Transactional
    public ItemEstoqueDTO criar(ItemEstoqueCreateDTO dto) {
        System.out.println("ItemEstoqueService.criar() - Iniciando criação de item de estoque com DTO: " + dto);
        Produto produto = produtoRepository.findById(dto.produtoId())
                      .orElseThrow(() -> new RuntimeException("Produto não encontrado"));      
                      
        Estoque estoque = estoqueRepository.findById(dto.estoqueId())
                    .orElseThrow(() -> new RuntimeException("Estoque não encontrado"));

        ItemEstoque itemExistente = itemEstoqueRepository.findByProdutoAndEstoqueAndDataValidade(produto, estoque, dto.dataValidade());

        if (itemExistente != null) {
            itemExistente.setQuantidade(itemExistente.getQuantidade() + dto.quantidade());
            itemEstoqueRepository.save(itemExistente);

            //TODO
            // chama a fila de espera de medicamentos aguardando item estoque (MICROSERVICE)
            // e notifica o usuário da primeira solicitação aguardando este medicamento.



            return itemEstoqueMapper.toDTO(itemExistente);
        } else {
            ItemEstoque item = new ItemEstoque();
            item.setProduto(produto);
            item.setEstoque(estoque);
            item.setDataValidade(dto.dataValidade());
            item.setQuantidade(dto.quantidade());
            itemEstoqueRepository.save(item);

            //TODO
            // chama a fila de espera de medicamentos aguardando item estoque (MICROSERVICE)
            // e notifica o usuário da primeira solicitação aguardando este medicamento.


            return itemEstoqueMapper.toDTO(item);
        }
    }

    @Transactional
    public ItemEstoqueDTO atualizar(Long id, ItemEstoqueUpdateDTO dto) {
    ItemEstoque item = itemEstoqueRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Item de estoque não encontrado"));

    item.setDataValidade(dto.dataValidade());
    item.setQuantidade(dto.quantidade());

    itemEstoqueRepository.save(item);
    return itemEstoqueMapper.toDTO(item);
    }
    

    @Transactional
    public void delete(Long id) {
        findById(id);
        itemEstoqueRepository.deleteById(id);
    }
}
