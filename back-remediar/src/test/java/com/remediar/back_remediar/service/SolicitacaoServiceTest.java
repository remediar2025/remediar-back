package com.remediar.back_remediar.service;

import com.remediar.back_remediar.model.*;
import com.remediar.back_remediar.model.dto.solicitacoes.*;
import com.remediar.back_remediar.model.enums.Status;
import com.remediar.back_remediar.model.mapper.ItemEstoqueMapper;
import com.remediar.back_remediar.model.mapper.SolicitacaoDoacaoMapper;
import com.remediar.back_remediar.model.mapper.SolicitacaoMapper;
import com.remediar.back_remediar.model.mapper.SolicitacaoPedidoMapper;
import com.remediar.back_remediar.repository.*;
import com.remediar.back_remediar.service.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServiceTest {

    @Mock
    private SolicitacaoPedidoRepository solicitacaoPedidoRepository;

    @Mock
    private ProdutoService produtoService;

    @Mock
    private ItemSolicitacaoPedidoRepository itemSolicitacaoPedidoRepository;

    @Mock
    private UsuarioComumService usuarioComumService;

    @Mock
    private SolicitacaoRepository solicitacaoRepository;

    @Mock
    private PrescricaoMedicaRepository prescricaoMedicaRepository;

    @Mock
    private SolicitacaoDoacaoRepository solicitacaoDoacaoRepository;

    @Mock
    private SolicitacaoDoacaoMapper solicitacaoDoacaoMapper;

    @Mock
    private ItemSolicitacaoDoacaoRepository itemSolicitacaoDoacaoRepository;

    @Mock
    private SolicitacaoPedidoMapper solicitacaoPedidoMapper;

    @Mock
    private SolicitacaoMapper solicitacaoMapper;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private HistoricoRepository historicoRepository;

    @Mock
    private HistoricoService historicoService;

    @Mock
    private ItemEstoqueService itemEstoqueService;

    @Mock
    private ItemEstoqueMapper itemEstoqueMapper;

    @InjectMocks
    private SolicitacaoService solicitacaoService;

    private SolicitacaoPedido solicitacaoPedido;
    private PedidoResponseDTO pedidoResponseDTO;

    @BeforeEach
    void setUp() {
        solicitacaoPedido = new SolicitacaoPedido();
        solicitacaoPedido.setId(UUID.randomUUID());
        solicitacaoPedido.setStatusAtual(Status.PENDENTE);

        pedidoResponseDTO = mock(PedidoResponseDTO.class);
    }

    private MockedStatic<SecurityContextHolder> mockUsuarioLogado(UserRole role) {
        User usuarioLogado = new User();
        usuarioLogado.setLogin("usuario.teste");
        usuarioLogado.setRole(role);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuarioLogado);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        return mockedSecurityContextHolder;
    }

    @Test
    void findById_WithValidId_ShouldReturnSolicitacao() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(solicitacaoRepository.findById(id)).thenReturn(Optional.of(solicitacaoPedido));

        // Act
        Solicitacao result = solicitacaoService.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(solicitacaoPedido, result);
        verify(solicitacaoRepository).findById(id);
    }

    @Test
    void findById_WithInvalidId_ShouldThrowObjectNotFoundException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(solicitacaoRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ObjectNotFoundException.class, () -> solicitacaoService.findById(id));
        verify(solicitacaoRepository).findById(id);
    }

    @Test
    void findAllPedidos_ShouldReturnPageOfPedidos() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<SolicitacaoPedido> solicitacoes = Arrays.asList(solicitacaoPedido);
        Page<SolicitacaoPedido> page = new PageImpl<>(solicitacoes, pageable, 1);

        when(solicitacaoPedidoRepository.findAll(ArgumentMatchers.<Specification<SolicitacaoPedido>>any(), eq(pageable)))
                .thenReturn(page);
        when(solicitacaoPedidoMapper.toPedidoResponseDTO(any(SolicitacaoPedido.class)))
                .thenReturn(pedidoResponseDTO);

        // Act
        Page<PedidoResponseDTO> result = solicitacaoService.findAllPedidos(
                pageable, null, null, null, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(solicitacaoPedidoMapper).toPedidoResponseDTO(solicitacaoPedido);
    }

    @Test
    void findAllPedidos_WithInvalidStatus_ShouldThrowIllegalArgumentException() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(IllegalArgumentException.class,
                () -> solicitacaoService.findAllPedidos(pageable, null, null, null, null, null, "NAO_EXISTE"));
    }

    @Test
    void findAllPedidos_WithInicioAfterFim_ShouldThrowIllegalArgumentException() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(IllegalArgumentException.class,
                () -> solicitacaoService.findAllPedidos(
                        pageable, null, null, null, "2026-07-15", "2026-07-01", null));
    }

    @Test
    void findAllDoacoes_ShouldReturnPageOfDoacoes() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        SolicitacaoDoacao solicitacaoDoacao = new SolicitacaoDoacao();
        List<SolicitacaoDoacao> solicitacoes = Arrays.asList(solicitacaoDoacao);
        Page<SolicitacaoDoacao> page = new PageImpl<>(solicitacoes, pageable, 1);
        DoacaoResponseDTO doacaoResponseDTO = mock(DoacaoResponseDTO.class);

        when(solicitacaoDoacaoRepository.findAll(pageable)).thenReturn(page);
        when(solicitacaoDoacaoMapper.toDoacaoResponseDTO(any(SolicitacaoDoacao.class)))
                .thenReturn(doacaoResponseDTO);

        // Act
        Page<DoacaoResponseDTO> result = solicitacaoService.findAllDoacoes(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(solicitacaoDoacaoRepository).findAll(pageable);
        verify(solicitacaoDoacaoMapper).toDoacaoResponseDTO(solicitacaoDoacao);
    }

    @Test
    void updateStatus_WithValidData_ShouldUpdateStatus() {
        // Arrange
        UUID solicitacaoId = UUID.randomUUID();
        String newStatus = "APROVADA";
        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacaoPedido));
        when(historicoService.saveHistorico(any(), any())).thenReturn(new Historico());

        // Act
        solicitacaoService.updateStatus(solicitacaoId, newStatus);

        // Assert
        assertEquals(Status.APROVADA, solicitacaoPedido.getStatusAtual());
        verify(solicitacaoRepository).findById(solicitacaoId);
        verify(solicitacaoRepository).save(solicitacaoPedido);
        verify(historicoService).saveHistorico(solicitacaoPedido, "Status alterado para: APROVADA");
    }

    @Test
    void updateStatus_WithInvalidId_ShouldThrowObjectNotFoundException() {
        // Arrange
        UUID solicitacaoId = UUID.randomUUID();
        String newStatus = "APROVADA";
        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ObjectNotFoundException.class, () -> solicitacaoService.updateStatus(solicitacaoId, newStatus));
        verify(solicitacaoRepository).findById(solicitacaoId);
        verify(solicitacaoRepository, never()).save(any());
    }

    @Test
    void aprovarSolicitacao_WithValidData_ShouldApproveSolicitacao() {
        // Arrange
        UUID solicitacaoId = UUID.randomUUID();
        Long funcionarioId = 1L;
        Funcionario funcionario = new Funcionario();
        funcionario.setId(funcionarioId);
        solicitacaoPedido.setFuncionarioResponsavelAtual(funcionario);

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacaoPedido));
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.of(funcionario));
        when(historicoService.saveHistorico(any(), any())).thenReturn(new Historico());

        // Act
        try (MockedStatic<SecurityContextHolder> ignored = mockUsuarioLogado(UserRole.FUNCIONARIO)) {
            solicitacaoService.aprovarSolicitacao(solicitacaoId, funcionarioId);
        }

        // Assert
        assertEquals(Status.APROVADA, solicitacaoPedido.getStatusAtual());
        assertEquals(funcionario, solicitacaoPedido.getFuncionarioResponsavelAtual());
        verify(solicitacaoRepository).findById(solicitacaoId);
        verify(funcionarioRepository).findById(funcionarioId);
        verify(solicitacaoRepository, atLeastOnce()).save(solicitacaoPedido);
    }

    @Test
    void aprovarSolicitacao_WithFuncionarioNaoEncontrado_ShouldThrow() {
        // Arrange - regressão: comportamento de FUNCIONARIO não deve mudar
        UUID solicitacaoId = UUID.randomUUID();
        Long funcionarioId = 1L;
        solicitacaoPedido.setFuncionarioResponsavelAtual(new Funcionario());

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacaoPedido));
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.empty());

        // Act & Assert
        try (MockedStatic<SecurityContextHolder> ignored = mockUsuarioLogado(UserRole.FUNCIONARIO)) {
            assertThrows(RuntimeException.class, () -> solicitacaoService.aprovarSolicitacao(solicitacaoId, funcionarioId));
        }
        verify(solicitacaoRepository, never()).save(any());
    }

    @Test
    void aprovarSolicitacao_AsAdminWithoutFuncionario_ShouldApproveAnyway() {
        // Arrange - ADMIN sem funcionário vinculado e solicitação ainda não reivindicada
        UUID solicitacaoId = UUID.randomUUID();
        Long funcionarioId = 999L;

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacaoPedido));
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.empty());
        when(historicoService.saveHistorico(any(), any())).thenReturn(new Historico());

        // Act
        try (MockedStatic<SecurityContextHolder> ignored = mockUsuarioLogado(UserRole.ADMIN)) {
            solicitacaoService.aprovarSolicitacao(solicitacaoId, funcionarioId);
        }

        // Assert
        assertEquals(Status.APROVADA, solicitacaoPedido.getStatusAtual());
        assertNull(solicitacaoPedido.getFuncionarioResponsavelAtual());
        verify(solicitacaoRepository, atLeastOnce()).save(solicitacaoPedido);
    }

    @Test
    void cancelarSolicitacao_WithValidData_ShouldCancelSolicitacao() {
        // Arrange
        UUID solicitacaoId = UUID.randomUUID();
        Long funcionarioId = 1L;
        Funcionario funcionario = new Funcionario();
        funcionario.setId(funcionarioId);
        solicitacaoPedido.setFuncionarioResponsavelAtual(funcionario);

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacaoPedido));
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.of(funcionario));
        when(historicoService.saveHistorico(any(), any())).thenReturn(new Historico());

        // Act
        try (MockedStatic<SecurityContextHolder> ignored = mockUsuarioLogado(UserRole.FUNCIONARIO)) {
            solicitacaoService.cancelarSolicitacao(solicitacaoId, funcionarioId);
        }

        // Assert
        assertEquals(Status.CANCELADA, solicitacaoPedido.getStatusAtual());
        assertEquals(funcionario, solicitacaoPedido.getFuncionarioResponsavelAtual());
        verify(solicitacaoRepository).findById(solicitacaoId);
        verify(funcionarioRepository).findById(funcionarioId);
        verify(solicitacaoRepository, atLeastOnce()).save(solicitacaoPedido);
    }

    @Test
    void updateFuncionarioResponsavelAtual_AsAdminWithoutFuncionario_ShouldClaimSolicitacao() {
        // Arrange
        UUID solicitacaoId = UUID.randomUUID();
        Long funcionarioId = 999L;

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacaoPedido));
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.empty());
        when(historicoService.saveHistorico(any(), any())).thenReturn(new Historico());

        // Act
        try (MockedStatic<SecurityContextHolder> ignored = mockUsuarioLogado(UserRole.ADMIN)) {
            solicitacaoService.updateFuncionarioResponsavelAtual(solicitacaoId, funcionarioId);
        }

        // Assert
        assertEquals(Status.EM_ANALISE, solicitacaoPedido.getStatusAtual());
        assertNull(solicitacaoPedido.getFuncionarioResponsavelAtual());
    }

    @Test
    void updateFuncionarioResponsavelAtual_AsAdminWithoutFuncionario_CalledTwice_ShouldThrowOnSecondCall() {
        // Arrange
        UUID solicitacaoId = UUID.randomUUID();
        Long funcionarioId = 999L;

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacaoPedido));
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.empty());
        when(historicoService.saveHistorico(any(), any())).thenReturn(new Historico());

        // Act & Assert
        try (MockedStatic<SecurityContextHolder> ignored = mockUsuarioLogado(UserRole.ADMIN)) {
            solicitacaoService.updateFuncionarioResponsavelAtual(solicitacaoId, funcionarioId);
            assertThrows(IllegalStateException.class,
                    () -> solicitacaoService.updateFuncionarioResponsavelAtual(solicitacaoId, funcionarioId));
        }
    }
} 