package com.cursoIntegrador.novaBackend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cursoIntegrador.novaBackend.Model.DTO.IA.PromptRequest;
import com.cursoIntegrador.novaBackend.Service.IA.GeminiService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/IA")
public class IAController {

    @Autowired
    private GeminiService gemservice;

    @PostMapping("/consulta")
    public String consulta(@RequestBody PromptRequest request) {
        return gemservice.novaPromptCompuesto(request.getPrompt(), request.getMode());
    }

}
