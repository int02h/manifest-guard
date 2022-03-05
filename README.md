# manifest-guard
[![Build Status](https://travis-ci.com/int02h/manifest-guard.svg?branch=main)](https://travis-ci.com/github/int02h/manifest-guard)
[![Latest release](https://img.shields.io/github/release/int02h/manifest-guard.svg)](https://github.com/int02h/manifest-guard/releases/latest)

Gradle plugin for Android applications for detecting unexpected changes in `AndroidManifest.xml`

## The problem being solved
Every third-party android library except .jar ones can have their own `AndroidManifest.xml`. Every Android-specific module in multi-module project is also required to have it. The final
`AndroidManifest.xml` included in application is made by merging all manifests from all libraries and modules. It's hard to track changes due to this file is inside `build` folder which is usually not under some VCS. But `AndroidManifest.xml` is extremely important file of your application. What if some library would introduce new dangerous permission and you would never know it until some problem occurs in the production.

ManifestGuard Gradle plugin is aimed to solve this issue for you. For every build process it will automatically compare old merged manifest with the new one and throw the error if unexpected changes are found.

## Setup
ManifestGuard is applicable only to Android application modules because for libraries it does not make any sense. The setup is easy, just add the plugin to `plugins` blocks in your application's `build.gradle`:
```groovy
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'com.dpforge.manifestguard' version 'x.x.x'
}
```
Basically that's it. But you can configure the plugin depending on your needs. Take a look at the next section.

### Configure the plugin
Plugin has default settings but you can change them in the following way:
```groovy
manifestGuard {
    enabled = shouldEnabledManifestGuard()
    referenceFile = new File(projectDir, "manifest/original.xml")
    htmlDiffFile = new File(projectDir, "manifest-diff.html")
    ignore {
        ignoreAppVersionChanges true
    }
}
```

* `enabled` - whether plugin enabled or not. By default it's enabled;
* `referenceFile` - path to the file which is treated like a reference `AndroidManifest.xml`. It means that this file is going to be compared with new merged manifest during next build. The default file is `GuardedAndroidManifest.xml` placed in the root of the project;
* `htmlDiffFile` - path to the file where HTML report will be written when there are differences between two manifests;
* `ignore` - configuration of ignore options
    * `ignoreAppVersionChanges` - treat as expected changes in `android:versionCode` and `android:versionName` attributes of `manifest` tag. Value of `true` means that if app version has changed then build will be succesful and no report would be generated

## Credits
Thanks to [Dmitriy Voronin](https://github.com/dsvoronin) for the project idea and the contribution.

## License

Copyright (c) 2022 Daniil Popov

Licensed under the [MIT](LICENSE) License.
