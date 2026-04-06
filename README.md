# Mutual Fund Future Value Calculator

A full-stack web application that helps users estimate potential returns on mutual fund investments using the **Capital Asset Pricing Model (CAPM)**.

## How It Works

Users select a mutual fund, enter an initial investment amount and time horizon, and the app calculates the predicted future value using:

**CAPM Formula:**
```
rate = riskFreeRate + beta * (expectedMarketReturn - riskFreeRate)
```

**Future Value:**
```
FV = principal * (1 + rate) ^ years
```

Where:
- **Risk-Free Rate** = US 10-Year Treasury yield (hardcoded at 4.25%)
- **Beta** = Fetched live from [Newton Analytics API](https://www.newtonanalytics.com/docs/api/stockbeta.php) — measures fund volatility vs. S&P 500
- **Expected Market Return** = S&P 500 historical average (~10%)

## Tech Stack

| Layer    | Technology                     |
|----------|--------------------------------|
| Backend  | Java 17, Spring Boot 3.5.11   |
| Frontend | Angular 21, TypeScript         |
| APIs     | Newton Analytics (beta data)   |

## Project Structure

```
├── backend/          # Spring Boot REST API
│   └── src/main/java/com/mutualfund/backend/
│       ├── controller/MutualFundController.java
│       ├── service/MutualFundService.java
│       ├── model/MutualFund.java
│       ├── model/InvestmentResult.java
│       └── config/CorsConfig.java
├── frontend/         # Angular 21 SPA
│   └── src/app/
│       ├── app.ts                    # Main component
│       ├── services/api.service.ts   # HTTP client
│       └── models/                   # TypeScript interfaces
└── README.md
```

## API Endpoints

| Method | Endpoint                  | Description                              |
|--------|---------------------------|------------------------------------------|
| GET    | `/api/mutual-funds`       | Returns list of 17 available mutual funds |
| GET    | `/api/future-value`       | Calculates future value using CAPM        |

**Future Value Parameters:**
- `ticker` (string) — Mutual fund ticker symbol (e.g., `VFIAX`)
- `principal` (number) — Initial investment amount in dollars
- `years` (integer) — Investment time horizon

**Example:**
```
GET /api/future-value?ticker=VFIAX&principal=10000&years=10
```

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+ and npm
- Angular CLI (`npm install -g @angular/cli`)

### Run the Backend
```bash
cd backend
./mvnw spring-boot:run
```
The API will be available at `http://localhost:8080`.

### Run the Frontend
```bash
cd frontend
npm install
ng serve
```
The app will be available at `http://localhost:4200`.

## Available Mutual Funds

The calculator includes 17 well-known mutual funds across categories:
- **Large Cap Blend:** Vanguard 500 Index (VFIAX), Fidelity 500 Index (FXAIX)
- **Large Cap Growth:** Vanguard Growth Index (VIGAX), Fidelity Contrafund (FCNTX), and more
- **Balanced:** Vanguard Wellington (VWELX), Vanguard Balanced Index (VBIAX)
- **International:** Vanguard Total International Stock Index (VTIAX)
- **Bond:** Vanguard Total Bond Market Index (VBTLX)
- And more...
