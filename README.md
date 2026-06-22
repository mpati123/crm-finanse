# CRM Finanse

Aplikacja do zarządzania finansami rodzinnymi. Pozwala na kompleksowe śledzenie przychodów, wydatków, budżetów oraz planowanie zakupów.

## Technologie

### Backend
- **Java 17** + **Spring Boot 3.2.5**
- **PostgreSQL 16** (produkcja) / H2 (development)
- **Liquibase** - migracje bazy danych
- **Lombok** - redukcja boilerplate
- **Apache POI** - eksport do Excel

### Frontend
- **Angular 18** z TypeScript 5.5
- **PWA** (Progressive Web App) z Service Worker
- **Chart.js** - wykresy i wizualizacje
- **ngx-translate** - wielojęzyczność (PL, EN, DE, FR, ES, IT i inne)

### Deployment
- **Docker Compose** - konteneryzacja
- **nginx** - serwowanie frontendu

## Funkcjonalności

### Przychody (Incomes)
- Rejestracja przychodów z różnych źródeł
- Obsługa różnych typów umów: B2B, UoP, Umowa zlecenie, Umowa o dzieło
- Automatyczne obliczanie godzin pracy dla B2B
- Kalkulacja podatków i składek ZUS

### Wydatki (Expenses)
- Kategoryzacja wydatków
- Statusy: PENDING, CONFIRMED
- Szablony wydatków cyklicznych
- Automatyczne generowanie wydatków z szablonów

### Budżety (Budgets)
- Planowanie budżetu miesięcznego/rocznego
- Śledzenie realizacji budżetu

### Źródła przychodów (Income Sources)
- Definiowanie źródeł z konfiguracją podatkową
- Obsługa stawek VAT (23%, 8%, 5%, 0%, ZW)
- Konfiguracja formy opodatkowania

### Kategorie (Categories)
- Kategorie dla przychodów i wydatków
- Hierarchiczna organizacja

### Osoby podatkowe (Tax Persons)
- Rozróżnienie osób fizycznych i firm
- Roczne śledzenie przychodów i progów podatkowych

### Zaplanowane zakupy (Planned Purchases)
- Planowanie przyszłych zakupów
- Priorytety: niski, średni, wysoki, pilny
- Statusy: zaplanowany, kupiony, anulowany, przełożony
- Automatyczne tworzenie wydatku przy dodaniu zakupu

### Cele oszczędnościowe (Savings Goals)
- Definiowanie celów z kwotą docelową
- Pasek postępu realizacji
- Status: aktywny, osiągnięty, wstrzymany

## Uruchomienie

### Wymagania
- Java 17+
- Node.js 18+
- Docker i Docker Compose (dla pełnego deploymentu)

### Development

**Backend:**
```bash
cd backend
mvn spring-boot:run
```
Backend uruchomi się na `http://localhost:8081`

**Frontend:**
```bash
cd frontend
npm install
npm start
```
Frontend uruchomi się na `http://localhost:4200`

### Docker (produkcja)

```bash
docker-compose up -d
```

Usługi:
- **Frontend**: http://localhost:8082
- **Backend**: http://localhost:8081
- **PostgreSQL**: localhost:5432

## Struktura projektu

```
crm-finanse/
├── backend/                    # Spring Boot API
│   ├── src/main/java/
│   │   └── pl/nehrebeccy/crmfinanse/
│   │       ├── controller/     # REST kontrolery
│   │       ├── service/        # Logika biznesowa
│   │       ├── repository/     # Repozytoria JPA
│   │       ├── model/          # Encje
│   │       └── dto/            # Data Transfer Objects
│   └── src/main/resources/
│       └── db/changelog/       # Migracje Liquibase
├── frontend/                   # Angular PWA
│   ├── src/app/
│   │   ├── components/         # Komponenty UI
│   │   ├── services/           # Serwisy Angular
│   │   └── models/             # Interfejsy TypeScript
│   └── src/assets/
│       └── i18n/               # Pliki tłumaczeń
├── android/                    # Aplikacja Android (Kotlin)
├── ios/                        # Aplikacja iOS (Swift)
└── docker-compose.yml          # Konfiguracja Docker
```

## API

Backend udostępnia REST API:

| Endpoint | Opis |
|----------|------|
| `/api/incomes` | Przychody |
| `/api/expenses` | Wydatki |
| `/api/budgets` | Budżety |
| `/api/income-sources` | Źródła przychodów |
| `/api/expense-templates` | Szablony wydatków |
| `/api/categories` | Kategorie |
| `/api/tax-persons` | Osoby podatkowe |
| `/api/planned-purchases` | Zaplanowane zakupy |
| `/api/savings-goals` | Cele oszczędnościowe |

## Konfiguracja

### Backend (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/crm_finanse
    username: crm_finanse
    password: your_password
```

### Zmienne środowiskowe (Docker)

| Zmienna | Opis |
|---------|------|
| `SPRING_DATASOURCE_URL` | URL bazy danych |
| `SPRING_DATASOURCE_USERNAME` | Użytkownik DB |
| `SPRING_DATASOURCE_PASSWORD` | Hasło DB |
| `SPRING_PROFILES_ACTIVE` | Profil Spring (docker) |

## Licencja

Projekt prywatny.
