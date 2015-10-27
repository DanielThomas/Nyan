package nyan

import nyan.NyanCat
import nyan.Stats
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import org.gradle.internal.nativeintegration.console.ConsoleDetector
import org.gradle.internal.nativeintegration.services.NativeServices
import org.gradle.listener.ListenerBroadcast
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream

class NyanPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Gradle has per thread line loggers, so we need the real stdout
        val ps = PrintStream(FileOutputStream(FileDescriptor.out))
        val detector = NativeServices.getInstance().get(javaClass<ConsoleDetector>())
        val cols = detector.getConsole().getCols()
        project.afterEvaluate {
            val tasks = project.getTasks()
            for (name in tasks.getNames()) {
                val task = tasks.getByName(name)
                val stats = Stats()
                val nyan = NyanCat(cols, stats, ps)
                if (task is Test) {
                    val listener = object : TestListener {
                        override fun afterTest(td: TestDescriptor, result: TestResult) {
                            when (result.getResultType()) {
                                TestResult.ResultType.SUCCESS -> stats.passes++
                                TestResult.ResultType.FAILURE -> stats.failures++
                                TestResult.ResultType.SKIPPED -> stats.skipped++
                            }
                            nyan.newline()
                            nyan.draw()
                            nyan.cursorUp(1)
                        }

                        override fun beforeSuite(td: TestDescriptor) {
                        }

                        override fun afterSuite(td: TestDescriptor, result: TestResult) {
                        }

                        override fun beforeTest(td: TestDescriptor) {
                        }
                    }
                    (task as Test).addTestListener(listener)
                }
            }

        }
    }
}