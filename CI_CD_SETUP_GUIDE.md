# GeoWav CI/CD Pipeline Setup Guide

A complete step-by-step guide to setting up the CI/CD pipeline for the GeoWav Android app using **GitHub Actions**, **Fastlane**, and **Google Play Store** deployment.

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Prerequisites](#2-prerequisites)
3. [Step 1: Generate the Signing Keystore](#step-1-generate-the-signing-keystore)
4. [Step 2: Configure Signing in build.gradle.kts](#step-2-configure-signing-in-buildgradlekts)
5. [Step 3: Google Cloud Platform Setup](#step-3-google-cloud-platform-setup)
6. [Step 4: Google Play Console Setup](#step-4-google-play-console-setup)
7. [Step 5: Fastlane Setup](#step-5-fastlane-setup)
8. [Step 6: GitHub Secrets Configuration](#step-6-github-secrets-configuration)
9. [Step 7: GitHub Actions Workflows](#step-7-github-actions-workflows)
10. [Step 8: Testing the Pipeline](#step-8-testing-the-pipeline)
11. [Troubleshooting](#troubleshooting)

---

## 1. Architecture Overview

```
Push to main/test
       │
       ▼
┌──────────────────┐
│   CI Workflow     │  ← ci.yml
│  (Test + Lint)    │
└──────────────────┘
       │
       ▼
┌──────────────────┐
│  Release Workflow │  ← release.yml
│  (Build + Sign)   │
└──────┬───────────┘
       │
       ├──► GitHub Release (AAB artifact)
       │
       ▼
┌──────────────────┐
│    Fastlane       │
│  (Deploy to Play  │
│   Store track)    │
└──────────────────┘
       │
       ▼
  Google Play Store
  (internal/alpha/beta/production)
```

### Workflow Files

| File | Purpose | Trigger |
|------|---------|---------|
| `ci.yml` | Unit tests, lint, detekt | Push/PR to `main`, `test` |
| `test-build.yml` | Build AAB only (no deploy) | Push to `main`, `test` |
| `release.yml` | Build + Sign + GitHub Release + Play Store deploy | Push to `main`, `test` or manual |
| `cd.yml` | Build release APK + GitHub Release | Push to `main` or tags `v*` |

---

## 2. Prerequisites

- Android project with Gradle build system
- GitHub repository
- Google Play Developer account 
- Google Cloud Platform account (linked to the same Google account)
- App already uploaded to Google Play Console at least once manually

---

### Base64 Encode the Keystore

The keystore binary file needs to be stored as a GitHub Secret, so we encode it as base64:

**Linux/macOS:**
```bash
base64 -w 0 keystore/keystore > keystore_base64.txt
```

**Windows (PowerShell):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore\keystore")) | Out-File -Encoding ASCII -NoNewline "keystore_base64.txt"
```

The contents of `keystore_base64.txt` will be used as the `KEYSTORE_BASE64` GitHub Secret.

### Add Keystore to Git

The keystore file itself is committed to the repo under `keystore/keystore`, but the password is never committed — it's stored only in GitHub Secrets.

```
project-root/
└── keystore/
    └── keystore          ← PKCS12 keystore file (committed)
```

---

## Step 2: Configure Signing in build.gradle.kts

The signing config reads credentials from **environment variables** (set by GitHub Actions), not from files:

```kotlin
android {
    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH")
            if (keystorePath != null) {
                storeFile = rootProject.file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // ...
            val keystorePath = System.getenv("KEYSTORE_PATH")
            if (keystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}
```

This approach means:
- **Locally**: No `KEYSTORE_PATH` env var → signing config is skipped → debug signing used
- **CI**: Env vars are set → release signing config is applied

---

## Step 3: Google Cloud Platform Setup

Fastlane uses a **GCP Service Account** to authenticate with the Google Play Developer API.

### 3.1 — Enable the Google Play Android Developer API

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select or create a project (use the same project linked to your Play Console)
3. Navigate to **APIs & Services → Library**
4. Search for **"Google Play Android Developer API"**
5. Click **Enable**

> **⚠️ Important**: The GCP project must be the same one linked to your Google Play Developer account. If you're unsure, check Play Console → Settings → API access.

### 3.2 — Create a Service Account

1. In GCP Console, go to **IAM & Admin → Service Accounts**
2. Click **+ Create Service Account**
3. Fill in details:
   - **Name**: `play-store-publisher@geowav3003.iam.gserviceaccount.com` (or similar)
   - **Description**: "Service account for Fastlane CI/CD deployments"
4. Click **Create and Continue**
5. **Skip** the "Grant this service account access to project" step (permissions are managed in Play Console)
6. Click **Done**

### 3.3 — Create a JSON Key

1. Click on the newly created service account
2. Go to the **Keys** tab
3. Click **Add Key → Create new key**
4. Select **JSON** format
5. Click **Create** — a `.json` file will download automatically

**This JSON file is your `PLAY_JSON_KEY_DATA` secret.** It looks like:

```json
{
  "type": "service_account",
  "project_id": "your-project-id",
  "private_key_id": "abc123...",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
  "client_email": "fastlane-deploy@your-project.iam.gserviceaccount.com",
  "client_id": "123456789",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/..."
}
```

> **🔒 Security**: Never commit this file. Store the entire JSON content as a GitHub Secret.

---

## Step 4: Google Play Console Setup

### 4.1 — Link GCP Project

1. Go to [Google Play Console](https://play.google.com/console/)
2. Navigate to **Test and Release → App Integrity**
3. If not already linked, click **Link** next to your GCP project
4. Your GCP project should appear — confirm the link

### 4.2 — Grant Service Account Access

1. On the **Users and permissions** page, Click **Invite user** 
2. find your service account (`play-store-publisher@geowav3003.iam.gserviceaccount.com`)
3. Set permissions:
   - **App permissions**: Select your app (`com.ext.demo`)
   - **Account permissions**: At minimum, grant:
     - ✅ View app information and download bulk reports
     - ✅ Release to production, exclude devices, and use Play App Signing
     - ✅ Manage testing tracks and edit tester lists
     - ✅ Manage store presence
4.  → **Send invite**

> **⚠️ Note**: It can take up to **24 hours** for the API access to fully propagate. If Fastlane fails with a 403 error immediately after setup, wait and retry.

### 4.3 — First Manual Upload (Required)

Google Play requires at least **one manual AAB upload** before the API can be used:

1. Go to your app in Play Console
2. Navigate to **Internal testing** (or any track)
3. Click **Create new release**
4. Upload your signed `.aab` file manually
5. Complete the release

After this, Fastlane can deploy subsequent versions automatically.

---

## Step 5: Fastlane Setup

### 5.1 — Project Structure

```
project-root/
└── fastlane/
    ├── Appfile       ← Package name config
    └── Fastfile      ← Lane definitions
```

### 5.2 — Appfile

```ruby
package_name("com.aarav.geowav")
```

### 5.3 — Fastfile

The Fastfile defines deployment lanes for each Play Store track:

```ruby
default_platform(:android)

platform :android do
  desc "Deploy to Internal Testing"
  lane :deploy_internal do
    upload_to_play_store(
      aab: "app/build/outputs/bundle/release/app-release.aab",
      track: "internal",
      json_key_data: ENV["PLAY_JSON_KEY_DATA"],
      package_name: "com.aarav.geowav",
      skip_upload_metadata: true,
      skip_upload_images: true,
      skip_upload_screenshots: true,
      skip_upload_changelogs: true,
      release_status: "completed"
    )
  end

  # Similar lanes for: deploy_alpha, deploy_beta, deploy_production
  # Production lane supports rollout percentage:
  lane :deploy_production do
    rollout_percent = ENV["ROLLOUT_PERCENT"] ? ENV["ROLLOUT_PERCENT"].to_f : 1.0
    upload_to_play_store(
      aab: "app/build/outputs/bundle/release/app-release.aab",
      track: "production",
      json_key_data: ENV["PLAY_JSON_KEY_DATA"],
      package_name: "com.aarav.geowav",
      skip_upload_metadata: true,
      skip_upload_images: true,
      skip_upload_screenshots: true,
      skip_upload_changelogs: true,
      release_status: "completed",
      rollout: rollout_percent
    )
  end
end
```

Key points:
- `json_key_data: ENV["PLAY_JSON_KEY_DATA"]` reads the service account JSON from environment
- `skip_upload_*: true` skips metadata/screenshots (faster deploys)
- Production supports `rollout` percentage for staged rollouts

---

## Step 6: GitHub Secrets Configuration

Go to **GitHub → Repository → Settings → Secrets and variables → Actions** and add:

### Signing Secrets

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `KEYSTORE_BASE64` | Contents of `keystore_base64.txt` | Base64-encoded keystore file |
| `KEYSTORE_PASSWORD` | `aarav_keystore_3003` | Keystore store password |
| `KEY_ALIAS` | `key0` | Key alias inside the keystore |
| `KEY_PASSWORD` | `aarav_keystore_3003` | Key password (**must equal store password for PKCS12**) |

### Firebase / App Secrets

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `GOOGLE_SERVICES_JSON` | Full contents of `google-services.json` | Firebase configuration |
| `GOOGLE_MAPS_API_KEY` | Your Maps API key | Google Maps API key |
| `META_ACCESS_TOEKN` | Your Meta token | Meta/Facebook access token |

### Play Store Deployment

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `PLAY_JSON_KEY_DATA` | Full contents of the GCP service account JSON | Fastlane Play Store authentication |

> **⚠️ Tips for Secrets:**
> - No trailing newlines or spaces
> - For `KEYSTORE_BASE64`: must be a single line with no wrapping
> - For JSON secrets: paste the entire JSON file content as-is

---

## Step 7: GitHub Actions Workflows

### release.yml — Full Pipeline

This is the main workflow. It:
1. Checks out code
2. Decodes the keystore from base64
3. Verifies keystore file integrity (size + hash check)
4. Reads version from `build.gradle.kts`
5. Creates `local.properties` and `google-services.json` from secrets
6. Builds the signed AAB
7. Uploads AAB as GitHub artifact
8. Creates a GitHub Release with the AAB
9. Installs Fastlane and deploys to Play Store

**Triggers:**
- Automatic on push to `main` or `test` → deploys to **internal** track
- Manual via `workflow_dispatch` → you choose the track (internal/alpha/beta/production)

**To deploy to production manually:**
1. Go to **Actions** tab in GitHub
2. Select **"Android CI/CD - Play Store Release"**
3. Click **"Run workflow"**
4. Select `production` from the track dropdown
5. Click **"Run workflow"**

### Keystore Decode (Critical Step)

```yaml
- name: Decode Keystore
  env:
    KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
  run: |
    mkdir -p keystore
    printf '%s' "$KEYSTORE_BASE64" | base64 -d > keystore/keystore
```

> **⚠️ Always use `printf '%s'` instead of `echo`** to pipe base64 data. `echo` can interpret backslash sequences (`\n`, `\t`) within the base64 string, silently corrupting the decoded binary.

## Track Selection: Branch-Based Deployment

The pipeline automatically selects the Play Store track based on which branch triggered the build. Manual overrides are also supported.

### Branch → Track Mapping

| Branch | Track | Description |
|--------|-------|-------------|
| `main` | **production** | Live release to all users |
| `test` | **internal** | Internal testing track (invite-only) |
| Any other branch | **internal** | Defaults to internal for safety |

### How It Works

The deploy step in `release.yml` uses this logic:

```yaml
# 1. If manually triggered with a track selection → use that
# 2. Otherwise → determine track from the branch name

if [ -n "${{ github.event.inputs.track }}" ]; then
  TRACK="${{ github.event.inputs.track }}"
else
  BRANCH="${{ github.ref_name }}"
  case "$BRANCH" in
    main)       TRACK="production" ;;
    test)       TRACK="internal" ;;
    *)          TRACK="internal" ;;
  esac
fi
```

### Adding More Branches

To add more branches (e.g., `staging` → alpha, `beta` → beta), add them in two places:

**1. Workflow trigger (top of `release.yml`):**
```yaml
on:
  push:
    branches:
      - main
      - test
      - staging    # ← add new branch
      - beta       # ← add new branch
```

**2. Track mapping (deploy step):**
```yaml
case "$BRANCH" in
  main)       TRACK="production" ;;
  test)       TRACK="internal" ;;
  staging)    TRACK="alpha" ;;      # ← add mapping
  beta)       TRACK="beta" ;;       # ← add mapping
  *)          TRACK="internal" ;;
esac
```

### Available Tracks

| Track | Purpose | Visibility |
|-------|---------|-----------|
| `internal` | Internal testing | Only invited internal testers (up to 100) |
| `alpha` | Closed testing | Invited testers via email/group |
| `beta` | Open testing | Anyone can join via opt-in link |
| `production` | Production release | All users on Google Play |

### Manual Override

You can always override the branch-based default by triggering the workflow manually:

1. Go to **GitHub → Actions → "Android CI/CD - Play Store Release"**
2. Click **"Run workflow"**
3. Select the branch
4. Choose the desired track from the dropdown (internal / alpha / beta / production)
5. Click **"Run workflow"**

The manual selection **always takes priority** over the branch-based default.

---

## Step 8: Testing the Pipeline

### Run on CI

1. Push to `test` branch first (triggers release to internal only)
2. Check the workflow logs for:
   - ✅ Keystore integrity verification passes
   - ✅ AAB builds successfully
   - ✅ GitHub Release created
   - ✅ Fastlane deploys to internal track
3. Verify in Play Console that the new version appears in Internal Testing

---

## Troubleshooting

### `403 Forbidden` from Play Store API

**Cause**: Service account doesn't have permission, or permissions haven't propagated yet.

**Fix**:
1. Verify the service account has correct permissions in Play Console → Settings → API access
2. Wait up to 24 hours after granting access
3. Ensure the Google Play Android Developer API is enabled in GCP

### `No matching app found` / `Package not found`

**Cause**: First upload must be done manually.

**Fix**: Upload an AAB manually via Play Console before using Fastlane.

---

## File Reference

| File | Path | Purpose |
|------|------|---------|
| Keystore | `keystore/keystore` | PKCS12 signing keystore |
| CI workflow | `.github/workflows/ci.yml` | Tests + lint |
| Build workflow | `.github/workflows/test-build.yml` | Build-only (no deploy) |
| Release workflow | `.github/workflows/release.yml` | Full build + deploy pipeline |
| CD workflow | `.github/workflows/cd.yml` | APK build + GitHub Release |
| Fastfile | `fastlane/Fastfile` | Fastlane lane definitions |
| Appfile | `fastlane/Appfile` | Package name config |
| Signing config | `app/build.gradle.kts` | Environment-based signing |
