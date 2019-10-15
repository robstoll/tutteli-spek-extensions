package ch.tutteli.spek.extensions

import org.spekframework.spek2.dsl.LifecycleAware
import org.spekframework.spek2.lifecycle.*
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/**
 * Creates a temporary folder which cleans itself up when the memoized value goes out of scope.
 *
 * @param cachingMode Use it to tweak caching, see
 *   [Caching modes @ spekframework.org](https://www.spekframework.org/core-concepts/#caching-modes)
 *   for more information.
 * @param additionalSetup Use this lambda with receiver to create files, folders etc.
 *   which should be setup each time this memoized value gets setup.
 *
 * @return A [MemoizedValue] containing a [MemoizedTempFolder]
 */
fun LifecycleAware.memoizedTempFolder(
    cachingMode: CachingMode = defaultCachingMode,
    additionalSetup: MemoizedTempFolder.() -> Unit = {}
): MemoizedValue<MemoizedTempFolder> =
    memoized(cachingMode, factory = {
        MemoizedTempFolder().apply(additionalSetup)
    }, destructor = {
        it.destructor()
    })

/**
 * Represents a temporary folder, use [memoizedTempFolder] to create one.
 *
 * This class is not thread-safe.
 */
class MemoizedTempFolder internal constructor() {
    private var notCleanedUp = true

    /**
     * The temporary folder.
     */
    val tmpDir: Path = Files.createTempDirectory("spek")


    private fun <T> checkState(actDescription: String, act: (Path) -> T): T {
        check(notCleanedUp) {
            "You tried to $actDescription but you cannot use MemoizedTempFolder once it is destructed."
        }
        return act(tmpDir)
    }

    /**
     * Creates a new file with the given [name] in the current [tmpDir].
     */
    fun newFile(name: String): Path = checkState("call newFile") { Files.createFile(it.resolve(name)) }

    /**
     * Creates a new folder with the given [name] in the current [tmpDir].
     */
    fun newFolder(name: String): Path = checkState("call newFolder") { Files.createDirectory(it.resolve(name)) }

    /**
     * Creates a new symbolic link with the given [name] in the current [tmpDir], targeting the given [target].
     */
    fun newSymbolicLink(name: String, target: Path): Path =
        checkState("call newSymbolicLink") { Files.createSymbolicLink(it.resolve(name), target) }

    /**
     * Resolves the name in the current [tmpDir] and returns the resulting [Path].
     */
    fun resolve(pathAsString: String): Path =
        checkState("call resolve") { it.resolve(pathAsString) }

    internal fun destructor() {
        deleteTmpDir()
        notCleanedUp = false
    }

    private fun deleteTmpDir() {
        Files.walkFileTree(tmpDir, object : SimpleFileVisitor<Path>() {

            override fun visitFile(file: Path, attrs: BasicFileAttributes) = deleteAndContinue(file)

            override fun postVisitDirectory(dir: Path, exc: IOException?) = deleteAndContinue(dir)

            private fun deleteAndContinue(path: Path): FileVisitResult {
                Files.delete(path)
                return FileVisitResult.CONTINUE
            }
        })
    }
}
