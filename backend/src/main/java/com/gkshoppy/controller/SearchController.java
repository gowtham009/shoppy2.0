package com.gkshoppy.controller;

import com.gkshoppy.service.SerpApiService;
import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchController {

    private final SerpApiService serpApiService;

    public SearchController(SerpApiService serpApiService) {
        this.serpApiService = serpApiService;
    }

    // Keep API search endpoint
    @GetMapping(value = "/api/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> apiSearch(@RequestParam(name = "q") String query) {
        try {
            String json = serpApiService.searchShopping(query);
            return ResponseEntity.ok(json);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("{\"error\":\"" + e.getMessage().replace("\"","\\\"") + "\"}");
        }
    }
}
