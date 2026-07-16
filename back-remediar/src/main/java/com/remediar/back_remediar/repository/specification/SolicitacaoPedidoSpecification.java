package com.remediar.back_remediar.repository.specification;

import com.remediar.back_remediar.model.ItemSolicitacaoPedido;
import com.remediar.back_remediar.model.Medicamento;
import com.remediar.back_remediar.model.Produto;
import com.remediar.back_remediar.model.SolicitacaoPedido;
import com.remediar.back_remediar.model.dto.solicitacoes.PedidoFiltroDTO;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SolicitacaoPedidoSpecification {

    private SolicitacaoPedidoSpecification() {
    }

    public static Specification<SolicitacaoPedido> comFiltros(PedidoFiltroDTO filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.id() != null) {
                predicates.add(cb.equal(root.get("id"), filtro.id()));
            }

            if (StringUtils.hasText(filtro.nomeSolicitante())) {
                predicates.add(cb.like(cb.lower(root.get("usuarioComum").get("nome")),
                        "%" + filtro.nomeSolicitante().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filtro.medicamento())) {
                String termo = "%" + filtro.medicamento().toLowerCase() + "%";
                Join<SolicitacaoPedido, ItemSolicitacaoPedido> itemJoin = root.join("itemSolicitacaoPedido");
                Join<ItemSolicitacaoPedido, Produto> produtoJoin = itemJoin.join("produto");

                Predicate porNomeComercial = cb.like(cb.lower(produtoJoin.get("nomeComercial")), termo);
                Predicate porPrincipioAtivo = cb.like(
                        cb.lower(cb.treat(produtoJoin, Medicamento.class).get("principioAtivo")), termo);

                predicates.add(cb.or(porNomeComercial, porPrincipioAtivo));
            }

            if (filtro.dataSolicitacaoInicio() != null) {
                LocalDateTime inicio = filtro.dataSolicitacaoInicio().atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataHoraCriacao"), inicio));
            }

            if (filtro.dataSolicitacaoFim() != null) {
                LocalDateTime fim = filtro.dataSolicitacaoFim().atTime(23, 59, 59, 999_000_000);
                predicates.add(cb.lessThanOrEqualTo(root.get("dataHoraCriacao"), fim));
            }

            if (filtro.status() != null) {
                predicates.add(cb.equal(root.get("statusAtual"), filtro.status()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
