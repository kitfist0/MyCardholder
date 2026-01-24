# Wallet

An open-source cardholder app wihtout GMS and Google services to store you cards in one place.

![Screenshots](screenshots.jpg)

## Tech-stack

* [Kotlin](https://kotlinlang.org/)
    * [Android KTX](https://developer.android.com/kotlin/ktx) - set of extensions that are included
      with Android Jetpack and other Android libraries
    * [Coroutines](https://developer.android.com/kotlin/coroutines) - recommended solution for
      asynchronous programming on Android
    * [Kotlin Flow](https://developer.android.com/kotlin/flow) - data flow across all app layers,
      including views
* [Jetpack](https://developer.android.com/jetpack)
    * [CameraX](https://developer.android.com/jetpack/androidx/releases/camera) - camera
      capabilities
    * [Constraintlayout](https://developer.android.com/jetpack/androidx/releases/constraintlayout) -
      position and size widgets in a flexible way with relative positioning
    * [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) - dependency
      injection
    * [Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle) - perform an
      action when lifecycle state changes
    * [Navigation](https://developer.android.com/topic/libraries/architecture/navigation/) - in-app
      navigation
    * [Room](https://developer.android.com/jetpack/androidx/releases/room) - store offline cache
* [Coil](https://github.com/coil-kt/coil) - image loading library
* [ZXing](https://mvnrepository.com/artifact/com.google.zxing/core) - barcode encoding/decoding
  library

## UI

* [View Binding](https://developer.android.com/topic/libraries/view-binding) - retrieve xml view ids
* [Material Design 3](https://m3.material.io/) - application design system providing UI components
* [Dark Theme](https://material.io/develop/android/theming/dark) - dark theme for the app (Android
  10+)
