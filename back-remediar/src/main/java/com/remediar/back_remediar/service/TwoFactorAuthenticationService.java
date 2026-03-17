package com.remediar.back_remediar.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class TwoFactorAuthenticationService {

    private final RedisTemplate<String, String> redisTemplate;

    public TwoFactorAuthenticationService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String gerarCodigo(String email) {
        String codigo = String.format("%06d", new SecureRandom().nextInt(999999));
        redisTemplate.opsForValue().set("2fa:" + email, codigo, Duration.ofMinutes(15));
        return codigo;
    }

    public boolean validarCodigo(String email, String codigo) {
        String chave = "2fa:" + email;
        String armazenado = redisTemplate.opsForValue().get(chave);
        if (codigo.equals(armazenado)) {
            redisTemplate.delete(chave);
            return true;
        }
        return false;
    }
}
