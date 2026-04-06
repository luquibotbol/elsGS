# GS Mutual Fund & ETF Calculator

A full-stack investment calculator that predicts future returns on mutual funds and ETFs using the **Capital Asset Pricing Model (CAPM)**, powered by **AI insights via GPT-4o**.

Built for the **Goldman Sachs ELS Hackathon 2025**.

---

## Getting Started (Teammate Setup)

### Prerequisites

Install the following before anything else:

| Tool | Version | Download |
|------|---------|----------|
| **Java 17 JDK** | 17+ | [Adoptium](https://adoptium.net/temurin/releases/?version=17) |
| **Node.js** | 18+ | [nodejs.org](https://nodejs.org/) |
| **Angular CLI** | latest | Run `npm install -g @angular/cli` after Node |
| **Git** | any | [git-scm.com](https://git-scm.com/) |

### 1. Clone the repo

```bash
git clone git@github.com:luquibotbol/elsGS.git
cd elsGS
```

### 2. Set up the OpenAI API key

The AI features (Portfolio Optimizer, Advisor Chat, Risk Analysis, Fund Compare) require an OpenAI API key. **This is not committed to the repo for security.**

```bash
# Copy the example env file
cp backend/.env.example backend/.env

# Edit it and paste your real key
nano backend/.env
```

Your `backend/.env` should look like:
```
OPENAI_API_KEY=sk-proj-your-actual-key-here
```

You can get an API key from [platform.openai.com/api-keys](https://platform.openai.com/api-keys).

> **Important:** Never commit `backend/.env` — it's already in `.gitignore`.

### 3. Run the Backend

```bash
cd backend

# Export the API key to your shell (required for Spring Boot to read it)
export OPENAI_API_KEY=$(grep OPENAI_API_KEY .env | cut -d '=' -f2)

# Start the server (macOS may block ./mvnw, use bash instead)
bash mvnw spring-boot:run
```

The backend will start on **http://localhost:8080**. You'll see it preloading beta values and expected returns for all 30 funds/ETFs — this takes ~30 seconds on first startup, then all calculations are instant.

### 4. Run the Frontend

Open a **second terminal**:

```bash
cd frontend
npm install        # first time only
ng serve
```

The app will be available at **http://localhost:4200**.

### 5. Verify it works

1. Open http://localhost:4200
2. Select a fund from the dropdown (you should see both MFs and ETFs)
3. Enter an investment amount and years
4. Click **Calculate Future Value**
5. Try the **AI-Powered Insights** panel below the results

---

## How It Works

Users select a mutual fund or ETF, input an investment amount and time horizon, and the app calculates the predicted future value using CAPM:

**CAPM Formula:**
```
rate = riskFreeRate + beta * (expectedReturn - riskFreeRate)
```

**Future Value:**
```
FV = principal * (1 + rate) ^ years
```

Where:
- **Risk-Free Rate** = US 10-Year Treasury yield (hardcoded at 4.25%)
- **Beta** = Fetched live from [Newton Analytics API](https://www.newtonanalytics.com/docs/api/stockbeta.php) — measures volatility vs. S&P 500
- **Expected Return** = Per-fund 5-year annualized return from Yahoo Finance (not a generic average)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.5.11 |
| Frontend | Angular 21, TypeScript |
| 3D Animation | Three.js (particle system) |
| AI | OpenAI GPT-4o |
| Data APIs | Newton Analytics (beta), Yahoo Finance (historical returns) |

---

## Project Structure

```
elsGS/
├── backend/                          # Spring Boot REST API
│   ├── .env.example                  # Template for API keys (copy to .env)
│   ├── pom.xml
│   └── src/main/java/com/mutualfund/backend/
│       ├── controller/
│       │   ├── MutualFundController.java   # /api/mutual-funds, /api/future-value
│       │   └── AiController.java           # /api/ai/* endpoints
│       ├── service/
│       │   ├── MutualFundService.java      # CAPM calculations, Newton + Yahoo APIs
│       │   └── AiService.java              # OpenAI GPT-4o integration
│       ├── model/                          # Data models
│       └── config/CorsConfig.java          # CORS for Angular dev server
├── frontend/                         # Angular 21 SPA
│   └── src/
│       ├── app/
│       │   ├── app.ts                      # Main component + Three.js animation
│       │   ├── app.html                    # UI template
│       │   ├── app.css                     # GS-branded dark theme
│       │   ├── services/api.service.ts     # HTTP client for all APIs
│       │   └── models/                     # TypeScript interfaces
│       ├── styles.css                      # Global design tokens
│       └── index.html                      # Three.js CDN + IBM Plex Sans
└── README.md
```

---

## API Endpoints

### Calculator
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/mutual-funds` | Returns list of 30 mutual funds + ETFs |
| GET | `/api/future-value?ticker=X&principal=Y&years=Z` | Calculates future value using CAPM |

### AI-Powered (require OpenAI key)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/ai/portfolio` | AI-optimized portfolio allocation |
| POST | `/api/ai/chat` | AI investment advisor conversation |
| POST | `/api/ai/analyze` | AI risk analysis of a calculation |
| POST | `/api/ai/compare` | AI side-by-side fund comparison |

---

## Available Investments

### Mutual Funds (17)
| Ticker | Name | Category |
|--------|------|----------|
| VFIAX | Vanguard 500 Index | Large Cap Blend |
| FXAIX | Fidelity 500 Index | Large Cap Blend |
| VTIAX | Vanguard Total International Stock Index | International Equity |
| VBTLX | Vanguard Total Bond Market Index | Intermediate Bond |
| VIGAX | Vanguard Growth Index | Large Cap Growth |
| FCNTX | Fidelity Contrafund | Large Cap Growth |
| VWELX | Vanguard Wellington | Balanced |
| VBIAX | Vanguard Balanced Index | Balanced |
| FSKAX | Fidelity Total Market Index | U.S. Total Market |
| TRBCX | T. Rowe Price Blue Chip Growth | Large Cap Growth |
| VIMAX | Vanguard Mid-Cap Index | Mid Cap Blend |
| DODGX | Dodge & Cox Stock | Large Cap Value |
| FDGRX | Fidelity Growth Company | Large Cap Growth |
| PRGFX | T. Rowe Price Growth Stock | Large Cap Growth |
| VVIAX | Vanguard Value Index | Large Cap Value |
| AGTHX | American Funds Growth Fund of America | Large Cap Growth |
| FBGRX | Fidelity Blue Chip Growth | Large Cap Growth |

### ETFs (13)
| Ticker | Name | Category |
|--------|------|----------|
| SPY | SPDR S&P 500 ETF Trust | Large Cap Blend |
| QQQ | Invesco QQQ Trust (Nasdaq 100) | Large Cap Growth |
| VTI | Vanguard Total Stock Market ETF | U.S. Total Market |
| VOO | Vanguard S&P 500 ETF | Large Cap Blend |
| IVV | iShares Core S&P 500 ETF | Large Cap Blend |
| BND | Vanguard Total Bond Market ETF | Intermediate Bond |
| VEA | Vanguard FTSE Developed Markets ETF | International Equity |
| EEM | iShares MSCI Emerging Markets ETF | Emerging Markets |
| ARKK | ARK Innovation ETF | Thematic Growth |
| VUG | Vanguard Growth ETF | Large Cap Growth |
| VTV | Vanguard Value ETF | Large Cap Value |
| IWM | iShares Russell 2000 ETF | Small Cap Blend |
| GLD | SPDR Gold Shares | Commodities |

---

## Features

### Core Calculator
- CAPM-based future value prediction with real market data
- Live beta from Newton Analytics API
- Per-fund 5-year annualized returns from Yahoo Finance
- Results show all CAPM parameters + live formula breakdown

### AI-Powered Insights (GPT-4o)
- **Portfolio Optimizer** — Enter amount + risk tolerance, get optimal allocation
- **AI Advisor Chat** — Ask anything about investing, funds, CAPM
- **Risk Analysis** — AI breakdown of your calculation's risk profile
- **Fund Compare** — Select 2-4 funds for AI side-by-side comparison

### UI/UX
- Goldman Sachs-branded dark theme (navy blue + black)
- Three.js "GS" particle animation (particles form the letters on load)
- Glassmorphism card design with IBM Plex Sans typography
- "Invest Now" deep-links to Robinhood, Fidelity, Schwab, Vanguard
- Responsive design (mobile + desktop)

---

## Troubleshooting

**`zsh: operation not permitted: ./mvnw`**
macOS blocks downloaded scripts. Use `bash mvnw spring-boot:run` instead.

**`Could not resolve placeholder 'OPENAI_API_KEY'`**
You forgot to export the key. Run: `export OPENAI_API_KEY=your-key-here` before starting the backend.

**`command not found: ng`**
Install Angular CLI: `npm install -g @angular/cli`

**Backend starts but calculations are slow**
First calculation for each fund fetches data from external APIs. Wait ~30 seconds after startup for preloading to finish — after that everything is cached and instant.

**AI features return errors**
Check that your OpenAI API key is valid and has credits at [platform.openai.com](https://platform.openai.com).
