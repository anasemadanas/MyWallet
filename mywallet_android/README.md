# MyWallet Android

Native Android Java version of MyWallet with local SQLite storage.

## Features

- Login
- Create user
- Forgot password with recovery answer
- Multi-user budgets, transactions, and goals
- Local SQLite database on the device

## Build

```bat
build_debug.bat
```

The debug APK is created at:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Seed users:

```text
admin / 1234
user / user
zaid / zaid
hamza / 9999
```

For seeded users, the recovery answer is the username.
