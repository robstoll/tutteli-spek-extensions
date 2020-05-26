package ch.tutteli.spek.extensions

import ch.tutteli.atrium.api.cc.en_GB.isNotSameAs
import ch.tutteli.atrium.api.cc.en_GB.returnValueOf
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.expect
import ch.tutteli.niok.createDirectories
import ch.tutteli.niok.followSymbolicLink
import org.spekframework.spek2.Spek
import org.spekframework.spek2.dsl.Root
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path

object FeatureSpec : Spek({


    val tmpFolder by memoizedTempFolder(CachingMode.EACH_GROUP)
    lateinit var tmpDirLeak: Path

    fun Root.checkCleanup(name: String, pathProvider: () -> Path) {
        describe("after $name") {
            it("file is deleted") {
                expect(pathProvider()).existsNot()
            }
            it("tmpDir is deleted") {
                expect(tmpDirLeak).existsNot()
            }
            it("new tmpDir is not the same") {
                expect(tmpDirLeak).isNotSameAs(tmpFolder.tmpDir)
            }
        }
    }

    lateinit var file: Path
    describe("newFile") {
        it("creates a file with the corresponding name") {
            tmpDirLeak = tmpFolder.tmpDir

            val fileName = "test.txt"
            file = tmpFolder.newFile(fileName)
            expect(file) {
                name.toBe(fileName)
                exists()
                parent.toBe(tmpFolder.tmpDir)
            }
        }
        it("tmpDir is deleted after scope") {
            expect(tmpDirLeak).exists()
        }
    }
    checkCleanup("newFile") { file }

    lateinit var dir1: Path
    describe("newDirectory") {

        it("creates a directory with the corresponding name") {
            val folderName = "testDir"
            dir1 = tmpFolder.newDirectory(folderName)
            tmpDirLeak = tmpFolder.tmpDir

            expect(dir1) {
                name.toBe(folderName)
                exists()
                parent.toBe(tmpFolder.tmpDir)
            }
        }
    }
    checkCleanup("newDirectory") { dir1 }

    lateinit var folder: Path
    describe("newFolder") {

        it("creates a folder with the corresponding name") {
            val folderName = "testDir"
            @Suppress("DEPRECATION")
            folder = tmpFolder.newFolder(folderName)
            tmpDirLeak = tmpFolder.tmpDir

            expect(folder) {
                name.toBe(folderName)
                exists()
                parent.toBe(tmpFolder.tmpDir)
            }
        }
    }
    checkCleanup("newFolder") { folder }

    lateinit var symbolicLink: Path
    describe("newSymbolicLink") {
        it("creates a link with the corresponding name") {
            tmpDirLeak = tmpFolder.tmpDir

            val linkName = "testLink"
            val target = tmpFolder.newFile("someFile.txt")
            symbolicLink = tmpFolder.newSymbolicLink(linkName, target)
            expect(symbolicLink) {
                name.toBe(linkName)
                exists(NOFOLLOW_LINKS)
                parent.toBe(tmpFolder.tmpDir)
                returnValueOf(Path::followSymbolicLink).toBe(target)
            }
        }
    }
    checkCleanup("newSymbolicLink") { symbolicLink }

    lateinit var dir2: Path
    describe("withinTmpDir") {
        it("applies the given function to the tmpDir") {
            tmpDirLeak = tmpFolder.tmpDir

            val dirName = "dir2"
            tmpFolder.withinTmpDir {
                dir2 = resolve(dirName)
                dir2.resolve("dir3/dir4/").createDirectories()
            }
            expect(dir2) {
                name.toBe(dirName)
                exists(NOFOLLOW_LINKS)
                parent.toBe(tmpFolder.tmpDir)
            }
        }
    }
    checkCleanup("withinTmpDir") { dir2 }
})
