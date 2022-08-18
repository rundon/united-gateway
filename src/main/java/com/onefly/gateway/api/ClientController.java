package com.onefly.gateway.api;

import com.onefly.gateway.constant.Result;
import com.onefly.gateway.dto.CreateClientDto;
import com.onefly.gateway.dto.UpdateClientDto;
import com.onefly.gateway.entity.Client;
import com.onefly.gateway.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Sean createAt 2021/6/25
 */
@RestController
@RequestMapping("/clients")
public class ClientController extends AbstractController {

    @Autowired
    private ClientService clientService;

    @GetMapping("/{id}")
    public Result getOne(@PathVariable String id, ServerHttpRequest request) {
        return success(clientService.getClient(id));
    }

    @PostMapping
    public Result create(@RequestBody @Valid CreateClientDto dto, ServerHttpRequest request) {
        Client client = clientService.save(dto);
        return success(client);
    }

    @PutMapping
    public Result update(@RequestBody @Valid UpdateClientDto dto, ServerHttpRequest request) {
        Client client = clientService.update(dto);
        return success(client);
    }
}
