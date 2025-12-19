# manifest-guard
[![Build Status](https://github.com/int02h/manifest-guard/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/int02h/manifest-guard/actions/workflows/android.yml)
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
    id 'com.dpforge.manifestguard' version '1.2.0'
}
```
or legacy plugin application:
```groovy
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    
    dependencies {
        classpath "com.dpforge:manifestguard:1.2.0"
    }
}

apply plugin: "com.dpforge.manifestguard"
```
Basically that's it. But you can configure the plugin depending on your needs. Take a look at the next section.

### Configure the plugin
Plugin has default settings. It means you can just apply plugin and it will work. In case you want more precise
configuration you can do it in the following way:

```groovy
manifestGuard {
    defaultConfig {
        enabled = true // by default it's also true, here it is just for sake of documentation
        compareOnAssemble = false // default value is true
        referenceFile = new File(projectDir, "manifest/original.xml")
        htmlDiffFile = new File(projectDir, "manifest-diff.html")
        ignore {
            ignoreAppVersionChanges = true // default value is false
        }
    }
    guardVariant("debug") {
        compareOnAssemble = true
    }
}
```

First of all there is a `defaultConfig` section which sets up the default configuration being used for all build
variants. It has the following parameters:

* `enabled` - whether plugin is enabled or not. When plugin is disable it does nothing.
* `compareOnAssemble` - whether manifest comparison is done automatically on every project assembly. 
  Default value is `true` while `false` means you have to invoke task `compare${VARIANT_NAME}MergedManifest` manually. 
  For example `compareDebugMergedManifest`. 
* `referenceFile` - path to the file which is treated like a reference `AndroidManifest.xml`. 
  It means that this file is going to be compared with new merged manifest during next build. The default file is 
  `GuardedAndroidManifest.xml` placed in the root of the project;
* `htmlDiffFile` - path to the file where HTML report will be written when there are differences between two manifests;
* `ignore` - configuration of ignore options
    * `ignoreAppVersionChanges` - treat as expected changes in `android:versionCode` and `android:versionName` 
      attributes of `manifest` tag. Value of `true` means that if app version has changed then manifest comparison will 
      be successful and no report would be generated. Default value is `false`.
      
If you would like to make a different configuration for some particular build variant then `guardVariant()` can help
you here. You don't need to provide all options there since any missing option will inherit its value from default 
configuration. In the example from above the option `compareOnAssemble` is set to `true` for `debug` build variant.

### Update reference manifest

If there is no reference `AndroidManifest.xml` file then it will be created automatically on next comparison. When you
introduce changes into manifest intentionally and want to update the reference then you should invoke task
`update${VARIANT_NAME}ReferenceManifest`. For example `updateDebugReferenceManifest`. It will update reference manifest
and the next comparison will be successful.

## Migration

### From 0.x.x to 1.x.x

Release 1.0.0 introduces new way of configuring the plugin. Now the plugin has a default configuration placed inside
`defaultConfig` section but it also allows you to configure the plugin on per-variant base using `guardVariant()`.

So the easiest way to migrate is to change the following configuration inside `build.gradle` file:
```groovy
manifestGuard {
    compareOnAssemble = false
    referenceFile = new File(projectDir, "manifest/original.xml")
    htmlDiffFile = new File(projectDir, "manifest-diff.html")
    ignore {
        ignoreAppVersionChanges = true
    }
}
```
to
```groovy
manifestGuard {
    defaultConfig {
        compareOnAssemble = false
        referenceFile = new File(projectDir, "manifest/original.xml")
        htmlDiffFile = new File(projectDir, "manifest-diff.html")
        ignore {
            ignoreAppVersionChanges = true
        }
    }
}
```

Please notice that properties inside `ignore` block now require `=`.

## Credits
Thanks to [Dmitriy Voronin](https://github.com/dsvoronin) for the project idea and the contribution.

Thanks to [Sergey Boishtyan](https://github.com/sboishtyan) for introducing per-build-variant configuration.

Thanks to [Alexander Suslov](https://github.com/subprogram) for keeping dependencies up-to-update and bux fixing

## License

Copyright (c) 2025 Daniil Popov

Licensed under the [MIT](LICENSE) License.
