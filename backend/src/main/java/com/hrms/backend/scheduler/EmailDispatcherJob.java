package com.hrms.backend.scheduler;

import com.hrms.backend.entity.EmailOutbox;
import com.hrms.backend.repository.EmailOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class EmailDispatcherJob {

    private static final Logger logger = LoggerFactory.getLogger(EmailDispatcherJob.class);

    @Autowired
    private EmailOutboxRepository outboxRepository;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    // Note: In a real deployment at scale, this DB polling mechanism should be replaced 
    // by a proper message queue like SQS, RabbitMQ, or Kafka.
    @Scheduled(fixedDelay = 30000)
    public void dispatchEmails() {
        List<EmailOutbox> pending = outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING");
        if (pending.isEmpty()) return;

        logger.info("Found {} pending emails to dispatch.", pending.size());

        for (EmailOutbox outbox : pending) {
            try {
                if (mailEnabled) {
                    // JavaMailSender logic would go here
                    logger.info("Real SMTP is enabled. Sending to {}...", outbox.getToAddress());
                } else {
                    // Dev mode: fake/log-only sender
                    logger.info("Would send email to: {} | Subject: {}", outbox.getToAddress(), outbox.getSubject());
                }
                outbox.setStatus("SENT");
                outbox.setSentAt(LocalDateTime.now());
            } catch (Exception e) {
                logger.error("Failed to send email id {}: {}", outbox.getId(), e.getMessage());
                outbox.setAttemptCount(outbox.getAttemptCount() + 1);
                outbox.setLastError(e.getMessage());
                if (outbox.getAttemptCount() >= 3) {
                    outbox.setStatus("FAILED");
                }
            }
            outboxRepository.save(outbox);
        }
    }
}
