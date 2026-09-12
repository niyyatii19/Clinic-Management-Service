package com.clinic.service.impl;

import com.clinic.dto.LiveQueueDto;
import com.clinic.repository.LiveQueueRepository;
import com.clinic.service.QueueService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final LiveQueueRepository liveQueueRepository;

    @Override
    public LiveQueueDto getLiveQueue() {
        return null;
    }
}
