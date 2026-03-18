package com.remediar.back_remediar.service;

import com.remediar.back_remediar.model.Historico;
import com.remediar.back_remediar.model.Solicitacao;
// import com.remediar.back_remediar.model.dto.notificacoes.NotificationRequestDto;
// import com.remediar.back_remediar.model.dto.notificacoes.TipoCanal;
// import com.remediar.back_remediar.producer.NotificationProducer;
import com.remediar.back_remediar.repository.HistoricoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class HistoricoService {

    private final HistoricoRepository historicoRepository;
    // private final NotificationProducer notificationProducer;

    public HistoricoService(HistoricoRepository historicoRepository/*, NotificationProducer notificationProducer*/) {
        this.historicoRepository = historicoRepository;
        // this.notificationProducer = notificationProducer;
    }

    @Transactional
    public Historico saveHistorico(Solicitacao solicitacao, String observacao) {
        Historico historico = new Historico();
        historico.setStatus(solicitacao.getStatusAtual());
        historico.setSolicitacao(solicitacao);
        historico.setObservacao(observacao);
        historico.setDataHora(LocalDateTime.now());
        historico.setFuncionario(solicitacao.getFuncionarioResponsavelAtual());
        historicoRepository.save(historico);

        // String titulo = "";
        // String mensagem = "";
        // String tipoSolicitacao = solicitacao.getTipoSolicitacao().getDescricao();

        // if (observacao.toUpperCase().contains("CRIADA")) {
        //     titulo = "Criação de Solicitação";
        //     mensagem = "Olá, " + historico.getSolicitacao().getUsuarioComum().getNome()
        //             + "\nSua solicitação de código: " + historico.getSolicitacao().getId() + " foi criada."
        //             + "\nCaso não tenha solicitado, desconsidere esta mensagem.";
        // }

        // if (observacao.toUpperCase().contains("APROVADA")) {
        //     titulo = "Aprovação de Solicitação";
        //     mensagem = "Olá, " + historico.getSolicitacao().getUsuarioComum().getNome()
        //             + "! Sua solicitação de " + tipoSolicitacao + ", de código: " + historico.getSolicitacao().getId() + " foi aprovada."
        //             + "\nAguarde a separação dos itens.";
        // }

        // if (observacao.toUpperCase().contains("CANCELADA")) {
        //     titulo = "Cancelamento de Solicitação";
        //     mensagem = "Olá, " + historico.getSolicitacao().getUsuarioComum().getNome()
        //             + "! Sua solicitação de " + tipoSolicitacao + ", de código: " + historico.getSolicitacao().getId() + " foi cancelada."
        //             + "\nCaso não tenha solicitado, desconsidere esta mensagem.";
        // }

        // if (observacao.toUpperCase().contains("SEPARADA")) {
        //     titulo = "Separação de Solicitação";
        //     mensagem = "Olá, " + historico.getSolicitacao().getUsuarioComum().getNome()
        //             + "! Sua solicitação de" + tipoSolicitacao + ", de código: " + historico.getSolicitacao().getId() + " foi separada."
        //             + "\nAguarde a retirada dos itens.";
        // }

        // if (observacao.toUpperCase().contains("AGUARDANDO")) {
        //     titulo = "Aguardando Resposta";
        //     mensagem = "Olá, " + historico.getSolicitacao().getUsuarioComum().getNome()
        //             + "! Sua solicitação de" + tipoSolicitacao + ", código: " + historico.getSolicitacao().getId() + " está aguardando sua resposta."
        //             + "\nPor favor, responda o mais breve possível.";
        // }

        // if (observacao.toUpperCase().contains("FINALIZADA")) {
        //     titulo = "Finalização de Solicitação";
        //     mensagem = "Olá, " + historico.getSolicitacao().getUsuarioComum().getNome()
        //             + "! Sua solicitação de" + tipoSolicitacao + ", código: " + historico.getSolicitacao().getId() + " foi finalizada."
        //             + "\nObrigado por utilizar a Remediar!";
        // }

        // if (!titulo.isEmpty()) {
        //     notificationProducer.sendNotification(new NotificationRequestDto(
        //             historico.getSolicitacao().getUsuarioComum().getId(),
        //             historico.getSolicitacao().getUsuarioComum().getUser().getLogin(),
        //             historico.getSolicitacao().getUsuarioComum().getUser().getLogin(),
        //             titulo,
        //             mensagem,
        //             TipoCanal.EMAIL
        //     ));
        // }

        //TODO
        // fila-solicitacao.pedido-create (Status.PENDENTE) -> email E zap consomem
        // fila-solicitacao.aprovacao (Status.APROVADA [MENSAGEM APROVADA], Status.CANCELADA [MENSAGEM CANCELADA]) -> email E Zap consome
        //      se aprovada e se tem itemEstoque RESERVA para esta solicitação e "retira" quantidade em estoque -> fila-solicitacao.pedido-separacao (Status.SEPARADA) -> email consome  - ALTERAR SOLICITAÇÃO PARA CANCELADA, POR ALGUM MOTIVO, VERIFICAR SE TINHA RESERVA E VOLTAR OS ITENS QUE FORAM RESERVADOS
        //      se aprovada e NÃO tem itemEstoque -> fila-solicitacao.pedido-sem-estoque (Status.SEM_ESTOQUE) -> email consome E NÃO SOME DA FILA - SÓ SOME DA FILA QUANDO O ITEM ESTIVER DISPONÍVEL
        //              se chegar um itemEstoque e tiver solicitação com esse item na filaDeEspera -> fila-solicitacao.aguardando-resposta-usuario (Status.AGUARDANDO_RESPOSTA_USUARIO) -> email E zap consome
        //              se usuario confirmou que quer o item -> fila-solicitacao.pedido-separacao (Status.SEPARADA) -> email consome
        //              se usuario não confirmou que quer o item OU caducou 2 dias uteis -> fila-solicitacao.cancelada (Status.CANCELADA) -> email E zap consomem
        // fila-solicitacao.pedido-aguardando-retirada (Status.AGUARDANDO_RETIRADA) -> email E zap consomem
        //      se passar 5 dias, E SE tiver solicitação com o mesmo medicamento, E SE a data de vencimento do remedio for menor que até daqui 35 dias -> fila.solicitacao.aguardando-usuario (Status.AGUARDANDO_USUARIO) - não some AINDA da fila -> email E zap consomem
        //      se passar 10 dias, E SE tiver solicitação com o mesmo medicamento, E SE a data de vencimento do remedio for menor que até daqui 30 dias -> fila.solicitacao.cancelada (Status.CANCELADA) - some da fila e VOLTA em estoque o que tinha sido reservado -> email E zap consomem
        // fila-solicitacao.pedido-aguardando-entrega (Status.AGUARDANDO_ENTREGA) -> email
        // fila-solicitacao.pedido-em-transporte (Status.EM_TRANSPORTE) -> email
        // fila-solicitacao.concluida (Status.CONCLUIDA) -> email E zap consomem
        // .
        // .
        // fila-solicitacao.doacao-create (Status.PENDENTE) -> email consome
        // fila-solicitacao.aprovacao (Status.APROVADA [MENSAGEM APROVADA], Status.CANCELADA [MENSAGEM CANCELADA]) -> email E Zap consome
        // se aprovada -> fila-solicitacao.aguardando-usuario (Status.AGUARDANDO_USUARIO) -> email E zap consomem
        //      se o cara não entregou em 5 dias uteis fila-solicitacao.aguardando-usuario (Status.AGUARDANDO_USUARIO) -> email E zap consomem
        //              se o cara não entregou -> fila-solicitacao.cancelada (Status.CANCELADA) -> email E zap consomem
        //      se o cara entregou -> fila-solicitacao.concluida (Status.CONCLUIDA) -> email E zap consomem
        // .
        // fila-solicitacao.pedido-create
        // fila-solicitacao.aprovacao
        // fila-solicitacao.pedido-separacao
        // fila-solicitacao.pedido-sem-estoque
        // fila-solicitacao.aguardando-resposta-usuario
        // fila-solicitacao.cancelada
        // fila-solicitacao.pedido-aguardando-retirada
        // fila-solicitacao.pedido-aguardando-entrega
        // fila-solicitacao.pedido-em-transporte
        // fila-solicitacao.concluida
        // .
        // fila-solicitacao.doacao-create
        // fila-solicitacao.aguardando-usuario
        // .
        // TOTAL DE 12 FILAS
        // .
        // Status.PENDENTE
        // Status.APROVADA
        // Status.CANCELADA
        // Status.SEPARADA
        // Status.SEM_ESTOQUE
        // Status.AGUARDANDO_RESPOSTA_USUARIO
        // Status.AGUARDANDO_RETIRADA
        // Status.AGUARDANDO_ENTREGA
        // Status.AGUARDANDO_USUARIO
        // Status.EM_TRANSPORTE
        // Status.CONCLUIDA

        return historico;
    }
}
