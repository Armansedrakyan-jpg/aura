# API Contract — AuraClone (Android Cheat Sheet)

Краткая шпаргалка по всем REST-эндпоинтам бэкенда (`Spring Boot`).

## Общие правила

- **Авторизация:** заголовок `Authorization: Bearer <token>` для всех защищённых эндпоинтов.
- **Формат дат (`LocalDateTime`):** ISO-8601 строка без часового пояса (например, `"2026-11-17T14:30:00.123456"`).
- **Формат ошибок (`4xx` / `5xx`):**
  ```json
  {
    "status": 400,
    "message": "Описание ошибки"
  }
  ```
  - `401 Unauthorized`: `{"status":401,"message":"Требуется авторизация"}`
  - `429 Too Many Requests` (rate limit): `{"status":429,"message":"Слишком много запросов, попробуй через минуту"}`
  - `429 Too Many Requests` (кулдаун анализа при `credits == 0`): кроме `status` и `message` содержит `remainingSeconds`, `nextFreeAt`, `credits`.
- **Rate Limits (в минуту):** `POST /api/auth/*` — 20, `POST /api/aura/analyze` — 10, `/api/battles/*` — 30, `POST /api/shop/*` — 30.

---

## Сводная таблица эндпоинтов

| Метод | URL | Bearer токен | Контроллер |
|---|---|---|---|
| `GET` | `/api/health` | Нет | `HealthController` |
| `POST` | `/api/auth/register` | Нет | `AuthController` |
| `POST` | `/api/auth/login` | Нет | `AuthController` |
| `GET` | `/api/auth/me` | **Да** | `AuthController` |
| `DELETE` | `/api/auth/account` | **Да** | `AuthController` |
| `POST` | `/api/aura/analyze` | **Да** | `AuraAnalyzeController` |
| `GET` | `/api/aura/cooldown` | **Да** | `AuraAnalyzeController` |
| `GET` | `/api/aura/history` | **Да** | `AuraAnalyzeController` |
| `GET` | `/api/aura/{id}` | **Да** | `AuraAnalyzeController` |
| `GET` | `/api/aura/leaderboard` | Нет | `AuraAnalyzeController` |
| `POST` | `/api/battles/start` | **Да** | `BattleController` |
| `PUT` | `/api/battles/join/{battleId}` | **Да** | `BattleController` |
| `GET` | `/api/battles/history` | **Да** | `BattleController` |
| `GET` | `/api/shop/items` | **Да** | `ShopController` |
| `POST` | `/api/shop/buy/{itemId}` | **Да** | `ShopController` |
| `POST` | `/api/shop/equip/{itemId}` | **Да** | `ShopController` |
| `POST` | `/api/shop/unequip/{type}` | **Да** | `ShopController` |
| `POST` | `/api/shop/verify-purchase` | **Да** | `ShopController` |
| `GET` | `/api/users/{id}` | Нет | `UserController` |
| `PUT` | `/api/users/me/frame` | **Да** | `UserController` |

---

## 1. Health (`HealthController`)

### `GET /api/health`
- **Authorization Bearer:** Нет
- **Request:** нет
- **Response (`200 OK`):**
  ```json
  {
    "status": "UP",
    "timestamp": "2026-11-17T12:00:00.000Z"
  }
  ```

---

## 2. Auth (`AuthController`)

### `POST /api/auth/register`
- **Authorization Bearer:** Нет
- **Request (`RegisterRequest`, `application/json`):**
  ```json
  {
    "username": "string (3..30, ^[a-zA-Z0-9._-]+$, required)",
    "email": "string (email, max 120, required)",
    "password": "string (6..100, required)"
  }
  ```
- **Response (`200 OK`, `AuthResponse`):**
  ```json
  {
    "token": "string (JWT)",
    "userId": 1,
    "username": "string"
  }
  ```

### `POST /api/auth/login`
- **Authorization Bearer:** Нет
- **Request (`LoginRequest`, `application/json`):**
  ```json
  {
    "usernameOrEmail": "string (required)",
    "password": "string (required)"
  }
  ```
- **Response (`200 OK`, `AuthResponse`):**
  ```json
  {
    "token": "string (JWT)",
    "userId": 1,
    "username": "string"
  }
  ```

### `GET /api/auth/me`
- **Authorization Bearer:** **Да**
- **Request:** нет
- **Response (`200 OK`, `UserProfileResponse`):**
  ```json
  {
    "userId": 1,
    "username": "string",
    "email": "string"
  }
  ```

### `DELETE /api/auth/account`
- **Authorization Bearer:** **Да**
- **Request:** нет
- **Response (`200 OK`):**
  ```json
  {
    "status": "deleted",
    "message": "Аккаунт и связанные данные удалены"
  }
  ```

---

## 3. Aura (`AuraAnalyzeController`)

### `POST /api/aura/analyze`
- **Authorization Bearer:** **Да**
- **Request (`multipart/form-data`, лимит файла/запроса: 10 MB):**
  - `photo` (`MultipartFile`, обязательное): изображение `image/jpeg`, `image/png` или `image/webp`.
  - `aboutMe` (`String`, опционально): текст о себе.
- **Response (`200 OK`, `AuraAnalysis`):**
  ```json
  {
    "id": 42,
    "userId": 1,
    "score": 87,
    "level": "HIGH",
    "vibe": "string",
    "energy": "string",
    "confidence": "string",
    "personality": "string",
    "verdict": "string",
    "createdAt": "2026-11-17T12:00:00"
  }
  ```
- **Ошибка кулдауна (`429 Too Many Requests`):**
  ```json
  {
    "status": 429,
    "message": "Следующая бесплатная генерация будет доступна через ...",
    "remainingSeconds": 43199,
    "nextFreeAt": "2026-11-18T00:00:00",
    "credits": 0
  }
  ```

### `GET /api/aura/cooldown`
- **Authorization Bearer:** **Да**
- **Request:** нет
- **Response (`200 OK`, `CooldownStatusResponse`):**
  ```json
  {
    "canGenerateFree": false,
    "remainingSeconds": 1800,
    "nextFreeAt": "2026-11-17T12:30:00",
    "credits": 50
  }
  ```
  *(Когда `canGenerateFree == true`: `remainingSeconds` = `0`, `nextFreeAt` = `null`.)*

### `GET /api/aura/history`
- **Authorization Bearer:** **Да**
- **Request:** нет
- **Response (`200 OK`, `List<AuraAnalysis>`):**
  ```json
  [
    {
      "id": 42,
      "userId": 1,
      "score": 87,
      "level": "HIGH",
      "vibe": "string",
      "energy": "string",
      "confidence": "string",
      "personality": "string",
      "verdict": "string",
      "createdAt": "2026-11-17T12:00:00"
    }
  ]
  ```

### `GET /api/aura/{id}`
- **Authorization Bearer:** **Да** *(возвращает только свой анализ, иначе `404`)*
- **Path Variable:** `id` (`Long`)
- **Request:** нет
- **Response (`200 OK`, `AuraAnalysis`):**
  ```json
  {
    "id": 42,
    "userId": 1,
    "score": 87,
    "level": "HIGH",
    "vibe": "string",
    "energy": "string",
    "confidence": "string",
    "personality": "string",
    "verdict": "string",
    "createdAt": "2026-11-17T12:00:00"
  }
  ```

### `GET /api/aura/leaderboard`
- **Authorization Bearer:** Нет
- **Request:** нет *(возвращает до 100 лучших записей)*
- **Response (`200 OK`, `List<LeaderboardEntryResponse>`):**
  ```json
  [
    {
      "userId": 1,
      "username": "string",
      "avatarFrame": "DEFAULT",
      "avatarEffect": "AURA_SPARKS",
      "equippedFrameId": 1,
      "equippedEffectId": 6,
      "score": 95,
      "level": "LEGENDARY",
      "verdict": "string",
      "createdAt": "2026-11-17T12:00:00"
    }
  ]
  ```

---

## 4. Battles (`BattleController`)

> **Важно про `Player`:** если у пользователя уже есть хотя бы один `AuraAnalysis`, сервер игнорирует переданные `userId`, `username`, `score`, `title` и подставляет их из БД. Поля из тела запроса используются как fallback только если анализов ещё нет (иначе без тела вернётся `400`).

### `POST /api/battles/start`
- **Authorization Bearer:** **Да**
- **Request (`Player`, `application/json`, опционально):**
  ```json
  {
    "userId": 1,
    "username": "string",
    "score": 80,
    "title": "HIGH"
  }
  ```
- **Response (`200 OK`):**
  ```json
  {
    "battleId": "123456"
  }
  ```
  *(6-значный PIN-код комнаты Firebase)*

### `PUT /api/battles/join/{battleId}`
- **Authorization Bearer:** **Да**
- **Path Variable:** `battleId` (`String`, строго 6 цифр `^\d{6}$`)
- **Request (`Player`, `application/json`, опционально):**
  ```json
  {
    "userId": 2,
    "username": "string",
    "score": 75,
    "title": "MID"
  }
  ```
- **Response (`200 OK`, `Battle`):**
  ```json
  {
    "status": "FINISHED",
    "createdAt": 1763380800000,
    "player1": {
      "userId": 1,
      "username": "playerOne",
      "score": 87,
      "title": "HIGH"
    },
    "player2": {
      "userId": 2,
      "username": "playerTwo",
      "score": 75,
      "title": "MID"
    },
    "winnerId": 1
  }
  ```
  *(При ничьей `winnerId` = `0`.)*

### `GET /api/battles/history`
- **Authorization Bearer:** **Да**
- **Request:** нет *(до 50 последних дуэлей пользователя)*
- **Response (`200 OK`, `List<BattleHistory>`):**
  ```json
  [
    {
      "id": 10,
      "player1Id": 1,
      "player1Name": "playerOne",
      "player1Score": 87,
      "player2Id": 2,
      "player2Name": "playerTwo",
      "player2Score": 75,
      "winnerId": 1,
      "createdAt": "2026-11-17T12:05:00"
    }
  ]
  ```


---

## 5. Shop (`ShopController`)

**Enums:**
- `ShopItemType`: `"FRAME"` | `"EFFECT"` | `"CREDIT_PACK"`
- `ItemRarity`: `"COMMON"` | `"RARE"` | `"EPIC"` | `"LEGENDARY"`

### `GET /api/shop/items`
- **Authorization Bearer:** **Да**
- **Query Param (опционально):** `type` (`FRAME` | `EFFECT` | `CREDIT_PACK`)
- **Request:** нет
- **Response (`200 OK`, `ShopCatalogResponse`):**
  ```json
  {
    "credits": 150,
    "equippedFrameId": 1,
    "avatarFrame": "NEON",
    "equippedEffectId": 6,
    "avatarEffect": "AURA_SPARKS",
    "items": [
      {
        "id": 1,
        "code": "NEON",
        "type": "FRAME",
        "name": "Неоновый импульс",
        "description": "Яркая кибер-рамка с голубым и пурпурным свечением",
        "rarity": "RARE",
        "priceCredits": 50,
        "googlePlayProductId": null,
        "creditsAmount": null,
        "owned": true,
        "equipped": true
      }
    ]
  }
  ```
  *(Для `CREDIT_PACK`: `priceCredits` = `null`, заполнены `googlePlayProductId` и `creditsAmount`, `owned` и `equipped` всегда `false`.)*

### `POST /api/shop/buy/{itemId}`
- **Authorization Bearer:** **Да**
- **Path Variable:** `itemId` (`Long`) — только для `FRAME` и `EFFECT`
- **Request:** нет
- **Response (`200 OK`, `BuyItemResponse`):**
  ```json
  {
    "itemId": 1,
    "code": "NEON",
    "type": "FRAME",
    "credits": 100,
    "owned": true
  }
  ```
  *(`credits` — новый остаток баланса после списания.)*

### `POST /api/shop/equip/{itemId}`
- **Authorization Bearer:** **Да**
- **Path Variable:** `itemId` (`Long`) — только купленные `FRAME` или `EFFECT`
- **Request:** нет
- **Response (`200 OK`, `EquipItemResponse`):**
  ```json
  {
    "equippedFrameId": 1,
    "avatarFrame": "NEON",
    "equippedEffectId": 6,
    "avatarEffect": "AURA_SPARKS"
  }
  ```

### `POST /api/shop/unequip/{type}`
- **Authorization Bearer:** **Да**
- **Path Variable:** `type` — только `FRAME` или `EFFECT` (`CREDIT_PACK` вернёт `400`)
- **Request:** нет
- **Response (`200 OK`, `EquipItemResponse`):**
  ```json
  {
    "equippedFrameId": null,
    "avatarFrame": "DEFAULT",
    "equippedEffectId": 6,
    "avatarEffect": "AURA_SPARKS"
  }
  ```
  *(При снятии `FRAME`: `equippedFrameId = null`, `avatarFrame = "DEFAULT"`. При снятии `EFFECT`: `equippedEffectId = null`, `avatarEffect = null`.)*

### `POST /api/shop/verify-purchase`
- **Authorization Bearer:** **Да**
- **Request (`VerifyPurchaseRequest`, `application/json`):**
  ```json
  {
    "productId": "string (not blank, max 128, напр. credits_pack_50)",
    "purchaseToken": "string (not blank, max 512)"
  }
  ```
- **Response (`200 OK`, `VerifyPurchaseResponse`):**
  ```json
  {
    "transactionId": 12,
    "productId": "credits_pack_50",
    "creditsAdded": 50,
    "totalCredits": 200,
    "status": "VERIFIED"
  }
  ```

---

## 6. Users (`UserController`)

### `GET /api/users/{id}`
- **Authorization Bearer:** Нет
- **Path Variable:** `id` (`Long`)
- **Request:** нет
- **Response (`200 OK`, `PublicUserProfileResponse`):**
  ```json
  {
    "userId": 1,
    "username": "string",
    "createdAt": "2026-11-17T10:00:00",
    "avatarFrame": "DEFAULT",
    "avatarEffect": null,
    "equippedFrameId": null,
    "equippedEffectId": null,
    "bestScore": 95,
    "bestLevel": "LEGENDARY",
    "averageScore": 84.5,
    "totalAnalyses": 4,
    "totalBattles": 3,
    "winsCount": 2,
    "achievements": [
      {
        "code": "string",
        "title": "string",
        "description": "string",
        "unlockedAt": "2026-11-17T12:00:00"
      }
    ]
  }
  ```
  *(Если анализов ещё не было: `bestScore = 0`, `bestLevel = null`, `averageScore = 0.0`.)*

### `PUT /api/users/me/frame`
- **Authorization Bearer:** **Да**
- **Request (`UpdateFrameRequest`, `application/json`):**
  ```json
  {
    "avatarFrame": "NEON"
  }
  ```
  *(Допустимые значения, case-insensitive: `"DEFAULT"`, `"NEON"`, `"GOLD"`, `"CYBER"`, `"SIGMA"`, `"LEGENDARY"`, max 50 символов.)*
- **Response (`200 OK`):**
  ```json
  {
    "avatarFrame": "NEON"
  }
  ```

