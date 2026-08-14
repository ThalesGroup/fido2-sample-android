# FIDO2 Sample Application

This project demonstrates how to integrate the Thales **FIDO2 SDK (4.1.0)** on Android via a
small sample application. It shows the registration and authentication flows, PIN/biometric
authenticators, secure logging, TLS certificate pinning, and — new in 4.1.0 — **Android 14+
Credential Provider (passkey) support**.

> The SDK integration points inside `Register.kt` and `Authenticate.kt` are intentionally left as
> `## ... ##` placeholders. Follow the release documentation to fill them in — this is how the
> sample teaches the integration steps.

## Prerequisites

Please contact your Thales representative to obtain your JFrog Artifactory token and the values below.

1. **Download the SDK from Thales JFrog Artifactory.** The Mobile FIDO SDK and UI SDK are
   distributed as Maven artifacts from the public Artifactory repository — no local AARs required.

   Provide your Artifactory token via an environment variable:

   ```bash
   export JFROG_URL_MAVEN=https://thalescpliam.jfrog.io/artifactory/onegini-sdk
   export JFROG_TOKEN=YOUR_ARTIFACTORY_TOKEN
   ```

   or via `gradle.properties` (`JFROG_URL_MAVEN` is already set to the public path there; just add
   `JFROG_TOKEN=...` — do **not** commit a real token). The repository is wired up in the root
   `build.gradle`, and the dependencies are declared in `app/fido2sample/build.gradle`:

   ```gradle
   implementation "com.thalesgroup.gemalto.fido2:fido2:4.1.0@aar"
   implementation "com.thalesgroup.gemalto.fido2:fido2ui:4.1.0@aar"
   ```

2. **Fill in `app/fido2sample/src/main/kotlin/.../sample/Configuration.kt`:**
   - `publicKeyModulus` and `publicKeyExponent` — the public key used to initialise Secure Log.
   - `rpId` — your relying party ID.
   - Optionally `customBiometricAaguid` / `customPasscodeAaguid` if your deployment needs custom
     authenticator AAGUIDs (leave `null` for the SDK defaults).
3. **Add your TLS pinning certificates** in `app/fido2sample/src/main/res/raw/` and reference them
   in `AppUtils.getPinningCertificates()`.

## Build

```bash
./gradlew :app:fido2sample:assembleDebug
```
