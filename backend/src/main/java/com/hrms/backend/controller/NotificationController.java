package com.hrms.backend.controller;

import com.hrms.backend.entity.Employee;
import com.hrms.backend.entity.Notification;
import com.hrms.backend.entity.NotificationPreference;
import com.hrms.backend.entity.User;
import com.hrms.backend.repository.NotificationPreferenceRepository;
import com.hrms.backend.repository.NotificationRepository;
import com.hrms.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepo;

    @Autowired
    private NotificationPreferenceRepository prefRepo;

    @Autowired
    private UserRepository userRepo;

    private Employee getCurrentEmployee(Principal principal) {
        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        return user.getEmployee();
    }

    @GetMapping
    public Page<Notification> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        Employee emp = getCurrentEmployee(principal);
        return notificationRepo.findByRecipientEmployeeIdOrderByCreatedAtDesc(emp.getEmployeeId(), PageRequest.of(page, size));
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(Principal principal) {
        Employee emp = getCurrentEmployee(principal);
        long count = notificationRepo.countByRecipientEmployeeIdAndIsReadFalse(emp.getEmployeeId());
        return Map.of("count", count);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Integer id, Principal principal) {
        Employee emp = getCurrentEmployee(principal);
        Notification notif = notificationRepo.findById(id).orElseThrow();
        if (!notif.getRecipient().getEmployeeId().equals(emp.getEmployeeId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not your notification");
        }
        notif.setRead(true);
        return ResponseEntity.ok(notificationRepo.save(notif));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(Principal principal) {
        Employee emp = getCurrentEmployee(principal);
        List<Notification> unread = notificationRepo.findByRecipientEmployeeIdAndIsReadFalse(emp.getEmployeeId());
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepo.saveAll(unread);
        return ResponseEntity.ok(Map.of("message", "All marked as read"));
    }

    @GetMapping("/preferences")
    public NotificationPreference getPreferences(Principal principal) {
        Employee emp = getCurrentEmployee(principal);
        return prefRepo.findById(emp.getEmployeeId()).orElse(new NotificationPreference(emp));
    }

    @PutMapping("/preferences")
    public ResponseEntity<?> updatePreferences(@RequestBody NotificationPreference pref, Principal principal) {
        Employee emp = getCurrentEmployee(principal);
        NotificationPreference existing = prefRepo.findById(emp.getEmployeeId()).orElse(new NotificationPreference(emp));
        existing.setEmailEnabled(pref.isEmailEnabled());
        existing.setInAppEnabled(pref.isInAppEnabled());
        return ResponseEntity.ok(prefRepo.save(existing));
    }
}
