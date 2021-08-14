package ch.tutteli.spek.extensions

import ch.tutteli.atrium.api.fluent.en_GB.messageContains
import ch.tutteli.atrium.api.fluent.en_GB.toThrow
import ch.tutteli.atrium.api.verbs.expect
import ch.tutteli.niok.getFileAttributeView
import ch.tutteli.niok.newFile
import org.spekframework.spek2.Spek
import org.spekframework.spek2.dsl.Skip
import org.spekframework.spek2.style.specification.describe
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFileAttributeView

object DirectoryNotEmptySpec : Spek({

    describe("Files.list on Windows without closing stream") {

        // Windows
        val ifPosixNotSupported =
            if (Paths.get("test").getFileAttributeView<PosixFileAttributeView>() == null) {
                Skip.No
            } else {
                Skip.Yes(
                    "POSIX permissions are supported on this file system, " +
                        "locking will only be an advice, hence skipping"
                )
            }

        it("outputs a hint about DirectoryNotEmptyException", skip = ifPosixNotSupported) {
            val tmpFolder = MemoizedTempFolder()
            val a = tmpFolder.newDirectory("a")
            a.newFile("b.txt")

            // keep file handle open on purpose
            Files.list(a)
            expect {
                tmpFolder.destructor()
            }.toThrow<IllegalStateException> {
                messageContains("not empty after deleting all containing files and directories")
            }
        }
    }
})
