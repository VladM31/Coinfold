# Coinfold: how to delete your data

*Last updated: 21 October 2026 · [Українська версія](DATA_DELETION.uk.md)*

This page explains how to delete data related to the **Coinfold** app (developer: VladM31). Coinfold has no accounts and no servers of its own, so there are two places where data can exist: your device and Google Firebase.

## 1. Data stored on your device (you control it)

Accounts, transactions, categories, recurring payments, settings and the PIN stay only on your phone. We cannot see them. To delete them:

1. In the app you can delete any account, category, transaction or recurring payment one by one.
2. To delete **everything at once**, open the Android settings → **Apps** → **Coinfold** → **Storage** → **Clear data** (or simply uninstall the app). On iPhone, delete the app.
3. If you made a backup file (Settings → Backup), delete that file yourself from the place where you saved it (your phone, a cloud drive, e-mail).

This deletes all financial data, the PIN and the settings immediately.

## 2. Data processed by Firebase (analytics and crash reports)

After you accept the privacy policy, the app sends anonymous usage events, crash reports and a random installation identifier to Google Firebase. This data does not contain your name, e-mail, amounts, notes or any financial content.

To request deletion of it:

1. Send an e-mail to **javacamp2021@gmail.com** with the subject **"Coinfold data deletion"**.
2. In the message, write the approximate dates when you used the app and your phone model. Because the identifier is random, this helps us find your installation. If you can, attach the installation ID (we can explain how to get it).
3. We will confirm by e-mail and delete the matching data within **30 days**.

You can also stop all new data collection at any time by uninstalling the app.

### What is deleted and what is kept

| Data | Action on request | Retention if you do not ask |
|---|---|---|
| Usage events (for example "expense added"), screen views | Deleted | Up to 14 months (Firebase Analytics) |
| Random installation identifier | Deleted | Up to 14 months |
| Crash reports (stack trace, app version, device model, OS version) | Deleted | 90 days (Firebase Crashlytics) |
| Approximate location (country/region from the IP address) | Deleted with the events | Up to 14 months |

We keep nothing else: there is no account, no profile and no copy of your financial data.

## 3. Questions

E-mail: **javacamp2021@gmail.com** · Privacy policy: [PRIVACY.md](PRIVACY.md) · Source code: https://github.com/VladM31/Coinfold
