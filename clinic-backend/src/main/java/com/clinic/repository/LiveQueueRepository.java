package com.clinic.repository;

import com.clinic.models.LiveQueue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiveQueueRepository extends JpaRepository<LiveQueue, Long> {
}
