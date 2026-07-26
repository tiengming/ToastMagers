# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://commonmark.org/help/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
