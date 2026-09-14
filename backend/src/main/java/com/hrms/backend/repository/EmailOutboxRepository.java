package com.hrms.backend.repository;

import com.hrms.backend.entity.EmailOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, Integer> {
    List<EmailOutbox> findByStatusOrderByCreatedAtAsc(String status);
}
