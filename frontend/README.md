# Mutual Fund Future Value Calculator

A full-stack web app that estimates the future value of a mutual fund investment using real financial data.

## 🚀 Overview

This project consists of:

- **Frontend:** Angular app (user interface)
- **Backend #1:** Provides list of mutual funds
- **Backend #2:** Calculates future value using CAPM + compounding

## 🧠 How it works

1. User selects a mutual fund
2. Inputs:
  - Initial investment
  - Investment horizon (years)
3. App calls backend API
4. Backend:
  - Pulls market data
  - Computes expected return using CAPM:
    ```
    r = riskFreeRate + beta * (expectedReturn - riskFreeRate)
    ```
  - Calculates future value:
    ```
    FV = P * e^(r * t)
    ```
5. Returns predicted value + breakdown

---

## 🖥️ Tech Stack

- Angular (Frontend)
- Spring Boot (Backend APIs)
- REST APIs
- TypeScript

---

## 📡 API Endpoints

### Get Mutual Funds

GET /api/mutual-funds

### Calculate Future Value

GET /api/future-value?ticker=VFIAX&principal=1000&years=10

---

## ⚙️ Running the Project

### Frontend

Runs on:

http://localhost:4200

---

### Backend APIs

#### 1. Mutual Funds API (Fetch Funds)
Provides the list of available mutual funds.

GET http://localhost:8080/api/mutualfunds

---
#### 2. Future Value API (Calculator Engine)
Calculates the predicted future value of an investment.

GET https://localhost:8081/api/future-value

---

### Current Status

- Mutual fund list API is fully integrated into the frontend
- Future value calculation API is working independently but is still **not fully integrated into the frontend yet**

---

- Frontend runs on port **4200**
- Backend APIs run on **localhost (Spring Boot default: 8080)**
- Both backend services must be running for full functionality


### 1. Start Backend(s)
Run Spring Boot apps in IntelliJ

---

### 2. Start Frontend
```bash
npm install
ng serve
```
### Open:

http://localhost:4200


---

### ⚠️ Known Issues
	•	Initial mutual fund fetch may be delayed (under investigation)
	•	Requires both backend services running

---

### Author

*Balty Urrutia*

*March 22nd, 2026*
