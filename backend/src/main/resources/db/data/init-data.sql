-- =============================================================
-- Skrypt inicjalizacyjny danych dla CRM Finanse
-- Uruchom recznie po pierwszym starcie aplikacji:
-- docker exec -i crm-postgres psql -U postgres -d crm_finanse < init-data.sql
-- =============================================================

-- Kategorie przychodow
INSERT INTO categories (name, type, color, icon) VALUES
('Wynagrodzenie', 'INCOME', '#4CAF50', 'wallet'),
('Premia', 'INCOME', '#8BC34A', 'star'),
('Zlecenia', 'INCOME', '#009688', 'briefcase'),
('Inne przychody', 'INCOME', '#607D8B', 'plus-circle');

-- Kategorie wydatkow
INSERT INTO categories (name, type, color, icon) VALUES
('Mieszkanie', 'EXPENSE', '#F44336', 'home'),
('Jedzenie', 'EXPENSE', '#FF9800', 'shopping-cart'),
('Transport', 'EXPENSE', '#2196F3', 'car'),
('Zdrowie', 'EXPENSE', '#E91E63', 'heart'),
('Rozrywka', 'EXPENSE', '#9C27B0', 'film'),
('Ubrania', 'EXPENSE', '#00BCD4', 'shopping-bag'),
('Edukacja', 'EXPENSE', '#3F51B5', 'book'),
('Rachunki', 'EXPENSE', '#795548', 'file-text'),
('Oszczednosci', 'EXPENSE', '#4CAF50', 'piggy-bank'),
('Subskrypcje', 'EXPENSE', '#FF5722', 'credit-card'),
('Inne wydatki', 'EXPENSE', '#9E9E9E', 'more-horizontal');

-- =============================================================
-- INFO: Szablony wydatkow nalezy skonfigurowac recznie
-- przez interfejs aplikacji, zgodnie z rzeczywistymi potrzebami
-- =============================================================

-- =============================================================
-- INFO: Zrodla przychodow nalezy skonfigurowac recznie
-- przez interfejs aplikacji, poniewaz wymagaja konfiguracji
-- B2B lub UoP specyficznej dla uzytkownika
-- =============================================================

SELECT 'Dane poczatkowe zostaly zaladowane pomyslnie!' as status;
