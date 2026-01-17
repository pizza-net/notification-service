package com.pizzanet.notificationservice.listener;

import com.pizzanet.notificationservice.config.RabbitMQConfig;
import com.pizzanet.notificationservice.dto.OrderStatusChangedEvent;
import com.pizzanet.notificationservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);

    private final EmailService emailService;

    public OrderEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Nasłuchuje na zdarzenia zmiany statusu zamówienia z RabbitMQ
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_STATUS_QUEUE)
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        logger.info("Otrzymano event zmiany statusu zamówienia: {}", event);

        try {
            // Wysyłamy email z powiadomieniem
            emailService.sendOrderStatusNotification(event);
            logger.info("Powiadomienie email wysłane dla zamówienia #{}", event.getOrderId());
            
        } catch (Exception e) {
            logger.error("Błąd podczas przetwarzania eventu dla zamówienia #{}: {}", 
                        event.getOrderId(), e.getMessage(), e);
            // W produkcji: można dodać retry logic lub dead letter queue
        }
    }
}
