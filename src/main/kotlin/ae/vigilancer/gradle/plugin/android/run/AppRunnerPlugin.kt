package ae.vigilancer.gradle.plugin.android.run

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.builder.testing.ConnectedDeviceProvider
import com.android.builder.testing.api.DeviceProvider
import com.android.ddmlib.NullOutputReceiver
import com.android.utils.StdLogger
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.StopExecutionException
import java.util.concurrent.TimeUnit


class AppRunnerPlugin : Plugin<Project> {

    companion object {
        private val CMD = "monkey -p %s -c %s 1"
    }

    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin(AppPlugin::class.java)) {
            throw StopExecutionException("should be applied after 'com.android.application' plugin")
        }
        project.extensions.create("appRunner", AppRunnerExtension::class.java)
        val ext: AppExtension = project.extensions.getByType(AppExtension::class.java)
        val adb = ext.adbExe
        val deviceProvider: DeviceProvider = ConnectedDeviceProvider(adb, 2000, StdLogger(StdLogger.Level.VERBOSE));
        deviceProvider.init()

        ext.applicationVariants.all { variant ->
            // skipping unsigned non-debug builds since they don't have 'Install*' tasks
            if (!variant.isSigningReady)
                return@all

            val taskName = "run${variant.name.capitalize()}"
            val packageId = variant.applicationId
            val parentTask = variant.install

            val intent_category = (project.extensions.getByName("appRunner") as AppRunnerExtension).intent_category

            val t: DefaultTask = project.task(
                    mapOf(
                            "type" to DefaultTask::class.java,
                            "dependsOn" to parentTask,
                            "description" to "Install and run ${variant.description}.",
                            "group" to "Running"
                    ),
                    taskName) as DefaultTask

            deviceProvider.devices.forEach { device ->
                t.doLast {
                    device.executeShellCommand(commandLineToRunApp(packageId, intent_category), NullOutputReceiver(),
                            10, TimeUnit.SECONDS)
                }
            }
        }
    }

    private fun commandLineToRunApp(packageId: String, intent_category: String): String {
        return CMD.format(packageId, intent_category)
    }

}

