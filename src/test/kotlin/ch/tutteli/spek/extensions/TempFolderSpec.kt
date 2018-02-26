package ch.tutteli.spek.extensions

import ch.tutteli.atrium.api.cc.en_UK.*
import ch.tutteli.atrium.assert
import ch.tutteli.atrium.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.lifecycle.GroupScope
import org.jetbrains.spek.api.lifecycle.TestScope
import java.io.File

object TempFolderSpec : Spek({

    val testScope: TestScope = object : TestScope {
        override val parent: GroupScope get() = throw NotImplementedError()
    }
    describe("beforeExecuteTest/afterExecuteTest") {

        action("calling beforeExecuteTest") {
            val testee = TempFolder()
            testee.beforeExecuteTest(testScope)
            val tmpDir = testee.tmpDir

            it("created the tmpDir"){
                assert(tmpDir).returnValueOf(tmpDir::exists).toBe(true)
            }

            var tmpFile = File("needs to be initialized")
            test("calling newFile creates a file with the corresponding name") {
                val fileName = "test.txt"
                tmpFile = testee.newFile(fileName)
                assert(tmpFile) {
                    returnValueOf(subject::getName).toBe(fileName)
                    returnValueOf(subject::exists).toBe(true)
                }
            }

            var tmpFolder = File("needs to be initialized")
            test("calling newFolder creates a folder with the corresponding name") {
                val folderName = "testDir"
                tmpFolder = testee.newFolder(folderName)
                assert(tmpFolder){
                    returnValueOf(subject::getName).toBe(folderName)
                    returnValueOf(subject::exists).toBe(true)
                }
            }

            test("calling afterExecuteTest, deletes tmpDir and the created file and folder"){
                testee.afterExecuteTest(testScope)
                assert(tmpDir).returnValueOf(tmpFile::exists).toBe(false)
                assert(tmpFile).returnValueOf(tmpFile::exists).toBe(false)
                assert(tmpFolder).returnValueOf(tmpFile::exists).toBe(false)
            }

        }
    }

    describe("usage outside of TestScope") {
        val testee = TempFolder()
        mapOf<Pair<String, String>, () -> Any>(
            "accessing" to "tmpDir" to { testee.tmpDir },
            "accessing" to "tmpPath" to { testee.tmpPath },
            "calling" to "newFile" to { testee.newFile("test") },
            "calling" to "newFolder" to { testee.newFolder("test") }
        ).forEach { (pair, act) ->
            val (verb, name) = pair
            it("throws an IllegalStateException when $verb `$name` where the message contains $name") {
                expect {
                    act()
                }.toThrow<IllegalStateException> { message { contains(name) } }
            }
        }
    }
})
