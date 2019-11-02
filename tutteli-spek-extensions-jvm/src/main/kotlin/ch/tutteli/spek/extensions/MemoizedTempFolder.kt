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

    /**
     * The temporary folder.
     */
    val tmpDir: Path = Files.createTempDirectory("spek")

    /**
     * Creates a new file with the given [name] in the current [tmpDir].
     */
    fun newFile(name: String): Path = Files.createFile(resolve(name))

    /**
     * Creates a new folder with the given [name] in the current [tmpDir].
     */
    @Deprecated("use newDirectory, will be removed with 2.0.0", ReplaceWith("newDirectory(name)"))
    fun newFolder(name: String): Path = newDirectory(name)

    /**
     * Creates a new directory with the given [name] in the current [tmpDir].
     */
    fun newDirectory(name: String): Path = Files.createDirectory(resolve(name))

    /**
     * Creates a new symbolic link with the given [name] in the current [tmpDir], targeting the given [target].
     */
    fun newSymbolicLink(name: String, target: Path): Path = Files.createSymbolicLink(resolve(name), target)

    /**
     * Resolves the name in the current [tmpDir] and returns the resulting [Path].
     */
    fun resolve(pathAsString: String): Path = tmpDir.resolve(pathAsString)

    /**
     * Applies the given function [f] to the [tmpDir] -- especially useful in combination with [Niok](https://github.com/robstoll/niok)
     */
    fun <R> withinTmpDir(f: Path.() -> R): R = with(tmpDir, f)

    internal fun destructor() {
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
