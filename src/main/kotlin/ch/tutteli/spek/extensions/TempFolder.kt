package ch.tutteli.spek.extensions

import org.jetbrains.spek.api.lifecycle.LifecycleListener
import org.jetbrains.spek.api.lifecycle.TestScope
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

class TempFolder : LifecycleListener {
    private var _tmpPath: Path? = null

    val tmpDir: File get() = checkState("access tmpDir", { it.toFile() })
    val tmpPath: Path get() = checkState("access tmpPath", { it })

    private fun <T> checkState(actDescription: String, act: (Path) -> T): T {
        check(_tmpPath != null) {
            "You tried to $actDescription but you cannot use TempFolder outside of a TestScope."
        }
        return act(_tmpPath!!)
    }

    fun newFile(name: String): File = checkState("call newFile", { File(it.toFile(), name).apply { createNewFile() } })
    fun newFolder(name: String): File = checkState("call newFolder", { File(it.toFile(), name).apply { mkdir() } })

    override fun beforeExecuteTest(test: TestScope) {
        _tmpPath = Files.createTempDirectory("spek")
    }

    override fun afterExecuteTest(test: TestScope) {
        Files.walkFileTree(_tmpPath, object : SimpleFileVisitor<Path>() {

            override fun visitFile(file: Path, attrs: BasicFileAttributes) = deleteAndContinue(file)

            override fun postVisitDirectory(dir: Path, exc: IOException?) = deleteAndContinue(dir)

            private fun deleteAndContinue(path: Path): FileVisitResult {
                Files.delete(path)
                return FileVisitResult.CONTINUE
            }
        })
        _tmpPath = null
    }
}
