## Unreleased

**Changed**

- Imported the `library/` module from `INFOnline-sg/gitlab-archive-tec-app-libraries-iomb_iomp_android` (branch `IOMb/release/1.0.0`, head `e869f9f`) as part of the GitLab → GitHub migration.
- Modernised the library build for AGP 8.1.4: namespace declared in `library/build.gradle`, `package=` removed from manifest, JVM target 17, `compileSdk`/`targetSdk` 33, `minSdk` 21, explicit `buildFeatures { buildConfig = true }`.
- Updated library dependencies in line with AGP 8 / JDK 17: `desugar_jdk_libs` 1.1.5 → 2.0.4, dagger 2.33 → 2.48, moshi 1.12.0 → 1.15.0, okhttp 4.9.1 → 4.12.0, robolectric 4.3.1 → 4.11.1, JUnit Jupiter 5.7.1 → 5.10.1, kotest 4.4.3 → 5.8.0, mockito 3.3.0 → 5.7.0, mockk 1.10.0 → 1.13.8.
- Sample app now consumes the library via `implementation project(':library')` instead of the dead `repo.infonline.de` GitLab Maven URL. Sample-app `credentials.gradle` removed (no longer needed).

**Removed**

- PowerMock test dependency stack (powermock-module-junit4, powermock-module-junit4-rule, powermock-api-mockito2, powermock-classloading-xstream) — incompatible with JDK 17 and unused in test sources.
- `org.json:json:20140107` legacy test dep — paired with PowerMock, also unused.
- `jcenter()` repository references — repository sunset.
- `git-publish` plugin and `gradle-git-publish` classpath at root — pushed to a now-dead Rockabyte GitHub mirror; replaced in a follow-up PR by GitHub Packages publishing via GitHub Actions.

**Notes**

- A follow-up PR will add the GitHub Packages publish target and the
  `.github/workflows/publish.yml` workflow.
- A hard-coded GitLab OAuth token was present in the source archive's
  root `build.gradle` (`UfsGq8Q6AjwzcZsLGyxn`). It does not enter this
  repo at any point. Rotation/revocation is tracked separately as a
  security follow-up.


## 1.1.2 (2024-02-26)

**Fixed**

- v1.1.1 issue: Interface not found (obfuscator settings)


## 1.1.1 (2024-02-20) [YANKED]

**Added**

- AGP8 / R8 Full Mode compatibility

**Changed**

- Optimised lifecycle events handling
- Compile/target SDK level 33
- Updated all relevant dependencies


## 1.1.0 (2023-06-27)

**Added**

- New measurement system for OEWA (measurement type IOMB_AT) including hybrid measurement

**Changed**

- Country parameter set to "at" for IOMB_AT
- Updated dependencies

**Fixed**

- Potential exception in network monitor when app is in background


## 1.0.3 (2022-08-18)

**Changed**

- Optimised data validation routine


## 1.0.2 (2022-03-04)

**Changed**

- Updated all relevant dependencies
- Default value for empty category
- Optimised maven publishing (gradle plugin)
- Optimised obfuscation
- Compile/target SDK level 31

**Fixed**

- Category string sanitizing
- Scope of transitive dependencies in Maven POM (avoid timber lint warnings)
- Potential NPE in network state handling

## 1.0.1 (2021-11-19)

**Changed**

- Using region of measurement instead of device region settings ("cn")

**Fixed**

- Character encoding/escaping for comment parameter ("co")
- Log output with debugMode=true


## 1.0.0 (2021-10-14)

- Initial release