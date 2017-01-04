package ae.vigilancer.gradle.plugin.android.run

/**
 * Possible to specify intent category other then .LAUNCHER
 * See https://developer.android.com/reference/android/content/Intent.html
 * for CATEGORY_name constants
 */
open class AppRunnerExtension {
    var intent_category: String = "android.intent.category.LAUNCHER"
}

