// Reference: https://www.youtube.com/watch?v=ouhBu-Sp3Hw
package com.example.client.controller;

import com.example.client.service.BackendClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ClientController {

    private final BackendClient backendClient;

    public ClientController(BackendClient backendClient) {
        this.backendClient = backendClient;
    }

   
    @GetMapping("/")
    public Map<String, Object> index() {
        return backendClient.callBackend();
    }
}
