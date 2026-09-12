package com.clinic.controller;

import com.clinic.dto.LiveQueueDto;
import com.clinic.service.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/queue")
@AllArgsConstructor
public class LiveQueueController {

    private final QueueService queueService;

    @GetMapping(value = "/getQueue")
    @Operation(summary = "Get live Queue for the day")
    public ResponseEntity<LiveQueueDto> getLiveQueue(){
        var queue = queueService.getLiveQueue();
        return ResponseEntity.ok().body(queue);
    }
}
