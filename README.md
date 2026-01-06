# biwooda-server
This repository contains the server-side code for the Biwooda web service. It provides user authentication, payment functionality, and umbrella rental management.

## 📸 screent shot
![Image](https://github.com/user-attachments/assets/8357b951-4082-4960-b88f-56406bc1d64e)

## 📦 skills
- **Frontend**: React
- **Backend**: Springboot, Firebase Realtimebase, Firebase Store
- **DevOps**: AWS EC2

## 🛠️ 📊 <strong>Rental & Return Process
  flowchart TD
  
    A[User: Scan main QR code to access website] --> B[Scan QR code on selected umbrella slot]
    B --> C[Proceed with payment]
    C --> D[Payment complete → Locker unlocks]
    D --> E[Umbrella taken → Confirmation sent to server]
    E --> F[Database updated]

    G[User: Click 'Return' button] --> H[Return umbrella to locker]
    H --> I[Return verified via Bluetooth connection]
    I --> J[Return status sent to server]
    J --> K[Extra fee (if any) automatically charged]
    K --> L[Database updated]

## 📡 API Overview

### 🔗 API Base URL

> Production
`https://sub.biwooda.site`

> Development  
`http://localhost:4000`


### 🧾 Request Headers

| Header           | Value                      | Description                     |
|------------------|----------------------------|---------------------------------|
| `Content-Type`   | `application/json`         | All requests use JSON format    |
| `Authorization`  | `Bearer {idToken}`         | Required for authenticated APIs |

✅ Most endpoints require a valid `id_token` obtained after login.

### 🔐 Authentication

| Method | Endpoint               | Description           |
|--------|------------------------|-----------------------|
| POST   | /auth/login            | Login with ID/PW      |
| GET    | /auth/kakao/oauth      | Login with KakaoOauth |
| GET    | /auth/naver/oauth      | Login with NaverOauth |
| POST   | /auth/reset-password   | Reset User's PW       |
| POST   | /auth/register         | SignIn with Email     |
| POST   | /auth/verify-email     | For verifying email   |

### ☂️ Umbrella Rental & Return

| Method | Endpoint               | Description              |
|--------|------------------------|--------------------------|
| POST   | /payment/ready         | Send payment details     |
| POST   | /payment/borrow-cancel | Refund payment           |
| POST   | /payment/return        | Return umbrella          |
