package dev.franke.felipe.website_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.franke.felipe.website_backend.model.InternalLog;
import dev.franke.felipe.website_backend.service.InternalLogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal_log")
@RequiredArgsConstructor
public class InternalLogController {

    private final InternalLogService internalLogService;

    @GetMapping
    public List<InternalLog> getRecentLogs() {
        return internalLogService.getFirstLogs();
    }

    @GetMapping("{id}")
    public InternalLog getLogById(@PathVariable String id) {
        return internalLogService.getLogById(id);
    }

}
