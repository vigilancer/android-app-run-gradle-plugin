package ae.vigilancer.gradle.plugin.android.run

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

class AppRunnerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin(AppPlugin::class.java)) {
            throw RuntimeException("should be declared after 'com.android.application'")
        }
        val ext: AppExtension = project.extensions.getByType(AppExtension::class.java)

        ext.applicationVariants.all { v ->
            val taskName = "run${v.name.capitalize()}"
            val parentTask = v.install
            val adb = ext.adbExe

            // skipping unsigned non-debug builds since they don't have 'Install*' tasks
            if (v.isSigningReady) {
                val packageId = v.applicationId

                val t: Exec = project.task(
                    mapOf(
                        "type" to Exec::class.java,
                        "dependsOn" to parentTask,
                        "description" to "Install and run ${v.description}.",
                        "group" to "Running"
                    ),
                    taskName) as Exec
                t.setCommandLine(adb, "shell", "monkey", "-p", packageId, "-c", "android.intent.category.LAUNCHER", "1")
            }
        }
    }
}
