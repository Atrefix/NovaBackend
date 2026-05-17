package com.cursoIntegrador.novaBackend.Model.DTO.Login;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {
    private String email;
    private String token;
    private String nuevaPassword;
}
