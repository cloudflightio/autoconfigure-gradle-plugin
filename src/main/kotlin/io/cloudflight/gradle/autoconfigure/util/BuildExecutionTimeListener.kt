package io.cloudflight.gradle.autoconfigure.util

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskState
import java.util.*
import java.util.concurrent.TimeUnit

class BuildExecutionTimeListener : TaskExecutionListener, BuildListener {

    companion object {
        private val LOG = Logging.getLogger(BuildExecutionTimeListener::class.java)

        private const val MIN_TIME_FOR_LOG = 100
        private const val MAX_LENGTH_FOR_TIME_STRING = 27
        private const val MIN_GAP_BETWEEN_TEXT_AND_TIME = 2
    }

    private val taskStack = Stack<String>()
    private val executionTimes = mutableMapOf<String, Long>()
    private var lastTaskStartTime: Long = 0

    override fun beforeExecute(task: Task) {
        taskStack.push(task.path)
        lastTaskStartTime = System.currentTimeMillis()
    }

    override fun afterExecute(task: Task, state: TaskState) {
        val latestTask = taskStack.peek()
        if (task.path != latestTask) {
            LOG.debug("We have $latestTask on the peek of the stack, but we were closing ${task.path}. Build Execution Times will be wrong")
        } else {
            val lastTask = taskStack.pop()
            executionTimes[lastTask] = System.currentTimeMillis() - lastTaskStartTime
        }
        if (!taskStack.empty()) {
            // we have other tasks still on the stack that will be executed probably now
            lastTaskStartTime = System.currentTimeMillis()
        }
    }


    override fun buildFinished(result: BuildResult) {
        if (executionTimes.isEmpty()) {
            return
        }
        val timesPerModule = mutableMapOf<String, Long>()
        val timesPerTask = mutableMapOf<String, Long>()

        val sum = executionTimes.values.sum()

        if (sum == 0.toLong()) {
            return
        }

        val allTasks = StringBuilder()
        allTasks.append("All tasks that take longer than $MIN_TIME_FOR_LOG ms").append(System.lineSeparator())
        val maxTaskPathLength = getMaxKeyLength(executionTimes)
        addHeader(allTasks, maxTaskPathLength)
        executionTimes.toSortedMap().forEach {
            val taskPath = it.key
            val time = it.value

            val lastColon = taskPath.lastIndexOf(':')
            val taskName = taskPath.substring(lastColon + 1)
            if (!timesPerTask.containsKey(taskName)) {
                timesPerTask.put(taskName, time)
            } else {
                timesPerTask.put(taskName, time + timesPerTask.getValue(taskName))
            }

            val module = taskPath.substring(0, lastColon)
            if (!timesPerModule.containsKey(module)) {
                timesPerModule.put(module, time)
            } else {
                timesPerModule.put(module, time + timesPerModule.getValue(module))
            }
            if (time >= MIN_TIME_FOR_LOG) {
                addEntry(allTasks, taskPath, maxTaskPathLength, time, sum)
            }
        }

        val modules = StringBuilder()
        modules.append("Grouped by module").append(System.lineSeparator())
        val maxModulePathLength = getMaxKeyLength(timesPerModule)
        addHeader(modules, maxModulePathLength)
        timesPerModule.entries.sortedWith(TimeDescendingComparator).forEach {
            val modulePath = it.key
            val time = it.value
            addEntry(modules, if (modulePath.isBlank()) "<<root>>" else modulePath, maxModulePathLength, time, sum)

        }

        val tasks = StringBuilder()
        tasks.append("Grouped by task").append(System.lineSeparator())
        val maxTaskNameLength = getMaxKeyLength(timesPerTask)
        addHeader(tasks, maxTaskNameLength)
        timesPerTask.entries.sortedWith(TimeDescendingComparator).forEach {
            val taskName = it.key
            val time = it.value
            addEntry(tasks, taskName, maxTaskNameLength, time, sum)
        }

        LOG.quiet(
            """
BUILD EXECUTION TIMES
=====================
Total: ${format(sum)}

${modules}
${tasks}
${allTasks}
"""
        )
    }


    private fun addHeader(tasks: StringBuilder, maxTaskNameLength: Int): StringBuilder {
        return tasks.append("-".repeat(maxTaskNameLength + MIN_GAP_BETWEEN_TEXT_AND_TIME + MAX_LENGTH_FOR_TIME_STRING))
            .append(System.lineSeparator())
    }

    private fun addEntry(builder: StringBuilder, entry: String, maxEntryLength: Int, time: Long, sum: Long) {
        val timeString = createTimeString(time, sum)
        builder.append(entry)
            .append(" ".repeat(Math.max(1, maxEntryLength + MIN_GAP_BETWEEN_TEXT_AND_TIME - entry.length)))
            .append(" ".repeat(Math.max(1, MAX_LENGTH_FOR_TIME_STRING - timeString.length)))
            .append(timeString).append(System.lineSeparator())
    }

    private fun createTimeString(time: Long, sum: Long): String {
        return time.toString() + " ms" + "  (" + (Math.round(time / sum.toDouble() * 100)).toString()
            .padStart(3, ' ') + "%)"
    }

    private fun format(millis: Long): String {
        return String.format(
            "%dm %ds",
            TimeUnit.MILLISECONDS.toMinutes(millis),
            TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        )
    }

    private fun getMaxKeyLength(map: Map<String, Long>): Int {
        if (map.isEmpty()) {
            return 0
        } else {
            return map.keys.toSortedSet(StringLengthDescendingComparator).first().length
        }
    }

    private object StringLengthDescendingComparator : Comparator<String> {
        override fun compare(o1: String, o2: String): Int {
            return o2.length - o1.length
        }
    }

    private object TimeDescendingComparator : Comparator<Map.Entry<String, Long>> {
        override fun compare(o1: Map.Entry<String, Long>, o2: Map.Entry<String, Long>): Int {
            return (o2.value - o1.value).toInt()
        }
    }

    override fun settingsEvaluated(settings: Settings) {}
    override fun projectsLoaded(gradle: Gradle) {}
    override fun projectsEvaluated(gradle: Gradle) {}
}