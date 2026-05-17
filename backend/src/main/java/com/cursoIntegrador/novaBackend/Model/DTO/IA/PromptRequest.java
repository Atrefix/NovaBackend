package com.cursoIntegrador.novaBackend.Model.DTO.IA;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PromptRequest {
    private String prompt;
    private String mode;
}
