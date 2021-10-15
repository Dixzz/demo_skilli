package org.skilli.snaper.utils

interface SnaperConstants {
    companion object {
        const val GIVEN_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss XXX"
        const val ROOT_URL = "https://api.jsonbin.io/"
        const val IMAGE_URL = "https://unsplash.it/"
    }

    object BundleKeys {
        const val KEY_ITEM_POS = "item_pos"
    }

    object IntentKeys {
        const val KEY_LOCAL_BR = "local_br"
    }
}