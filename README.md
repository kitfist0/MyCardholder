# MyCardholder

An open source cardholder app to store you cards in one place.

![Screenshots](readme-screenshots.jpg)

## Cloud sync logic

The following terminology is used below:

* **Sync id** - card identifier shared by all devices.
* **Deleted card** - record of a deleted card, kept so that the deletion reaches other devices.
* **Checksum** - hash of sync ids and change times of all cards and deleted cards.
  Equal checksums mean equal data. A backup file is named by its checksum.
* **Latest synced checksum** - checksum saved after the last successful sync.

Sync runs when it is enabled or the app starts, and then two seconds after local changes.
Syncs never run in parallel.

```mermaid
sequenceDiagram
    participant Client
    participant Cloud

    Note over Client, Cloud: Getting cloud checksums
    Client->>Cloud: Get backup file names
    Cloud-->>Client: Cloud checksums

    loop Each cloud checksum except local and latest synced
        Note over Client, Cloud: Download backup file
        Client->>Cloud: Get backup file
        Cloud-->>Client: Backup file bytes

        Note over Client: Merge backup into the database
        Client->>Client: Reading cards and deleted cards
        Client->>Client: Merge them card by card
    end

    Note over Client: Calculating checksum of the merged data
    alt Cloud has other backups
        opt No backup with the merged checksum
            Note over Client, Cloud: Upload backup file
            Client->>Client: Writing cards and deleted cards to a backup file
            Client->>Cloud: Backup file bytes
            Cloud-->>Client: OK
        end

        Note over Client, Cloud: Delete merged backups
        Client->>Cloud: Delete old backup files
    else Cloud has only the merged backup
        Note over Client: Skip upload
    end

    Client->>Client: Save merged checksum as latest synced
```

A new backup is uploaded before old ones are deleted, so the cloud is never left without a backup.
Backups uploaded by another device during the sync are not deleted and are merged next time.

### Merge rules

* The card version with the latest change time wins.
* A deletion wins over changes made before it, a later change restores the card.
* Cards from backups created before sync ids existed are added only if there is
  no card with the same name, content and format.
* A card created with the same name, content and format on another device takes its sync id
  instead of becoming a duplicate.

### Build Variants

The application has
two [product flavors](https://developer.android.com/build/build-variants#product-flavors).

| Product Flavor | For which store                                  | Billing Library                                                                    |
|----------------|--------------------------------------------------|------------------------------------------------------------------------------------|
| **googleplay** | [GooglePlay](https://play.google.com/store/apps) | [Google Play Billing](https://developer.android.com/google/play/billing/integrate) |
| **rustore**    | [RuStore](https://www.rustore.ru/)               | [RuStore Pay SDK](https://www.rustore.ru/help/sdk/pay)                             |

## How to build

1. Put `release.keystore` and `release.properties` files to [cert](/cert) folder.
    * `release.keystore` - contains the key used to sign the Android app.
    * `release.properties` - contains the values of the store password, alias, and key password.
2. Run [build-aab.sh](/build-aab.sh) in terminal.

Also, don't forget about the [google-services.json](app/google-services.json) file. It can be found
in the Firebase console:

![Firebase console](readme-firebase.jpg)

Download this file and place it in the [app](app) folder of this project.

## Tech-stack

* [Kotlin](https://kotlinlang.org/)
    * [Android KTX](https://developer.android.com/kotlin/ktx) - set of extensions that are included
      with Android Jetpack and other Android libraries
    * [Coroutines](https://developer.android.com/kotlin/coroutines) - recommended solution for
      asynchronous programming on Android
    * [Kotlin Flow](https://developer.android.com/kotlin/flow) - data flow across all app layers,
      including views
* [Firebase](https://firebase.google.com/)
    * [ML Kit's barcode scanning](https://firebase.google.com/docs/ml-kit/read-barcodes)
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
