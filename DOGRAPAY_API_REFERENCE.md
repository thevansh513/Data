# Dograapy Payment Gateway — API Reference

**Base URL (Production):** `https://dograpay.online`  
**Base URL (Local):** `http://localhost:3000`  
**Content-Type:** `application/json`

---

## 1. Register Application

**`POST /api/application`**

Register your application and receive a permanent API key. Store the API key securely — it is only shown once.

### Request Body

```json
{
  "platformName": "My Application",
  "ownerName": "John Doe",
  "ownerEmail": "john@example.com"
}
```

| Field          | Type   | Required | Description                        |
|---------------|--------|----------|------------------------------------|
| `platformName` | string | ✅        | Unique application name (2-100 chars) |
| `ownerName`    | string | ❌        | Owner's full name                  |
| `ownerEmail`   | string | ❌        | Owner's email address              |

### Success Response — `201 Created`

```json
{
  "success": true,
  "message": "Application registered successfully. Store your API key safely — it will not be shown again.",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "platformName": "My Application",
    "apiKey": "dgpy_a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2",
    "status": "ACTIVE",
    "createdAt": "2025-01-15T10:30:00.000Z"
  }
}
```

### Error Responses

| Status | Error              | Cause                             |
|--------|--------------------|-----------------------------------|
| 400    | `BAD_REQUEST`      | Validation failed                 |
| 409    | `CONFLICT`         | Platform name already registered  |
| 429    | `TOO_MANY_REQUESTS`| Rate limit exceeded (5/min per IP)|
| 500    | `INTERNAL_SERVER_ERROR` | Server error               |

---

## 2. Create Payment

**`POST /api/payment/create`**

Verify your API key, create a Razorpay order, and save the payment as PENDING. Pass the returned `orderId` to Razorpay Checkout SDK in your Android app.

### Request Body

```json
{
  "apiKey": "dgpy_a1b2c3d4e5f6...",
  "platformName": "My Application",
  "transactionId": "TRX123456789",
  "userId": "1001",
  "amount": 500
}
```

| Field           | Type   | Required | Description                           |
|----------------|--------|----------|---------------------------------------|
| `apiKey`        | string | ✅        | Your application's API key            |
| `platformName`  | string | ✅        | Must match your registered platform   |
| `transactionId` | string | ✅        | Your unique transaction ID (alphanumeric, 5-100 chars) |
| `userId`        | string | ✅        | Your app's user identifier            |
| `amount`        | number | ✅        | Amount in INR (e.g. `500` = ₹500)    |
| `currency`      | string | ❌        | Default: `"INR"`                      |

### Success Response — `200 OK`

```json
{
  "success": true,
  "orderId": "order_PZS9FJhEZnJ4xN",
  "amount": 50000,
  "currency": "INR",
  "transactionId": "TRX123456789"
}
```

> **Note:** `amount` in the response is in **paise** (multiply by 100). Pass it directly to the Razorpay Checkout SDK.

### Error Responses

| Status | Error              | Cause                                  |
|--------|--------------------|----------------------------------------|
| 400    | `BAD_REQUEST`      | Validation failed                      |
| 401    | `UNAUTHORIZED`     | Invalid API key or platform name       |
| 409    | `CONFLICT`         | Duplicate `transactionId`              |
| 429    | `TOO_MANY_REQUESTS`| Rate limit exceeded (20/min per IP)    |
| 500    | `INTERNAL_SERVER_ERROR` | Razorpay error or server error    |

---

## 3. Razorpay Webhook

**`POST /api/webhook`**

Razorpay calls this endpoint automatically after a payment event. **Do not call this from your app.** Configure this URL in your Razorpay Dashboard.

### Webhook URL

```
https://dograpay.online/api/webhook
```

### Headers (sent by Razorpay)

| Header                    | Description                        |
|---------------------------|------------------------------------|
| `x-razorpay-signature`    | HMAC-SHA256 signature of the body  |

### Handled Events

| Event               | Result in Firestore                |
|--------------------|-------------------------------------|
| `payment.captured`  | `paymentStatus = "SUCCESS"`        |
| `order.paid`        | `paymentStatus = "SUCCESS"`        |
| `payment.failed`    | `paymentStatus = "FAILED"`         |

### Response — `200 OK`

```json
{
  "success": true,
  "message": "Payment success.",
  "orderId": "order_PZS9FJhEZnJ4xN"
}
```

> Always returns `200` to prevent Razorpay from retrying. Failures are logged server-side.

---

## 4. Verify Payment

**`POST /api/payment/verify`**

Check the payment status for a transaction. If `SUCCESS` and not yet credited, the API atomically sets `credited = true` and returns the amount — credit the user's wallet **only** when this API returns `"success": true`.

### Request Body

```json
{
  "apiKey": "dgpy_a1b2c3d4e5f6...",
  "platformName": "My Application",
  "transactionId": "TRX123456789"
}
```

| Field           | Type   | Required | Description                        |
|----------------|--------|----------|------------------------------------|
| `apiKey`        | string | ✅        | Your application's API key         |
| `platformName`  | string | ✅        | Your registered platform name      |
| `transactionId` | string | ✅        | The transaction ID to verify       |

### Response: SUCCESS (first verify)

```json
{
  "success": true,
  "status": "SUCCESS",
  "amount": 500,
  "transactionId": "TRX123456789",
  "message": "Payment verified. Wallet credited.",
  "alreadyCredited": false
}
```

### Response: SUCCESS (already credited)

```json
{
  "success": true,
  "status": "SUCCESS",
  "amount": 500,
  "transactionId": "TRX123456789",
  "message": "Payment already credited.",
  "alreadyCredited": true
}
```

> **Important:** Only credit the user's wallet when `alreadyCredited === false`. If `alreadyCredited === true`, the wallet was already credited in a previous call.

### Response: PENDING

```json
{
  "success": false,
  "status": "PENDING",
  "message": "Payment is still pending."
}
```

### Response: NOT FOUND

```json
{
  "success": false,
  "status": "NOT_FOUND",
  "message": "No payment found for this transaction ID."
}
```

### Response: FAILED / CANCELLED

```json
{
  "success": false,
  "status": "FAILED",
  "message": "Payment failed."
}
```

### Error Responses

| Status | Error              | Cause                               |
|--------|--------------------|-------------------------------------|
| 400    | `BAD_REQUEST`      | Validation failed                   |
| 401    | `UNAUTHORIZED`     | Invalid API key or platform name    |
| 429    | `TOO_MANY_REQUESTS`| Rate limit exceeded (30/min per IP) |
| 500    | `INTERNAL_SERVER_ERROR` | Server error                   |

---

## Rate Limits

| Endpoint              | Limit         |
|----------------------|---------------|
| `POST /api/application` | 5 req/min per IP  |
| `POST /api/payment/create` | 20 req/min per IP |
| `POST /api/payment/verify` | 30 req/min per IP |

---

## Error Response Format

All errors follow this format:

```json
{
  "success": false,
  "error": "ERROR_CODE",
  "message": "Human-readable description"
}
```
