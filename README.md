# Spring-Boot-Banking-Anwendung mit automatisierter Testausführung

Dieses Projekt wurde entwickelt, um Ausschnitte aus der Anwendung, den Tests sowie dem CI-Workflow 
als unterstützende Beispiele für meine Bachelorarbeit zum Thema "Implementierung und Evaluierung 
automatisierter Tests einer mit Spring Boot erstellten REST-API in GitHub" zu nutzen.

## Lokale Einrichtung

### Voraussetzungen

Installation von Java 21, Docker, Git 

### Klonen des Repositorys

  ```bash
  git clone https://github.com/psophis/REST-API-with-automated-testing.git
  cd banking-backend
```

### Konfiguration der Umgebungsvariablen

Anlegen einer `.env` Datei im Projektverzeichnis:

POSTGRES_DB=banking
POSTGRES_USER=banking
POSTGRES_PASSWORD=lokales-passwort

### PostgreSQL starten

Starten der lokalen PostgreSQL-Datenbank mit Docker:

```bash
docker run --name banking-postgres \
  -e POSTGRES_DB=banking \
  -e POSTGRES_USER=banking \
  -e POSTGRES_PASSWORD=lokales-passwort \
  -p 5432:5432 \
  -v banking-postgres-data:/var/lib/postgresql/data \
  -d postgres:16
```

Erneutes Starten des Containers, wenn er bereits existiert:

```bash
docker start banking-postgres
```

### PostgreSQL beenden

```bash
docker stop banking-postgres
```

### Starten der Anwendung

Laden der Umgebungsvariablen und Starten der Anwendung:

```bash
set -a && source .env && set +a && ./gradlew :app:bootRun
```

Die Anwendung ist unter http://localhost:8080 lokal zu erreichen.

### Code formatieren und prüfen

Ktlint-Prüfung ausführen:

```bash
./gradlew ktlintCheck
```

Ktlint-Formatierung ausführen:

```bash
./gradlew ktlintFormat
```

Ktlint-Formatierung eines bestimmten Moduls ausführen (bspw. `Client`):

```bash
./gradlew :modules:client:ktlintFormat
```

### Tests ausführen

**Das Projekt verwendet folgende Testarten:**

- Unit-Tests
- Integrationstests
- E2E-API-Tests mit REST Assured
- Contract-Tests mit Spring Cloud Contract

Unit-, Integrations- und E2E-Tests ausführen:

```bash
./gradlew test
```

Tests eines bestimmten Moduls ausführen (bspw. `Client`):

```bash
./gradlew :modules:client:test
```

Alle Prüfungen einschließlich aller Tests ausführen:

```bash
./gradlew check
```

Contract-Tests generieren:

```bash
./gradlew generateContractTests
```

Contract-Tests eines bestimmten Moduls generieren (bspw. `Client`):

```bash
./gradlew :modules:client:generateContractTests
```

Contract-Tests ausführen:

```bash
./gradlew contractTest
```

Contract-Tests eines bestimmten Moduls ausführen (bspw. `Client`):

```bash
./gradlew :modules:client:contractTest
```

## Struktur der Anwendung

- `app/` – Einstiegspunkt und Start der Anwendung
- `modules/client/` – Client-Modul für die Verwaltung von Kund:innen
- `modules/payment/` – Payment-Modul für Überweisungen, Ein- und Auszahlungen und das Erzeugen von Transaktionen
- `modules/bank-account/` – Bank-Account-Modul für die Verwaltung der Bankkonten der Kund:innen
- `src/test/` – Unit-, Integrations- und E2E-API-Tests
- `src/contractTest/` – Contract-Tests

## Technische Struktur

Die Anwendung verwendet:

- Kotlin
- Spring Boot
- Gradle
- PostgreSQL
- Docker

Für die Umsetzung der Tests und CI:

- JUnit 5 – Ausführung und Strukturierung der Tests
- Spring Boot Test – Tests der Spring-Anwendung
- MockK – Mocking in Kotlin
- REST Assured – Testen von HTTP- und REST-Endpunkten
- Spring Cloud Contract – Contract Tests
- Testcontainers – Tests mit temporären PostgreSQL-Containern
- H2 – In-Memory-Datenbank für Tests
- JaCoCo – Ermittlung der Testabdeckung