package com.restaurant.api.service.email;

/**
 * Strategy interface for sending transactional email.
 * Implementations decide the delivery channel (SMTP, SendGrid, console-log, etc.).
 */
public interface EmailSender {
    void sendEmail(String toEmail, String subject, String body);
}
