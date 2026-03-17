package com.remediar.back_remediar.service;

import com.remediar.back_remediar.model.*;
import com.remediar.back_remediar.model.dto.EnderecoDTO;
import com.remediar.back_remediar.model.dto.usuarios.UserDTO;
import com.remediar.back_remediar.model.dto.usuarios.UsuarioComumDTO;
import com.remediar.back_remediar.model.dto.usuarios.UsuarioComumPFDTO;
import com.remediar.back_remediar.model.enums.StatusConta;
import com.remediar.back_remediar.repository.UserRepository;
import com.remediar.back_remediar.repository.UsuarioComumPFRepository;
import com.remediar.back_remediar.repository.UsuarioComumPJRepository;
import com.remediar.back_remediar.repository.UsuarioComumRepository;
import com.remediar.back_remediar.service.exceptions.ObjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class UsuarioComumService {

    private final UsuarioComumRepository usuarioComumRepository;

    private final UsuarioComumPFRepository usuarioComumPFRepository;

    private final UsuarioComumPJRepository usuarioComumPJRepository;

    private final AuthorizationService authService;

    private final EnderecoService enderecoService;
    private final UserRepository userRepository;

    public UsuarioComumService(UsuarioComumRepository usuarioComumRepository,
            UsuarioComumPFRepository usuarioComumPFRepository,
            UsuarioComumPJRepository usuarioComumPJRepository,
            AuthorizationService authService,
            EnderecoService enderecoService, UserRepository userRepository) {
        this.usuarioComumRepository = usuarioComumRepository;
        this.usuarioComumPFRepository = usuarioComumPFRepository;
        this.usuarioComumPJRepository = usuarioComumPJRepository;
        this.authService = authService;
        this.enderecoService = enderecoService;
        this.userRepository = userRepository;
    }

    public UsuarioComum findById(Long id) {
        UsuarioComum usuarioComum = usuarioComumRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (usuarioComum instanceof UsuarioComumPJ) {
            return usuarioComumPJRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        } else {
            return usuarioComumPFRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        }
    }

    public UsuarioComum findByEmail(String email) {
        return usuarioComumRepository.findByUser_Login(email)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário não encontrado"));
    }

    public List<Record> listar() {
        return usuarioComumRepository.findAll().stream()
                .map(usuarioComum -> {
                    if (usuarioComum instanceof UsuarioComumPF) {
                        return pfToDto((UsuarioComumPF) usuarioComum);
                    } else {
                        return pjToDto((UsuarioComumPJ) usuarioComum);
                    }
                })
                .toList();
    }

   @Transactional
public UsuarioComumDTO createPJ(UsuarioComumDTO obj) {
    if (authService.checkLoginExits(obj.user().getLogin())) {
        throw new IllegalArgumentException("Já existe um cadastro com esse e-mail.");
    }

    if (obj.documento().length() == 11) {
        if (usuarioComumPFRepository.existsByCpf(obj.documento())) {
            throw new IllegalArgumentException("Já existe um cadastro com esse cpf.");
        } else {
            throw new RuntimeException("Não é possível cadastrar um CPF como PJ.");
        }
    } else if (obj.documento().length() == 14) {
        if (usuarioComumPJRepository.existsByCnpj(obj.documento())) {
            throw new IllegalArgumentException("Já existe um cadastro com esse cnpj.");
        }

        UsuarioComumPJ usuarioComumPJ = new UsuarioComumPJ();
        usuarioComumPJ.setNome(obj.nome());
        usuarioComumPJ.setTelefone(obj.telefone());
        usuarioComumPJ.setCnpj(obj.documento());

        // Preparar e salvar o endereço primeiro
        Endereco endereco = enderecoService.saveAndReturnEntity(obj.endereco());
        usuarioComumPJ.setEndereco(endereco);

        // Criar e configurar o User
        UserDTO userDTO = obj.user();
        userDTO.setPassword(authService.encryptPassword(userDTO.getPassword()));
        userDTO.setRole(UserRole.USER);
        
        User user = userDtoToEntity(userDTO);
        user.setStatus(StatusConta.PENDENTE_VERIFICACAO);
        
        // Associar o user ao usuário PJ
        usuarioComumPJ.setUser(user);

        // Salvar tudo
        usuarioComumPJRepository.save(usuarioComumPJ);
        
        return pjToDto(usuarioComumPJ);
    } else {
        throw new IllegalArgumentException("O documento deve ter 11 ou 14 dígitos.");
    }
}

    @Transactional
public UsuarioComumPFDTO createPF(UsuarioComumPFDTO obj) {
    try {
        if (authService.checkLoginExits(obj.usuario().user().getLogin())) {
            throw new IllegalArgumentException("Já existe um cadastro com esse e-mail.");
        }

        if (obj.usuario().documento().length() == 14) {
            if (usuarioComumPJRepository.existsByCnpj(obj.usuario().documento())) {
                throw new IllegalArgumentException("Já existe um cadastro com esse cnpj.");
            } else {
                throw new RuntimeException("Não é possível cadastrar um CNPJ como PF.");
            }
        } else if (obj.usuario().documento().length() == 11) {
            if (usuarioComumPFRepository.existsByCpf(obj.usuario().documento())) {
                throw new IllegalArgumentException("Já existe um cadastro com esse cpf.");
            }

            UsuarioComumPF usuarioComumPF = new UsuarioComumPF();
            usuarioComumPF.setNome(obj.usuario().nome());
            usuarioComumPF.setTelefone(obj.usuario().telefone());
            usuarioComumPF.setCpf(obj.usuario().documento());

            // Preparar e salvar o endereço primeiro
            Endereco endereco = enderecoService.saveAndReturnEntity(obj.usuario().endereco());
            usuarioComumPF.setEndereco(endereco);

            // Criar e configurar o User
            UserDTO userDTO = obj.usuario().user();
            userDTO.setPassword(authService.encryptPassword(userDTO.getPassword()));
            userDTO.setRole(UserRole.USER);
            
            User user = userDtoToEntity(userDTO);
            user.setStatus(StatusConta.PENDENTE_VERIFICACAO);
            
            // Associar o user ao usuário PF
            usuarioComumPF.setUser(user);

            usuarioComumPF.setDataNascimento(obj.dataNascimento());
            usuarioComumPF.setGenero(obj.genero());

            // Salvar tudo
            usuarioComumPFRepository.save(usuarioComumPF);

            return pfToDto(usuarioComumPF);
        } else {
            throw new IllegalArgumentException("O documento deve ter 11 ou 14 dígitos.");
        }
    } catch (org.springframework.data.redis.RedisConnectionFailureException e) {
        // Log do erro mas não interrompe o fluxo
        System.err.println("Redis não disponível, continuando sem cache: " + e.getMessage());
        
        // Continua o fluxo normal sem Redis
        if (authService.checkLoginExits(obj.usuario().user().getLogin())) {
            throw new IllegalArgumentException("Já existe um cadastro com esse e-mail.");
        }

        UsuarioComumPF usuarioComumPF = new UsuarioComumPF();
        usuarioComumPF.setNome(obj.usuario().nome());
        usuarioComumPF.setTelefone(obj.usuario().telefone());
        usuarioComumPF.setCpf(obj.usuario().documento());

        Endereco endereco = enderecoService.saveAndReturnEntity(obj.usuario().endereco());
        usuarioComumPF.setEndereco(endereco);

        UserDTO userDTO = obj.usuario().user();
        userDTO.setPassword(authService.encryptPassword(userDTO.getPassword()));
        userDTO.setRole(UserRole.USER);
        
        User user = userDtoToEntity(userDTO);
        user.setStatus(StatusConta.PENDENTE_VERIFICACAO);
        usuarioComumPF.setUser(user);

        usuarioComumPF.setDataNascimento(obj.dataNascimento());
        usuarioComumPF.setGenero(obj.genero());

        usuarioComumPFRepository.save(usuarioComumPF);

        return pfToDto(usuarioComumPF);
    }
}

    @Transactional
    public UsuarioComumDTO atualizarPJ(Long id, UsuarioComumDTO obj) {
        UsuarioComum usuario = findById(id);
        if (usuario instanceof UsuarioComumPJ usuarioComumPJ) {
            usuarioComumPJ.setNome(obj.nome());
            usuarioComumPJ.setTelefone(obj.telefone());
            usuarioComumPJ.setCnpj(obj.documento());

            if (!Objects.equals(obj.endereco().getRua(), usuario.getEndereco().getRua()) ||
                    !Objects.equals(obj.endereco().getNumero(), usuario.getEndereco().getNumero()) ||
                    !Objects.equals(obj.endereco().getBairro(), usuario.getEndereco().getBairro()) ||
                    !Objects.equals(obj.endereco().getCidade(), usuario.getEndereco().getCidade()) ||
                    !Objects.equals(obj.endereco().getEstado(), usuario.getEndereco().getEstado()) ||
                    !Objects.equals(obj.endereco().getCep(), usuario.getEndereco().getCep())) {
                EnderecoDTO enderecoDTO = enderecoService.save(obj.endereco());
                usuarioComumPJ.setEndereco(enderecoService.fromDto(enderecoDTO));
            }

            usuarioComumPJ.setUser(userDtoToEntity(obj.user()));
            usuarioComumPJRepository.save(usuarioComumPJ);
            return pjToDto(usuarioComumPJ);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não é possível atualizar um usuário PF como PJ.");
        }
    }

    @Transactional
    public UsuarioComumPFDTO atualizarPF(Long id, UsuarioComumPFDTO obj) {
        UsuarioComum usuario = findById(id);
        if (usuario instanceof UsuarioComumPF usuarioComumPF) {
            usuarioComumPF.setNome(obj.usuario().nome());
            usuarioComumPF.setTelefone(obj.usuario().telefone());
            usuarioComumPF.setCpf(obj.usuario().documento());

            if (!Objects.equals(obj.usuario().endereco().getRua(), usuario.getEndereco().getRua()) ||
                    !Objects.equals(obj.usuario().endereco().getNumero(), usuario.getEndereco().getNumero()) ||
                    !Objects.equals(obj.usuario().endereco().getBairro(), usuario.getEndereco().getBairro()) ||
                    !Objects.equals(obj.usuario().endereco().getCidade(), usuario.getEndereco().getCidade()) ||
                    !Objects.equals(obj.usuario().endereco().getEstado(), usuario.getEndereco().getEstado()) ||
                    !Objects.equals(obj.usuario().endereco().getCep(), usuario.getEndereco().getCep())) {
                EnderecoDTO enderecoDTO = enderecoService.save(obj.usuario().endereco());
                usuarioComumPF.setEndereco(enderecoService.fromDto(enderecoDTO));
            }

            usuarioComumPF.setEscolaridade(obj.escolaridade());
            usuarioComumPF.setQtdPessoasCasa(obj.qtdPessoasCasa());
            usuarioComumPF.setRendaFamiliar(obj.rendaFamiliar());
            usuarioComumPF.setGenero(obj.genero());
            usuarioComumPF.setDataNascimento(obj.dataNascimento());
            usuarioComumPF.setUser(userDtoToEntity(obj.usuario().user()));

            usuarioComumPFRepository.save(usuarioComumPF);
            return pfToDto(usuarioComumPF);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não é possível atualizar um usuário PJ como PF.");
        }
    }

    @Transactional
    public void deletar(Long id) {
        findById(id);
        usuarioComumRepository.deleteById(id);
    }

    public UserDTO userEntityToDto(UsuarioComum usuario) {
        UserDTO userDTO = new UserDTO();
        userDTO.setLogin(usuario.getUser().getLogin());
        userDTO.setPassword(usuario.getUser().getPassword());
        userDTO.setRole(usuario.getUser().getRole());

        return userDTO;
    }

    public User userDtoToEntity(UserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin());
        user.setPassword(userDTO.getPassword());
        user.setRole(userDTO.getRole());

        return user;
    }

    public UsuarioComumDTO pjToDto(UsuarioComumPJ usuarioComumPJ) {
        UserDTO userDTO = userEntityToDto(usuarioComumPJ);

        return new UsuarioComumDTO(
                usuarioComumPJ.getId(),
                usuarioComumPJ.getNome(),
                usuarioComumPJ.getCnpj(),
                usuarioComumPJ.getTelefone(),
                enderecoService.toDto(usuarioComumPJ.getEndereco()),
                userDTO);
    }

    public UsuarioComumPFDTO pfToDto(UsuarioComumPF usuarioComum) {
        UserDTO userDTO = userEntityToDto(usuarioComum);

        return new UsuarioComumPFDTO(
                new UsuarioComumDTO(
                        usuarioComum.getId(),
                        usuarioComum.getNome(),
                        usuarioComum.getCpf(),
                        usuarioComum.getTelefone(),
                        enderecoService.toDto(usuarioComum.getEndereco()),
                        userDTO),
                usuarioComum.getGenero(),
                usuarioComum.getDataNascimento(),
                usuarioComum.getEscolaridade(),
                usuarioComum.getQtdPessoasCasa(),
                usuarioComum.getRendaFamiliar());
    }

    @Transactional
    public void ativarUsuario(String email) {
        User usuario = (User) userRepository.findByLogin(email);

        if (usuario == null) {
            throw new ObjectNotFoundException("Usuário não encontrado");
        }

        usuario.setStatus(StatusConta.ATIVA);
        userRepository.save(usuario);
    }

    @Transactional
    public boolean isVerificado(String email) {
        User usuario = (User) userRepository.findByLogin(email);

        if (usuario == null) {
            throw new ObjectNotFoundException("Usuário não encontrado");
        }

        return usuario.getStatus() == StatusConta.ATIVA;
    }
}