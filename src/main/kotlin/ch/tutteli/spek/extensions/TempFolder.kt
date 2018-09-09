package ch.tutteli.spek.extensions

import org.jetbrains.spek.api.lifecycle.ActionScope
import org.jetbrains.spek.api.lifecycle.GroupScope
import org.jetbrains.spek.api.lifecycle.LifecycleListener
import org.jetbrains.spek.api.lifecycle.TestScope
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

class TempFolder private constructor(private val scope: Scope) : LifecycleListener {

    private var _tmpDir: Path? = null

    val tmpDir: Path get() = checkState("access tmpDir") { it }

    private fun <T> checkState(actDescription: String, act: (Path) -> T): T {
        check(_tmpDir != null) {
            "You tried to $actDescription but you cannot use TempFolder outside of a ${scope.name} scope."
        }
        return act(_tmpDir!!)
    }

    fun newFile(name: String): Path = checkState("call newFile") { Files.createFile(it.resolve(name)) }
    fun newFolder(name: String): Path = checkState("call newFolder") { Files.createDirectory(it.resolve(name)) }

    override fun beforeExecuteTest(test: TestScope) = setUp(Scope.TEST)
    override fun beforeExecuteAction(action: ActionScope) = setUp(Scope.ACTION)
    override fun beforeExecuteGroup(group: GroupScope) = setUp(Scope.GROUP)

    override fun afterExecuteTest(test: TestScope) = tearDown(Scope.TEST)
    override fun afterExecuteAction(action: ActionScope) = tearDown(Scope.ACTION)
    override fun afterExecuteGroup(group: GroupScope) = tearDown(Scope.GROUP)


    private fun setUp(expectedScope: Scope) {
        if (scope == expectedScope) {
            _tmpDir = Files.createTempDirectory("spek")
        }
    }

    private fun tearDown(expectedScope: Scope) {
        if (scope == expectedScope) {
            Files.walkFileTree(_tmpDir, object : SimpleFileVisitor<Path>() {

                override fun visitFile(file: Path, attrs: BasicFileAttributes) = deleteAndContinue(file)

                override fun postVisitDirectory(dir: Path, exc: IOException?) = deleteAndContinue(dir)

                private fun deleteAndContinue(path: Path): FileVisitResult {
                    Files.delete(path)
                    return FileVisitResult.CONTINUE
                }
            })
            _tmpDir = null
        }
    }

    companion object {
        /**
         * Sets up the [tmpDir] before each test and cleans it up after each test.
         */
        fun perTest() = TempFolder(Scope.TEST)

        /**
         * Sets up the [tmpDir] before each action and cleans it up after each action.
         */
        fun perAction() = TempFolder(Scope.ACTION)

        /**
         * Sets up the [tmpDir] before each group and cleans it up after each group.
         */
        fun perGroup() = TempFolder(Scope.GROUP)
    }

    private enum class Scope {
        TEST,
        ACTION,
        GROUP
    }
}
