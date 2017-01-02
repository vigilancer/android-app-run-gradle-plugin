package ae.vigilancer.gradle.plugin.android.run

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.StopExecutionException

class AppRunnerPlugin : Plugin<Project> {

    val cmd_monkey_mobile = "shell monkey -p %s -c android.intent.category.LAUNCHER 1"
    val cmd_monkey_tv = "shell monkey -p %s -c android.intent.category.LEANBACK_LAUNCHER 1"

    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin(AppPlugin::class.java)) {
            throw StopExecutionException("should be applied after 'com.android.application' plugin")
        }
        project.extensions.create("appRunner", AppRunnerExtension::class.java)
        val ext: AppExtension = project.extensions.getByType(AppExtension::class.java)
        val adb = ext.adbExe

        ext.applicationVariants.all { variant ->
            // skipping unsigned non-debug builds since they don't have 'Install*' tasks
            if (!variant.isSigningReady)
                return@all

            val taskName = "run${variant.name.capitalize()}"
            val packageId = variant.applicationId
            val parentTask = variant.install

            val isTvApp = (project.extensions.getByName("appRunner") as AppRunnerExtension).tv

            val cmd: Exec = project.task(
            mapOf(
                "type" to Exec::class.java,
                "dependsOn" to parentTask,
                "description" to "Install and run ${variant.description}.",
                "group" to "Running"
            ),
            taskName) as Exec
            cmd.setExecutable(adb)
            cmd.setArgs(adbArguments(packageId, isTvApp))
        }
    }

    private fun adbArguments(packageId: String, isTvApp: Boolean): Iterable<Any> {
        val cmd = if (isTvApp) cmd_monkey_tv else cmd_monkey_mobile

        return cmd.format(packageId).split(" ")
    }
}

