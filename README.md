# Notification Service

Serwis odpowiedzialny za wysyłanie powiadomień email o zmianach statusu zamówień w systemie Pizza Net.

## Technologie
- Spring Boot 3.3.6
- Spring Cloud 2023.0.1
- RabbitMQ (AMQP)
- Spring Mail (SMTP)
- Netflix Eureka Client
- Java 21

## Funkcjonalności
- Nasłuchiwanie eventów z RabbitMQ (order.exchange → order.status.queue)
- Wysyłanie emaili z powiadomieniami o statusie zamówienia
- Wsparcie dla różnych statusów zamówienia (PENDING, CONFIRMED, PREPARING, READY, DELIVERED, CANCELLED)
- Profesjonalne HTML email templates
- Rejestracja w Eureka Service Discovery

## Architektura

```
order-service → RabbitMQ (order.exchange) → notification-service → SMTP → Email
```

Gdy status zamówienia się zmienia:
1. `order-service` publikuje event do RabbitMQ
2. `notification-service` nasłuchuje na kolejce `order.status.queue`
3. Event zawiera: orderId, userId, userEmail, orderStatus, totalPrice, timestamp
4. Email jest wysyłany na adres klienta z informacją o zmianie statusu

## Konfiguracja Email

### Opcja 1: Użyj zmiennych środowiskowych (.env)

1. Skopiuj plik `.env.example` do `.env` w głównym katalogu projektu:
   ```bash
   cp .env.example .env
   ```

2. Stwórz dedykowane konto Gmail dla projektu (np. `pizzanet.notifications@gmail.com`)

3. Wygeneruj hasło aplikacji Gmail:
   - Idź na: https://myaccount.google.com/apppasswords
   - Zaloguj się na konto Gmail projektu
   - Włącz weryfikację dwuetapową (jeśli nie jest włączona)
   - Wygeneruj hasło aplikacji dla "Mail" / "Pizza Net"
   - Skopiuj 16-znakowe hasło (usuń spacje)

4. Edytuj plik `.env`:
   ```
   MAIL_USERNAME=pizzanet.notifications@gmail.com
   MAIL_PASSWORD=abcdefghijklmnop
   ```

5. Zrestartuj serwis:
   ```bash
   docker-compose restart notification-service
   ```

### Opcja 2: Tryb testowy (bez prawdziwych emaili)

Jeśli nie skonfigurujesz SMTP, serwis będzie działał normalnie, ale emaile nie będą wysyłane. 
Zamiast tego zobaczysz błędy w logach z pełną treścią emaila, który byłby wysłany.

**Sprawdź logi:**
```bash
docker logs notification-service --tail 50
```

## Testowanie

### 1. Sprawdź status serwisu
```bash
docker ps | grep notification
docker logs notification-service --tail 20
```

### 2. Sprawdź RabbitMQ Management UI
- URL: http://localhost:15672
- Login: admin / admin
- Sprawdź queue: `order.status.queue`
- Sprawdź exchange: `order.exchange`

### 3. Przetestuj wysyłkę emaila

```bash
# Zaloguj się jako admin
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' -s | jq -r '.token')

# Zmień status zamówienia
curl -X PATCH "http://localhost:8080/api/orders/1/status?status=PREPARING" \
  -H "Authorization: Bearer $TOKEN"

# Sprawdź logi notification-service
docker logs notification-service --tail 30 | grep -i "email\|event"
```

### 4. Treść emaila

Email zawiera:
- **Temat:** Pizza Net - Status zamówienia #{orderId}
- **Od:** Skonfigurowany MAIL_USERNAME
- **Do:** Email klienta z bazy danych
- **Treść HTML:**
  - Logo Pizza Net 🍕
  - Numer zamówienia
  - Nowy status (PENDING, CONFIRMED, PREPARING, READY, DELIVERED, CANCELLED)
  - Data aktualizacji
  - Wartość zamówienia
  - Przyjazny opis statusu w języku polskim

## Troubleshooting

### Problem: "Authentication failed"
**Rozwiązanie:** Upewnij się, że używasz hasła aplikacji (App Password), a nie zwykłego hasła Gmail.

### Problem: "Mail server connection failed"
**Rozwiązanie:** Sprawdź połączenie z internetem lub użyj innego SMTP (np. smtp.gmail.com:587).

### Problem: "To address must not be null"
**Rozwiązanie:** Użytkownicy w bazie danych muszą mieć ustawiony email. Sprawdź:
```sql
SELECT id, username, email FROM users;
```

### Problem: Brak eventów w RabbitMQ
**Rozwiązanie:** Sprawdź czy `order-service` jest podłączony do RabbitMQ:
```bash
docker logs order-service --tail 50 | grep -i rabbitmq
```

## Zmienne środowiskowe

| Zmienna | Opis | Domyślna wartość |
|---------|------|------------------|
| `SPRING_RABBITMQ_HOST` | Adres serwera RabbitMQ | `rabbitmq` |
| `SPRING_RABBITMQ_PORT` | Port RabbitMQ | `5672` |
| `SPRING_RABBITMQ_USERNAME` | Login do RabbitMQ | `admin` |
| `SPRING_RABBITMQ_PASSWORD` | Hasło do RabbitMQ | `admin` |
| `MAIL_USERNAME` | Email wysyłający | `your-email@gmail.com` |
| `MAIL_PASSWORD` | Hasło aplikacji Gmail | `your-app-password` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | URL Eureka Server | `http://discovery-server:8761/eureka/` |

## Port
- **8086** - HTTP (nie wystawiony na zewnątrz, dostępny tylko w sieci Docker)
# notification-service
