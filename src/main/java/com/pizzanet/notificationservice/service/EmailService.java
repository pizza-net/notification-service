package com.pizzanet.notificationservice.service;

import com.pizzanet.notificationservice.dto.OrderStatusChangedEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Wysyła email z powiadomieniem o zmianie statusu zamówienia
     */
    public void sendOrderStatusNotification(OrderStatusChangedEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(event.getUserEmail());
            helper.setSubject("Pizza Net - Status zamówienia #" + event.getOrderId());
            
            String htmlContent = buildEmailContent(event);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email wysłany do {} dla zamówienia #{}", event.getUserEmail(), event.getOrderId());

        } catch (MessagingException e) {
            logger.error("Błąd podczas wysyłania emaila do {}: {}", event.getUserEmail(), e.getMessage(), e);
        }
    }

    /**
     * Buduje treść HTML emaila
     */
    private String buildEmailContent(OrderStatusChangedEvent event) {
        String statusMessage = getStatusMessage(event.getOrderStatus());
        String formattedDate = event.getTimestamp() != null 
            ? event.getTimestamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            : "N/A";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #e74c3c; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #777; }
                    .status { font-size: 18px; font-weight: bold; color: #e74c3c; }
                    .info { margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🍕 Pizza Net</h1>
                    </div>
                    <div class="content">
                        <h2>Status Twojego zamówienia został zaktualizowany!</h2>
                        
                        <div class="info">
                            <strong>Numer zamówienia:</strong> #%d
                        </div>
                        
                        <div class="info">
                            <strong>Nowy status:</strong> <span class="status">%s</span>
                        </div>
                        
                        <div class="info">
                            <strong>Data aktualizacji:</strong> %s
                        </div>
                        
                        <div class="info">
                            <strong>Wartość zamówienia:</strong> %.2f PLN
                        </div>
                        
                        <p style="margin-top: 20px;">%s</p>
                    </div>
                    <div class="footer">
                        <p>Dziękujemy za zamówienie w Pizza Net!</p>
                        <p>To jest automatyczna wiadomość, prosimy nie odpowiadać.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                event.getOrderId(),
                event.getOrderStatus(),
                formattedDate,
                event.getTotalPrice() != null ? event.getTotalPrice() : 0.0,
                statusMessage
            );
    }

    /**
     * Zwraca przyjazny opis statusu dla użytkownika
     */
    private String getStatusMessage(String status) {
        return switch (status) {
            case "PENDING" -> "Twoje zamówienie oczekuje na potwierdzenie.";
            case "CONFIRMED" -> "Twoje zamówienie zostało potwierdzone i jest w trakcie przygotowania.";
            case "PREPARING" -> "Twoja pizza jest właśnie przygotowywana przez naszych kucharzy!";
            case "READY" -> "Twoja pizza jest gotowa do odbioru lub dostawy!";
            case "DELIVERED" -> "Twoja pizza została dostarczona. Smacznego!";
            case "CANCELLED" -> "Twoje zamówienie zostało anulowane.";
            default -> "Status Twojego zamówienia został zaktualizowany.";
        };
    }
}
