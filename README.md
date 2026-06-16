# Vinty - Android

A rewards and engagement app where users earn tickets by watching ads, completing missions, and participating in games. Tickets can be redeemed for gift cards and prizes.

**Package:** `com.idealink.vinty`
**Min SDK:** 24 (Android 7.0) | **Target SDK:** 36 | **Kotlin:** 2.0.21

---

## Architecture

**MVVM + Repository Pattern**

```
com.idealink.vinty/
├── data/
│   ├── api/              # Retrofit service, interceptors, token management
│   ├── model/            # Data classes for API requests/responses
│   ├── repository/       # VintyRepository interface & implementation
│   └── DataManager.kt    # Thread-safe SharedPreferences singleton
├── ui/
│   ├── activities/       # SplashActivity, MainActivity, AccountActivity
│   ├── fragments/        # Feature-based fragments (auth, home, profile, etc.)
│   ├── viewmodel/        # MVVM ViewModels
│   ├── adapters/         # RecyclerView adapters
│   └── views/            # Custom views (ProfileHeaderView)
└── utils/                # Managers and helpers
```

### Key Patterns
- **StateFlow/Flow** for reactive UI updates
- **Singleton** pattern for managers (DataManager, AdManager, AnalyticsManager)
- **Interceptor** pattern for auth token injection and automatic refresh
- **ViewModelFactory** for dependency injection

---

## Activities & Navigation

| Activity | Purpose |
|----------|---------|
| `SplashActivity` | Entry point, routes based on auth state |
| `AccountActivity` | Auth flows (login, register, OTP, password reset) |
| `MainActivity` | Main app with bottom navigation (Home, Missions, Gift Store, Profile, Ad Watch) |

Navigation uses **Jetpack Navigation Component** with two nav graphs:
- `nav_graph.xml` — Main app navigation
- `nav_graph_account.xml` — Authentication flows

---

## Features

### Authentication
- Email/Password registration & login
- Google Sign-In (via Credentials API)
- OTP-based email verification
- Forgot password flow (email → OTP → new password)
- Auto token refresh on 401 (via `TokenAuthenticator`)

### Home
- Jackpot spin game (slot machine with weighted probabilities)
- Badge progress tracking
- Quick navigation to missions, leaderboard, badges

### Missions
- Mission list with claim functionality
- Ticket rewards for completed missions

### Gift Store (3 tabs)
- **Shop** — Browse and claim gift card packages
- **Available** — View claimed gift cards, redeem with codes
- **History** — Redemption history

### Badges
- Badge gallery with progress tracking
- Badge reward claiming
- Ad watching badge progress

### Leaderboard
- Top 50 player rankings by tickets earned
- Period filtering (all-time)

### Profile & Settings
- Avatar upload (camera/gallery)
- Invite friend system (codes & links)
- User history (Ads, Lotteries, Rewards tabs)
- Change password, delete account, logout
- Notification toggle (register/unregister device with backend)
- Privacy policy, terms, FAQs, support

---

## API Integration

**Base URL:** `https://be.coraffle.com/api/`
**HTTP Client:** OkHttp 4.12.0 + Retrofit 3.0.0

### Auth Mechanism
- `AuthInterceptor` adds `Authorization: Bearer {token}` to all requests
- `TokenAuthenticator` auto-refreshes expired tokens via `auth/refresh`
- Max 2 retry attempts before session clear

### Endpoints (38 active)

| Category | Endpoints |
|----------|-----------|
| Auth | register, login, google, verify-email-otp, resend-verification-email, forgot-password, forgot-password/verify-otp, change-password, refresh, logout |
| User | profile, avatar, ad-view, gain-xp, ad-history, lottery-history, reward-history, history, fcm-token, account (delete) |
| Notifications | register-device, unregister-device, {id}/clicked, {id}/read |
| Missions | list, claim, badge-progress |
| Leaderboard | list (period, limit) |
| Badges | gallery, stats, {id}/claim |
| Invites | my-code, apply-code |
| Games | spin-status, spin-result |
| Gift Cards | packages (shop), claimed, history, {id}/redeem, {id}/claim |

---

## Analytics & Tracking

### Firebase Analytics (`AnalyticsManager`)
7 custom events tracked across the app:

| Event | Trigger |
|-------|---------|
| `login` | Successful login (email + Google) |
| `register` | Successful registration |
| `ad_watch_clicked` | User initiates ad watch |
| `ad_reward_earned` | Ad watch completed |
| `earn_ticket` | Tickets awarded |
| `mission_progress_up` | Mission claimed |
| `redeem_attempt` | Gift card redemption initiated |

### Firebase Crashlytics (`CrashlyticsManager`)
- Non-fatal error recording with custom keys
- User ID tracking
- Custom log messages

### Facebook Analytics (`FacebookAnalyticsManager`)
- Same 7 events mirrored to Facebook App Events
- Facebook App ID: `884329484553894`
- Auto app event logging enabled

---

## Push Notifications (FCM)

**Service:** `VintyFirebaseMessagingService`
**Channel:** `vinty_notifications` (HIGH importance)

### Notification Types & Deep Links

| Type | Navigates To |
|------|-------------|
| `lottery_win`, `lottery_draw`, `lottery_reminder` | Home |
| `mission_complete`, `new_mission` | Missions |
| `gift_delivery`, `gift_redemption` | Gift Store |
| `system_announcement`, `user_engagement` | Home |

### Flow
1. FCM message received → system notification displayed with `PendingIntent`
2. User taps notification → `MainActivity` handles intent
3. Navigates to correct fragment based on `notification_type` extra
4. Calls `notifications/{id}/clicked` API to track engagement

---

## Ads Integration

**SDK:** Unity Ads 4.13.0
**Game ID:** `4853271`
**Ad Unit:** `Rewarded_Android`
**Test Mode:** Enabled

`AdManager` handles the full lifecycle: init → load → show → reward callback.
Rewards tracked via `UserSharedViewModel.adReward` StateFlow.

---

## Dependencies

| Category | Library | Version |
|----------|---------|---------|
| **Networking** | Retrofit | 3.0.0 |
| | OkHttp | 4.12.0 |
| | Gson Converter | 3.0.0 |
| **Firebase** | BOM | 33.7.0 |
| | Analytics, Crashlytics, Messaging | via BOM |
| **Facebook** | Android SDK | 17.0.2 |
| **Ads** | Unity Ads | 4.13.0 |
| **UI** | Material Design | 1.10.0 |
| | Lottie | 6.7.1 |
| | Glide | 4.16.0 |
| | DotsIndicator | 5.0 |
| **Media** | ExoPlayer (Media3) | 1.3.1 |
| **Auth** | Google Identity | 1.2.0 |
| | Credentials API | 1.5.0 |
| **Coroutines** | kotlinx-coroutines | 1.7.1 |
| **Navigation** | Navigation Component | 2.9.5 |
| **Lifecycle** | ViewModel, LiveData | 2.10.0 |

---

## Build Configuration

### Build Types
- **Debug** — Same signing as release (for testing)
- **Release** — ProGuard enabled, custom keystore from `keystore.properties`

### Build Config Fields
```
API_URL = "https://be.coraffle.com/api/"
BASE_IMAGE_URL = "https://admin.coraffle.com"
WEB_CLIENT_ID = "1065134009929-..." (Google Sign-In)
```

### Build Features
- View Binding: enabled
- BuildConfig: enabled
- Java 11 target

---

## Permissions

| Permission | Purpose |
|------------|---------|
| `INTERNET` | API calls |
| `ACCESS_NETWORK_STATE` | Network availability checks |
| `CAMERA` | Avatar photo capture |
| `POST_NOTIFICATIONS` | Push notifications (Android 13+) |

---

## Setup & Build

1. Clone the repository
2. Add `google-services.json` to `app/` (Firebase config)
3. Add `keystore.properties` to project root (signing config):
   ```properties
   storeFile=path/to/keystore.jks
   storePassword=...
   keyAlias=...
   keyPassword=...
   ```
4. Build:
   ```bash
   ./gradlew assembleDebug
   ```

---

## ViewModels

| ViewModel | Scope |
|-----------|-------|
| `AuthViewModel` | Login, register, OTP, password reset |
| `HomeViewModel` | Home screen state, badge progress, spin status |
| `UserSharedViewModel` | Shared user profile, history, ad rewards (activity-scoped) |
| `MissionViewModel` | Mission list and claiming |
| `BadgeViewModel` | Badge gallery and stats |
| `FragmentGiftStoreViewModel` | Gift store tabs (shop, available, redeemed) |
| `LeaderboardViewModel` | Player rankings |
| `AdHistoryViewModel` | Ad, lottery, reward history |
| `JackpotViewModel` | Spin game results |
