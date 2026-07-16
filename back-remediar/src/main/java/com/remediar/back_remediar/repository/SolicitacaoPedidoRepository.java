package com.remediar.back_remediar.repository;

import com.remediar.back_remediar.model.SolicitacaoPedido;
import com.remediar.back_remediar.model.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface SolicitacaoPedidoRepository extends JpaRepository<SolicitacaoPedido, UUID>, JpaSpecificationExecutor<SolicitacaoPedido> {
    List<SolicitacaoPedido> findAllByUsuarioComum_Id(Long id);

    Page<SolicitacaoPedido> findAllByStatusAtual(Status status, Pageable pageable);

    List<SolicitacaoPedido> findAllByStatusAtual(Status status);
    
    List<SolicitacaoPedido> findAllByStatusAtualAndUsuarioComum_Id(Status status, Long usuarioId);

    @Query("SELECT COUNT(sd) FROM SolicitacaoPedido sd WHERE sd.statusAtual = :status")
    Long countByStatusAtual(@Param("status") Status status);
    
    @Query("SELECT COUNT(sd) FROM SolicitacaoPedido sd WHERE sd.statusAtual = :status AND sd.usuarioComum.id = :usuarioId")
    Long countByStatusAtualAndUsuarioComum_Id(@Param("status") Status status, @Param("usuarioId") Long usuarioId);
}
