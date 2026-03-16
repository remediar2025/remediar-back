package com.remediar.back_remediar.service;

import com.remediar.back_remediar.model.Endereco;
import com.remediar.back_remediar.model.dto.EnderecoDTO;
import com.remediar.back_remediar.repository.EnderecoRepository;
import org.springframework.stereotype.Service;

@Service
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;

    public EnderecoService(EnderecoRepository enderecoRepository) {
        this.enderecoRepository = enderecoRepository;
    }

    public EnderecoDTO save(EnderecoDTO enderecoDTO) {
        Endereco endereco = fromDto(enderecoDTO);
        enderecoRepository.save(endereco);
        return toDto(endereco);
    }

    public Endereco saveAndReturnEntity(EnderecoDTO enderecoDTO) {
        Endereco endereco = fromDto(enderecoDTO);
        return enderecoRepository.save(endereco);
    }

    public EnderecoDTO toDto(Endereco endereco) {
        EnderecoDTO enderecoDTO = new EnderecoDTO();
        enderecoDTO.setRua(endereco.getRua());
        enderecoDTO.setNumero(endereco.getNumero());
        enderecoDTO.setBairro(endereco.getBairro());
        enderecoDTO.setCidade(endereco.getCidade());
        enderecoDTO.setEstado(endereco.getEstado());
        enderecoDTO.setCep(endereco.getCep());

        return enderecoDTO;
    }

    public Endereco fromDto(EnderecoDTO enderecoDTO) {
        Endereco endereco = new Endereco();
        endereco.setRua(enderecoDTO.getRua());
        endereco.setNumero(enderecoDTO.getNumero());
        endereco.setBairro(enderecoDTO.getBairro());
        endereco.setCidade(enderecoDTO.getCidade());
        endereco.setEstado(enderecoDTO.getEstado());
        endereco.setCep(enderecoDTO.getCep());
        return endereco;
    }
}
