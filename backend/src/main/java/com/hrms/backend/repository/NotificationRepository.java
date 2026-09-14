package com.hrms.backend.repository;

import com.hrms.backend.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    Page<Notification> findByRecipientEmployeeIdOrderByCreatedAtDesc(Integer employeeId, Pageable pageable);
    long countByRecipientEmployeeIdAndIsReadFalse(Integer employeeId);
    List<Notification> findByRecipientEmployeeIdAndIsReadFalse(Integer employeeId);
}
