# IOMb Library Android

This repository hosts the IOMb Android library source as the `library/`
module, alongside a `sample-app/` that consumes it. Library source was
imported from
`INFOnline-sg/gitlab-archive-tec-app-libraries-iomb_iomp_android`
(branch `IOMb/release/1.0.0`, head `e869f9f`) as part of the GitLab →
GitHub migration. See `CHANGELOG.md`.

## Modules

* `library/` — IOMb Android library (`de.infonline.lib.iomb`).
* `sample-app/` — example app consuming the library directly via a Gradle
  project dependency (`implementation project(':library')`).

## Requirements

* Android Studio: Giraffe 2022.3.1+ (download latest version [here](https://developer.android.com/studio))
* JDK 17
* TargetSDK 33
* MinSDK 21

## Build

```
./gradlew :library:assembleProdRelease
./gradlew :sample-app:assembleProdDebug
```

## Integration guide

For a detailed integration guide please refer to: [INFOnline IOMb Lib Android Integration Guide](https://docs.infonline.de/infonline-measurement/integration/lib/android/Vorgaben_zum_Aufruf/)

A follow-up PR will add publishing of the library AAR to GitHub Packages
(`https://maven.pkg.github.com/INFOnline-sg/libs-appsensor-iomb-android`)
via a GitHub Actions workflow.
