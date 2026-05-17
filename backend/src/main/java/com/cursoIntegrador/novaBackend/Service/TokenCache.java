package com.cursoIntegrador.novaBackend.Service;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
public class TokenCache {
    private final Cache<String, String> tokenCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(500).build();

    public void guardarToken(String email, String token) {
        tokenCache.put(email, token);
    }

    public String obtenerToken(String email) {
        return tokenCache.getIfPresent(email);
    }

    public void eliminarToken(String email) {
        tokenCache.invalidate(email);
    }
}
