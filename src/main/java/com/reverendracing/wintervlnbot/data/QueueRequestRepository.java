package com.reverendracing.wintervlnbot.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueueRequestRepository extends JpaRepository<QueueRequest, String> {
    List<QueueRequest> findByCarNumber(final String carNumber);
}
