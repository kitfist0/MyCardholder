package my.cardholder.data.model

import my.cardholder.R

enum class CloudProvider(val cloudName: String) {
    GOOGLE("Google Drive"),
    YANDEX( "Yandex Disk");

    companion object {
        fun CloudProvider.getDrawableRes(): Int = when (this) {
            GOOGLE -> R.drawable.ic_cloud_google
            YANDEX -> R.drawable.ic_cloud_yandex
        }
    }
}
