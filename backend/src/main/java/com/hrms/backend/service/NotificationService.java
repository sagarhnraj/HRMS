package com.hrms.backend.service;

import com.hrms.backend.entity.EmailOutbox;
import com.hrms.backend.entity.Employee;
import com.hrms.backend.entity.Notification;
import com.hrms.backend.entity.NotificationPreference;
import com.hrms.backend.repository.EmailOutboxRepository;
import com.hrms.backend.repository.NotificationPreferenceRepository;
import com.hrms.backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    @Autowired
    private EmailOutboxRepository outboxRepository;

    @Autowired
    private com.hrms.backend.repository.UserRepository userRepository;

    @Transactional
    public void notify(Employee recipient, String type, String title, String body, String relatedEntityType, String relatedEntityId) {
        if (recipient == null) return;
        
        NotificationPreference pref = preferenceRepository.findById(recipient.getEmployeeId())
                .orElse(new NotificationPreference(recipient));
        
        if (pref.isInAppEnabled()) {
            Notification notif = new Notification(recipient, type, title, body, relatedEntityType, relatedEntityId);
            notificationRepository.save(notif);
        }

        if (pref.isEmailEnabled()) {
            String email = null;
            com.hrms.backend.entity.User user = userRepository.findByEmployee_EmployeeId(recipient.getEmployeeId()).orElse(null);
            if (user != null) {
                email = user.getEmail();
            } else {
                email = recipient.getEmployeeId() + "@test.com"; // dummy fallback
            }
            
            if (email != null && !email.isEmpty()) {
                EmailOutbox outbox = new EmailOutbox(email, title, body);
                outboxRepository.save(outbox);
            }
        }
    }
}
