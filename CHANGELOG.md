# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://commonmark.org/help/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0-alpha] - 2026-07-26

### Added
- Implemented `stats-engine` module (`StatsManager`) providing lock-free asynchronous notification intercept collection, top blocked apps ranking, and daily intercept trends.
- Added system-level Notification Channel service disabling (`setNotificationChannelEnabled`) to `NotificationServiceAccessor` and `SystemStateSynchronizer`.
- Updated WebUI with interactive Statistics Dashboard (total intercepts, top blocked apps, daily trends).
- Updated AGENTS.md, README.md, and TASKS.md with Epic K (intercept stats), system channel service shutdown, and production-grade engineering guarantees.
- Integrated release ZIP packaging workflow (`./gradlew zipModule`).
- Modified `settings.gradle.kts` to conditionally include `:testing-framework` only when the physical directory exists.
- Established root-level `module/` directory containing complete Magisk template files (module.prop, post-fs-data.sh, uninstall.sh, system/.gitkeep, and updater scripts).
- Modified `release-engineering/build.gradle.kts` to correctly compile the Magisk module package from the new `module/` source directory.
- Created automated GitHub Actions CI pipeline (`.github/workflows/ci.yml`) with automated builds, tests, ZIP generation, and content validations.
- Extended `hook-engine` with `enqueueNotificationWithTag` method-level hook (`T-HOOK-04`) to intercept and evaluate notification content and channels under Fail-open guarantees.
- Implemented hook engine self-check and automatic disable on self-check failure (`T-HOOK-06`).
- Expanded unit test coverage in `NotificationHookManagerTest` for the new hooks, self-check, and disable mechanisms.
- Implemented concrete, production-grade on-device Android system implementations for core platform components: `AndroidSystemHookBridge` (native JNI hook bridge loader), `AndroidRuntimeDataAccessor` (PackageManager & NMS reflection scanner), and `AndroidNotificationServiceAccessor` (NMS direct controller for package states and channel closures).
- Added test coverage in `NotificationServiceAccessorTest`, `RuntimeDataAccessorTest`, and `SystemHookBridgeTest` verifying fallback behaviors under dummy environments.
- Updated `release-engineering/build.gradle.kts` to package all compiled submodule JAR libraries (Epic A-K) inside the Magisk release ZIP under `/libs/`.
- Packaged complete architectural placeholders (`arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86`) for the native Zygisk hooks and companion libraries in the output release ZIP.

## [0.1.0-alpha] - 2026-07-26

### Added
- Created gradle multi-module skeleton with 10 submodules mapped to Epic A-J.
- Established standard Magisk module file layout in `release-engineering/magisk_template`.
- Implemented `zipModule` gradle task generating release-ready `out/ToastMagers-release.zip`.
- Added abstract interfaces `SystemHookBridge`, `NotificationServiceAccessor`, and `RuntimeDataAccessor` for dependency decoupling.
- Implemented ReDoS-safe matcher with TimeoutCharSequence evaluation.
- Implemented full rules engine (blacklist, whitelist packages, app rules, global rules).
- Implemented `ConfigManager` configuration validation, file parsing, and fallback.
- Implemented safe log formatting, masking sensitive information (verification codes, phone numbers), and automated log retention policy.
- Implemented subscription secure protocols and cryptographic signature verification.
- Implemented rate-limiting and breaker for toast storms.
- Configured GitHub Actions CI pipeline and complete unit tests covering code structures.
