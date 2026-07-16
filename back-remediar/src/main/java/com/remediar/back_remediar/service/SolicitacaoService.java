package com.remediar.back_remediar.service;

import com.remediar.back_remediar.model.*;
import com.remediar.back_remediar.model.dto.ItemEstoqueCreateDTO;
import com.remediar.back_remediar.model.dto.solicitacoes.*;
import com.remediar.back_remediar.model.enums.Status;
import com.remediar.back_remediar.model.mapper.ItemEstoqueMapper;
import com.remediar.back_remediar.model.mapper.SolicitacaoDoacaoMapper;
import com.remediar.back_remediar.model.mapper.SolicitacaoMapper;
import com.remediar.back_remediar.model.mapper.SolicitacaoPedidoMapper;
import com.remediar.back_remediar.repository.*;
import com.remediar.back_remediar.repository.specification.SolicitacaoPedidoSpecification;
import com.remediar.back_remediar.service.exceptions.ObjectNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SolicitacaoService {

    private final SolicitacaoPedidoRepository solicitacaoPedidoRepository;
    private final ProdutoService produtoService;
    private final ItemSolicitacaoPedidoRepository itemSolicitacaoPedidoRepository;
    private final UsuarioComumService usuarioComumService;
    private final SolicitacaoRepository solicitacaoRepository;
    private final PrescricaoMedicaRepository prescricaoMedicaRepository;
    private final SolicitacaoDoacaoRepository solicitacaoDoacaoRepository;
    private final SolicitacaoDoacaoMapper solicitacaoDoacaoMapper;
    private final ItemSolicitacaoDoacaoRepository itemSolicitacaoDoacaoRepository;
    private final SolicitacaoPedidoMapper solicitacaoPedidoMapper;
    private final SolicitacaoMapper solicitacaoMapper;
    private final FuncionarioRepository funcionarioRepository;
    private final HistoricoRepository historicoRepository;
    private final HistoricoService historicoService;
    private final ItemEstoqueService itemEstoqueService;
    private final ItemEstoqueMapper itemEstoqueMapper;

    public SolicitacaoService(SolicitacaoPedidoRepository solicitacaoPedidoRepository,
                              ProdutoService produtoService,
                              ItemSolicitacaoPedidoRepository itemSolicitacaoPedidoRepository,
                              UsuarioComumService usuarioComumService,
                              SolicitacaoRepository solicitacaoRepository,
                              PrescricaoMedicaRepository prescricaoMedicaRepository,
                              SolicitacaoDoacaoRepository solicitacaoDoacaoRepository,
                              SolicitacaoDoacaoMapper solicitacaoDoacaoMapper,
                              ItemSolicitacaoDoacaoRepository itemSolicitacaoDoacaoRepository,
                              SolicitacaoPedidoMapper solicitacaoPedidoMapper,
                              SolicitacaoMapper solicitacaoMapper,
                              FuncionarioRepository funcionarioRepository, HistoricoRepository historicoRepository, HistoricoService historicoService, ItemEstoqueService itemEstoqueService, ItemEstoqueMapper itemEstoqueMapper) {
        this.solicitacaoPedidoRepository = solicitacaoPedidoRepository;
        this.produtoService = produtoService;
        this.itemSolicitacaoPedidoRepository = itemSolicitacaoPedidoRepository;
        this.usuarioComumService = usuarioComumService;
        this.solicitacaoRepository = solicitacaoRepository;
        this.prescricaoMedicaRepository = prescricaoMedicaRepository;
        this.solicitacaoDoacaoRepository = solicitacaoDoacaoRepository;
        this.solicitacaoDoacaoMapper = solicitacaoDoacaoMapper;
        this.itemSolicitacaoDoacaoRepository = itemSolicitacaoDoacaoRepository;
        this.solicitacaoPedidoMapper = solicitacaoPedidoMapper;
        this.solicitacaoMapper = solicitacaoMapper;
        this.funcionarioRepository = funcionarioRepository;
        this.historicoRepository = historicoRepository;
        this.historicoService = historicoService;
        this.itemEstoqueService = itemEstoqueService;
        this.itemEstoqueMapper = itemEstoqueMapper;
    }

    @Transactional(readOnly = true)
    public Solicitacao findById(UUID id) {
        return solicitacaoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Solicitação não encontrada"));
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> findAllPedidos(Pageable pageable, String id, String nomeSolicitante,
                                                   String medicamento, String dataSolicitacaoInicio,
                                                   String dataSolicitacaoFim, String status) {
        PedidoFiltroDTO filtro = montarFiltroPedido(id, nomeSolicitante, medicamento, dataSolicitacaoInicio,
                dataSolicitacaoFim, status);
        Specification<SolicitacaoPedido> spec = SolicitacaoPedidoSpecification.comFiltros(filtro);
        Page<SolicitacaoPedido> solicitacoes = solicitacaoPedidoRepository.findAll(spec, pageable);
        return solicitacoes.map(solicitacaoPedidoMapper::toPedidoResponseDTO);
    }

    private PedidoFiltroDTO montarFiltroPedido(String id, String nomeSolicitante, String medicamento,
                                                String dataSolicitacaoInicio, String dataSolicitacaoFim,
                                                String status) {
        UUID idFiltro = StringUtils.hasText(id) ? parseUUID(id) : null;
        Status statusFiltro = StringUtils.hasText(status) ? parseStatus(status) : null;
        LocalDate inicio = StringUtils.hasText(dataSolicitacaoInicio)
                ? parseData(dataSolicitacaoInicio, "dataSolicitacaoInicio") : null;
        LocalDate fim = StringUtils.hasText(dataSolicitacaoFim)
                ? parseData(dataSolicitacaoFim, "dataSolicitacaoFim") : null;

        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new IllegalArgumentException("dataSolicitacaoInicio não pode ser posterior a dataSolicitacaoFim");
        }

        return new PedidoFiltroDTO(idFiltro, nomeSolicitante, medicamento, inicio, fim, statusFiltro);
    }

    private UUID parseUUID(String valor) {
        try {
            return UUID.fromString(valor);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Valor de 'id' inválido: " + valor);
        }
    }

    private Status parseStatus(String valor) {
        try {
            return Status.valueOf(valor);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Status inválido: '" + valor + "'. Valores aceitos: " + Arrays.toString(Status.values()));
        }
    }

    private LocalDate parseData(String valor, String nomeParametro) {
        try {
            return LocalDate.parse(valor, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Formato inválido para '" + nomeParametro + "'. Use o formato yyyy-MM-dd.");
        }
    }

    @Transactional
    public PedidoResponseDTO criarSolicitacaoPedido(PedidoRequestDTO obj) {
        if (obj.item() == null) {
            throw new ObjectNotFoundException("Nenhum item encontrado na solicitação de pedido");
        }

        UsuarioComum usuario = usuarioComumService.findById(obj.usuarioId());

        if (obj.prescricaoMedica() == null) {
            throw new ObjectNotFoundException("Nenhuma prescrição médica encontrada na solicitação de pedido");
        }
        PrescricaoMedica prescricaoMedica = solicitacaoMapper.toPrescricaoMedica(obj.prescricaoMedica());
        prescricaoMedica = prescricaoMedicaRepository.save(prescricaoMedica);

        SolicitacaoPedido solicitacaoPedido = SolicitacaoFactory.criarSolicitacaoPedido(usuario);
        solicitacaoPedido.setModoEntrega(obj.modoEntrega());
        solicitacaoPedido.setPrescricaoMedica(prescricaoMedica);

        ItemSolicitacaoPedido itemSolicitacaoPedido = new ItemSolicitacaoPedido();
        itemSolicitacaoPedido.setProduto(produtoService.findMedicamentosByDescricao(obj.item().nomeComercialOrPrincipioAtivo()).getFirst());
        itemSolicitacaoPedido.setQuantidade(obj.item().quantidade());
        itemSolicitacaoPedido.setSolicitacaoPedido(solicitacaoPedido);

        solicitacaoPedido.setItemSolicitacaoPedido(itemSolicitacaoPedido);

        solicitacaoPedidoRepository.save(solicitacaoPedido);

        prescricaoMedica.getSolicitacoes().add(solicitacaoPedido);

        usuario.getSolicitacoes().add(solicitacaoPedido);

        Historico historico = historicoService.saveHistorico(solicitacaoPedido, "Solicitação do tipo pedido criada");
        solicitacaoPedido.getHistoricos().add(historico);

        return solicitacaoPedidoMapper.toPedidoResponseDTO(solicitacaoPedido);
    }

    @Transactional
    public PedidoResponseDTO updateSolicitacaoPedido(UUID id, PedidoPatchRequestDTO obj) {
        SolicitacaoPedido solicitacao = solicitacaoPedidoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Solicitação do tipo Pedido não encontrada"));

        if (obj.item() == null && obj.modoEntrega() == null && obj.prescricaoMedica() == null) {
            throw new ObjectNotFoundException("Nenhum dado encontrado para atualizar");
        }

        solicitacao.setStatusAtual(Status.ALTERADA_PELO_USUARIO);
        LocalDateTime agora = LocalDateTime.now();

        StringBuilder observacao = new StringBuilder("Solicitação alterada");

        if (obj.modoEntrega() != null) {
            solicitacao.setModoEntrega(obj.modoEntrega());
            observacao.append(" - Modo de entrega alterado");
        }

        if (obj.item() != null) {
            Produto produto = produtoService.findMedicamentosByDescricao(obj.item().nomeComercialOrPrincipioAtivo()).getFirst();

            if (solicitacao.getItemSolicitacaoPedido() != null) {
                itemSolicitacaoPedidoRepository.delete(solicitacao.getItemSolicitacaoPedido());
            }

            ItemSolicitacaoPedido novoItem = new ItemSolicitacaoPedido();
            novoItem.setProduto(produto);
            novoItem.setQuantidade(obj.item().quantidade());
            novoItem.setSolicitacaoPedido(solicitacao);
            itemSolicitacaoPedidoRepository.save(novoItem);
            solicitacao.setItemSolicitacaoPedido(novoItem);

            observacao.append(" - Item alterado");
        }

        if (obj.prescricaoMedica() != null) {
            PrescricaoMedica prescricao = solicitacao.getPrescricaoMedica();

            if (prescricao != null) {
                solicitacaoMapper.updatePrescricaoFromPatchDTO(obj.prescricaoMedica(), prescricao);
                prescricaoMedicaRepository.save(prescricao);
                observacao.append(" - Prescrição alterada");
                prescricao.getSolicitacoes().add(solicitacao);
            } else {
                throw new ObjectNotFoundException("Prescrição médica não encontrada");
            }
        }

        solicitacao.setDataHoraUltimaAtualizacao(agora);

        Historico historico = historicoService.saveHistorico(solicitacao, observacao.toString());
        solicitacao.getHistoricos().add(historico);
        solicitacaoPedidoRepository.save(solicitacao);

        return solicitacaoPedidoMapper.toPedidoResponseDTO(solicitacao);
    }

    @Transactional(readOnly = true)
    public Page<DoacaoResponseDTO> findAllDoacoes(Pageable pageable) {
        Page<SolicitacaoDoacao> solicitacoes = solicitacaoDoacaoRepository.findAll(pageable);
        return solicitacoes.map(solicitacaoDoacaoMapper::toDoacaoResponseDTO);
    }

    @Transactional
    public DoacaoResponseDTO criarSolicitacaoDoacao(DoacaoRequestDTO obj) {

        if (obj.itens() == null || obj.itens().isEmpty()) {
            throw new ObjectNotFoundException("Nenhum item encontrado na solicitação de doação");
        }

        UsuarioComum usuario = usuarioComumService.findById(obj.usuarioId());
        SolicitacaoDoacao solicitacaoDoacao = SolicitacaoFactory.criarSolicitacaoDoacao(usuario);

        solicitacaoDoacao = solicitacaoDoacaoRepository.save(solicitacaoDoacao);

        List<ItemSolicitacaoDoacao> itens = new ArrayList<>();

        for (ItemDoacaoRequestDTO i : obj.itens()) {
            ItemSolicitacaoDoacao itemSolicitacaoDoacao = new ItemSolicitacaoDoacao();

            List<Medicamento> medicamentos = produtoService.findMedicamentosByDescricao(i.descricao());
            Medicamento medicamentoSelecionado = medicamentos.getFirst();

            itemSolicitacaoDoacao.setProduto(medicamentoSelecionado);
            itemSolicitacaoDoacao.setQuantidade(i.quantidade());
            itemSolicitacaoDoacao.setSolicitacaoDoacao(solicitacaoDoacao);
            itemSolicitacaoDoacao.setDataValidade(i.dataValidade());
            itemSolicitacaoDoacao.setImagem(i.imagem());

            itens.add(itemSolicitacaoDoacao);
        }

        itemSolicitacaoDoacaoRepository.saveAll(itens);

        solicitacaoDoacao.setItensDoacao(itens);
        solicitacaoDoacaoRepository.save(solicitacaoDoacao);

        usuario.getSolicitacoes().add(solicitacaoDoacao);

        Historico historico = historicoService.saveHistorico(solicitacaoDoacao, "Solicitação do tipo doação criada");

        solicitacaoDoacao.getHistoricos().add(historico);

        return solicitacaoDoacaoMapper.toDoacaoResponseDTO(solicitacaoDoacao);
    }

    @Transactional
    public DoacaoResponseDTO userUpdatesDoacao(UUID id, DoacaoPatchRequestDTO obj) {
        SolicitacaoDoacao solicitacao = solicitacaoDoacaoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Solicitação do tipo Doação não encontrada"));

        if (obj.itens() != null) {
            solicitacao.setStatusAtual(Status.ALTERADA_PELO_USUARIO);
            Historico historico = historicoService.saveHistorico(solicitacao, "Solicitação alterada pelo usuário");

            historico.setObservacao(historico.getObservacao() + " - Itens antigos: ");
            for (ItemSolicitacao i : solicitacao.getItensDoacao()) {
                historico.setObservacao(historico.getObservacao() + i.getProduto().getId() + "; ");
            }

            historico.setObservacao(historico.getObservacao() + " - Itens novos: ");
            for (ItemDoacaoDTO i : obj.itens()) {
                historico.setObservacao(historico.getObservacao() + i.item().nomeComercialOrPrincipioAtivo() + "; ");
            }

            itemSolicitacaoDoacaoRepository.deleteAllBySolicitacaoDoacaoId(solicitacao.getId());
            solicitacao.getItensDoacao().clear();

            for (ItemDoacaoDTO dto : obj.itens()) {
                Produto produto = produtoService.findMedicamentosByDescricao(dto.item().nomeComercialOrPrincipioAtivo()).getFirst();

                ItemSolicitacaoDoacao novoItem = new ItemSolicitacaoDoacao();
                novoItem.setProduto(produto);
                novoItem.setQuantidade(dto.item().quantidade());
                novoItem.setSolicitacaoDoacao(solicitacao);
                novoItem.setDataValidade(dto.dataValidade());
                novoItem.setImagem(dto.imagem());

                solicitacao.getItensDoacao().add(novoItem);
            }

            historicoRepository.save(historico);
            solicitacao.getHistoricos().add(historico);
            itemSolicitacaoDoacaoRepository.saveAll(solicitacao.getItensDoacao());
            solicitacao.setDataHoraUltimaAtualizacao(historico.getDataHora());

        }

        return solicitacaoDoacaoMapper.toDoacaoResponseDTO(solicitacao);
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> findAllSolicitacaoPedidoByStatusAtual(String status, Pageable pageable) {
        return findAllPedidos(pageable, null, null, null, null, null, status);
    }

    @Transactional(readOnly = true)
    public Page<DoacaoResponseDTO> findAllSolicitacaoDoacaoByStatusAtual(String status, Pageable pageable) {
        Page<SolicitacaoDoacao> solicitacoes = solicitacaoDoacaoRepository.findAllByStatusAtual(Status.valueOf(status), pageable);
        return solicitacoes.map(solicitacaoDoacaoMapper::toDoacaoResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> findAllSolicitacaoPedidoByAutorId(Long id) {
        return solicitacaoPedidoRepository.findAllByUsuarioComum_Id(id).stream()
                .map(solicitacaoPedidoMapper::toPedidoResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DoacaoResponseDTO> findAllSolicitacaoDoacaoByAutorId(Long id) {
        return solicitacaoDoacaoRepository.findAllByUsuarioComum_Id(id).stream()
                .map(solicitacaoDoacaoMapper::toDoacaoResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Record> findAllSolicitacaoByFuncionarioResponsavelAtualId(Long id) {
        return solicitacaoRepository.findAllByFuncionarioResponsavelAtual_Id(id).stream()
                .map(s -> {
                    if (s instanceof SolicitacaoPedido pedido) {
                        return solicitacaoPedidoMapper.toPedidoResponseDTO(pedido);
                    } else if (s instanceof SolicitacaoDoacao doacao) {
                        return solicitacaoDoacaoMapper.toDoacaoResponseDTO(doacao);
                    } else {
                        throw new IllegalStateException("Tipo de solicitação desconhecida: " + s.getClass() + ".");
                    }
                })
                .toList();


    }

    @Transactional(readOnly = true)
    public List<Record> findAllSolicitacaoByFuncionarioResponsavelAtualIdAndStatus(Long id, String status) {
        Status statusEnum = Status.fromDescricao(status);
        return solicitacaoRepository.findAllByFuncionarioResponsavelAtual_IdAndStatusAtual(id, statusEnum).stream()
                .map(s -> {
                    if (s instanceof SolicitacaoPedido pedido) {
                        return solicitacaoPedidoMapper.toPedidoResponseDTO(pedido);
                    } else if (s instanceof SolicitacaoDoacao doacao) {
                        return solicitacaoDoacaoMapper.toDoacaoResponseDTO(doacao);
                    } else {
                        throw new IllegalStateException("Tipo de solicitação desconhecida: " + s.getClass() + ".");
                    }
                })
                .toList();
    }

    private User getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    private Funcionario resolveFuncionario(Long funcionarioId, boolean isAdmin) {
        Funcionario funcionario = funcionarioRepository.findById(funcionarioId).orElse(null);
        if (funcionario == null && !isAdmin) {
            throw new RuntimeException("Funcionário não encontrado");
        }
        return funcionario;
    }

    @Transactional
    public void updateFuncionarioResponsavelAtual(UUID solicitacaoId, Long funcionarioId) {
        Solicitacao solicitacao = findById(solicitacaoId);
        User usuarioLogado = getUsuarioLogado();
        boolean isAdmin = usuarioLogado.getRole() == UserRole.ADMIN;
        Funcionario funcionario = resolveFuncionario(funcionarioId, isAdmin);

        boolean jaAssumidaPorAdminSemFuncionario = funcionario == null
                && solicitacao.getFuncionarioResponsavelAtual() == null
                && solicitacao.getStatusAtual() == Status.EM_ANALISE;

        if (jaAssumidaPorAdminSemFuncionario) {
            throw new IllegalStateException("Solicitação já foi assumida.");
        }

        if (funcionario != null && solicitacao.getFuncionarioResponsavelAtual() == funcionario) {
            throw new IllegalStateException("Funcionário(a) já é responsável atual pela solicitação.");
        }

        solicitacao.setDataHoraUltimaAtualizacao(LocalDateTime.now());
        solicitacaoRepository.save(solicitacao);

        Historico historico;

        if (solicitacao.getFuncionarioResponsavelAtual() == null) {
            if (funcionario != null) {
                solicitacao.setFuncionarioResponsavelAtual(funcionario);
            }
            solicitacao.setStatusAtual(Status.EM_ANALISE);
            solicitacaoRepository.save(solicitacao);
            String quemAssumiu = funcionario != null ? "Funcionário(a) " + funcionario.getNome() : "Administrador(a)";
            historico = historicoService.saveHistorico(solicitacao, quemAssumiu + " assumiu a solicitação.");
        } else {
            if (funcionario != null) {
                solicitacao.setFuncionarioResponsavelAtual(funcionario);
            }
            String quemAtualizou = funcionario != null ? "Funcionário(a) " + funcionario.getNome() : "Administrador(a)";
            historico = historicoService.saveHistorico(solicitacao, "Responsável atualizado para " + quemAtualizou + ".");
        }

        historicoRepository.save(historico);

        solicitacao.getHistoricos().add(historico);
        solicitacaoRepository.save(solicitacao);
    }

    @Transactional
    public void updateStatus(UUID solicitacaoId, String status) {
        Solicitacao solicitacao = findById(solicitacaoId);
        Status novoStatus = Status.fromDescricao(status);

        if (solicitacao.getStatusAtual() == novoStatus) {
            throw new IllegalStateException("Solicitação já está com o status " + novoStatus);
        }

        solicitacao.setStatusAtual(novoStatus);
        solicitacao.setDataHoraUltimaAtualizacao(LocalDateTime.now());
        solicitacaoRepository.save(solicitacao);

        solicitacao.getHistoricos().add(historicoService.saveHistorico(solicitacao, "Status atualizado para " + novoStatus + "."));
    }

    @Transactional
    public void aprovarSolicitacao(UUID solicitacaoId, Long funcionarioId) {
        Solicitacao solicitacao = findById(solicitacaoId);
        User usuarioLogado = getUsuarioLogado();
        boolean isAdmin = usuarioLogado.getRole() == UserRole.ADMIN;
        Funcionario funcionario = resolveFuncionario(funcionarioId, isAdmin);

        if (solicitacao.getFuncionarioResponsavelAtual() == null && !isAdmin) {
            throw new IllegalStateException("Funcionário(a) responsável não encontrado");
        }

        if (solicitacao.getStatusAtual() == Status.APROVADA) {
            throw new IllegalStateException("Solicitação já está aprovada");
        }

        // BAIXA NO ESTOQUE
    if (solicitacao instanceof SolicitacaoPedido pedido) {
        ItemSolicitacaoPedido itemPedido = pedido.getItemSolicitacaoPedido();
        
        if (itemPedido != null) {
            Produto produto = itemPedido.getProduto();
            int quantidadeSolicitada = itemPedido.getQuantidade();
            
            Optional<ItemEstoque> itemEstoqueOpt = itemEstoqueService.findByProduto(produto);
            
            if (itemEstoqueOpt.isEmpty()) {
                throw new IllegalStateException(
                    "Produto '" + produto.getNomeComercial() + "' não encontrado no estoque"
                );
            }
            
            ItemEstoque itemEstoque = itemEstoqueOpt.get();
            
            if (itemEstoque.getQuantidade() < quantidadeSolicitada) {
                throw new IllegalStateException(
                    "Estoque insuficiente para o produto '" + produto.getNomeComercial() + 
                    "'. Disponível: " + itemEstoque.getQuantidade() + 
                    ", Solicitado: " + quantidadeSolicitada
                );
            }

            System.out.println("Baixando estoque para produto '" + produto.getNomeComercial() + 
                "'. Quantidade atual: " + itemEstoque.getQuantidade() + 
                ", Quantidade solicitada: " + quantidadeSolicitada);
            
            int novaQuantidade = itemEstoque.getQuantidade() - quantidadeSolicitada;
            itemEstoqueService.atualizarQuantidade(itemEstoque.getId(), novaQuantidade);
        }
    }


        solicitacao.setStatusAtual(Status.APROVADA);
        if (funcionario != null) {
            solicitacao.setFuncionarioResponsavelAtual(funcionario);
        }
        solicitacao.setDataHoraUltimaAtualizacao(LocalDateTime.now());
        solicitacaoRepository.save(solicitacao);

        Historico historico = historicoService.saveHistorico(solicitacao, "Solicitação aprovada.");
        solicitacao.getHistoricos().add(historico);
        solicitacaoRepository.save(solicitacao);
    }

    @Transactional
    public void cancelarSolicitacao(UUID solicitacaoId, Long funcionarioId) {
        Solicitacao solicitacao = findById(solicitacaoId);
        User usuarioLogado = getUsuarioLogado();
        boolean isAdmin = usuarioLogado.getRole() == UserRole.ADMIN;
        Funcionario funcionario = resolveFuncionario(funcionarioId, isAdmin);

        if (solicitacao.getFuncionarioResponsavelAtual() == null && !isAdmin) {
            throw new IllegalStateException("Funcionário(a) responsável não encontrado");
        }

        if (solicitacao.getStatusAtual() == Status.CANCELADA) {
            throw new IllegalStateException("Solicitação já está cancelada");
        }

        solicitacao.setStatusAtual(Status.CANCELADA);
        if (funcionario != null) {
            solicitacao.setFuncionarioResponsavelAtual(funcionario);
        }
        solicitacao.setDataHoraUltimaAtualizacao(LocalDateTime.now());
        solicitacao.setDataHoraFinalizacao(LocalDateTime.now());
        solicitacaoRepository.save(solicitacao);

        Historico historico = historicoService.saveHistorico(solicitacao, "Solicitação cancelada.");
        solicitacao.getHistoricos().add(historico);
        solicitacaoRepository.save(solicitacao);
    }

    @Transactional
    public void separarSolicitacao(UUID solicitacaoId, Long funcionarioId) {
        Solicitacao solicitacao = findById(solicitacaoId);
        User usuarioLogado = getUsuarioLogado();
        boolean isAdmin = usuarioLogado.getRole() == UserRole.ADMIN;
        Funcionario funcionario = resolveFuncionario(funcionarioId, isAdmin);

        if (solicitacao.getFuncionarioResponsavelAtual() == null && !isAdmin) {
            throw new IllegalStateException("Funcionário(a) responsável não encontrado");
        }

        if (solicitacao.getStatusAtual() == Status.SEPARADA) {
            throw new IllegalStateException("Solicitação já está separada");
        }

        if (solicitacao.getStatusAtual() == Status.APROVADA) {
            solicitacao.setStatusAtual(Status.SEPARADA);
            if (funcionario != null) {
                solicitacao.setFuncionarioResponsavelAtual(funcionario);
            }
            solicitacao.setDataHoraUltimaAtualizacao(LocalDateTime.now());
            solicitacaoRepository.save(solicitacao);

            Historico historico = historicoService.saveHistorico(solicitacao, "Solicitação separada");
            solicitacao.getHistoricos().add(historico);
            solicitacaoRepository.save(solicitacao);
        } else {
            throw new IllegalStateException("Solicitação não pode ser separada, pois não está aprovada");
        }
    }

    @Transactional
    public void prontoParaRetirarSolicitacao(UUID solicitacaoId, Long funcionarioId) {

        Solicitacao solicitacao = findById(solicitacaoId);
        User usuarioLogado = getUsuarioLogado();
        boolean isAdmin = usuarioLogado.getRole() == UserRole.ADMIN;

        if (solicitacao.getFuncionarioResponsavelAtual() == null && !isAdmin) {
            throw new IllegalStateException("Funcionário(a) responsável não encontrado");
        }

        if (solicitacao.getStatusAtual() == Status.AGUARDANDO_RETIRADA) {
            throw new IllegalStateException("Solicitação já está AGUARDANDO_RETIRADA");
        }

        Funcionario funcionario = resolveFuncionario(funcionarioId, isAdmin);

        if (solicitacao.getStatusAtual() == Status.SEPARADA) {

            if (solicitacao instanceof SolicitacaoDoacao) {
                solicitacao.setStatusAtual(Status.AGUARDANDO_USUARIO);
            } else {
                solicitacao.setStatusAtual(Status.AGUARDANDO_RETIRADA);
            }

            if (funcionario != null) {
                solicitacao.setFuncionarioResponsavelAtual(funcionario);
            }
            solicitacao.setDataHoraUltimaAtualizacao(LocalDateTime.now());
            solicitacaoRepository.save(solicitacao);

            Historico historico = historicoService.saveHistorico(solicitacao, "Solicitação aguardando usuário.");
            solicitacao.getHistoricos().add(historico);
            solicitacaoRepository.save(solicitacao);
        } else {
            throw new IllegalStateException("Solicitação não pode ser marcada como aguardando, pois não está separada");
        }
    }

    @Transactional
    public void concluirSolicitacao(UUID solicitacaoId, Long funcionarioId) {
        Solicitacao solicitacao = findById(solicitacaoId);
        User usuarioLogado = getUsuarioLogado();
        boolean isAdmin = usuarioLogado.getRole() == UserRole.ADMIN;
        Funcionario funcionario = resolveFuncionario(funcionarioId, isAdmin);

        if (solicitacao.getFuncionarioResponsavelAtual() == null && !isAdmin) {
            throw new IllegalStateException("Funcionário(a) responsável não encontrado");
        }

        if (solicitacao.getStatusAtual() == Status.CONCLUIDA) {
            throw new IllegalStateException("Solicitação já está FINALIZADA");
        }

            solicitacao.setStatusAtual(Status.CONCLUIDA);
            if (funcionario != null) {
                solicitacao.setFuncionarioResponsavelAtual(funcionario);
            }
            solicitacao.setDataHoraUltimaAtualizacao(LocalDateTime.now());
            solicitacao.setDataHoraFinalizacao(LocalDateTime.now());
            solicitacaoRepository.save(solicitacao);

            Historico historico = historicoService.saveHistorico(solicitacao, "Solicitação finalizada.");
            solicitacao.getHistoricos().add(historico);
            solicitacaoRepository.save(solicitacao);

            if (solicitacao instanceof SolicitacaoDoacao doacao) {
                for (ItemSolicitacaoDoacao item : doacao.getItensDoacao()) {
                    itemEstoqueService.criar(new ItemEstoqueCreateDTO(
                            item.getProduto().getId(),
                            item.getDataValidade(),
                            item.getQuantidade(),
                            1L
                    ));
                }
            }
    }

    @Transactional(readOnly = true)
    public EstatisticasPedidoDTO getEstatisticasPedidosConcluidos(Long usuarioId) {
        if (usuarioId != null) {
            return getEstatisticasPedidosConcluidosPorUsuario(usuarioId);
        } else {
            return getEstatisticasPedidosConcluidosGerais();
        }
    }

    @Transactional(readOnly = true)
    private EstatisticasPedidoDTO getEstatisticasPedidosConcluidosGerais() {
        List<SolicitacaoPedido> pedidosConcluidos = solicitacaoPedidoRepository.findAllByStatusAtual(Status.CONCLUIDA);
        
        Long quantidadeTotalMedicamentos = pedidosConcluidos.stream()
                .mapToLong(pedido -> pedido.getItemSolicitacaoPedido() != null ? pedido.getItemSolicitacaoPedido().getQuantidade() : 0)
                .sum();
        
        Long quantidadeSolicitacoes = (long) pedidosConcluidos.size();
        
        return new EstatisticasPedidoDTO(
                quantidadeTotalMedicamentos,
                quantidadeSolicitacoes,
                null
        );
    }

    @Transactional(readOnly = true)
    private EstatisticasPedidoDTO getEstatisticasPedidosConcluidosPorUsuario(Long usuarioId) {
        List<SolicitacaoPedido> pedidosConcluidos = solicitacaoPedidoRepository.findAllByStatusAtualAndUsuarioComum_Id(Status.CONCLUIDA, usuarioId);
        
        if (pedidosConcluidos.isEmpty()) {
            return new EstatisticasPedidoDTO(0L, 0L, "Usuário não encontrado ou sem pedidos concluídos");
        }
        
        Long quantidadeTotalMedicamentos = pedidosConcluidos.stream()
                .mapToLong(pedido -> pedido.getItemSolicitacaoPedido() != null ? pedido.getItemSolicitacaoPedido().getQuantidade() : 0)
                .sum();
        
        Long quantidadeSolicitacoes = (long) pedidosConcluidos.size();
        
        String usuarioNome = pedidosConcluidos.get(0).getUsuarioComum().getNome();
        
        return new EstatisticasPedidoDTO(
                quantidadeTotalMedicamentos,
                quantidadeSolicitacoes,
                usuarioNome
        );
    }
}
