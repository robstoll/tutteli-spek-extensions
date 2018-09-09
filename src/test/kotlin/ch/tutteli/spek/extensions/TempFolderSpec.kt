package ch.tutteli.spek.extensions

import ch.tutteli.atrium.api.cc.en_GB.*
import ch.tutteli.atrium.assert
import ch.tutteli.atrium.expect
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.lifecycle.ActionScope
import org.jetbrains.spek.api.lifecycle.GroupScope
import org.jetbrains.spek.api.lifecycle.TestScope
import java.nio.file.Paths

object TempFolderSpec : Spek({

    val testScope: TestScope = object : TestScope {
        override val parent: GroupScope get() = throw NotImplementedError()
    }
    val actionScope: ActionScope = object : ActionScope {
        override val parent: ActionScope get() = throw NotImplementedError()
    }
    val groupScope: GroupScope = object : GroupScope {
        override val parent: GroupScope get() = throw NotImplementedError()
    }

    describe("beforeExecuteTest/afterExecuteTest") {

        action("calling beforeExecuteTest") {
            val testee = TempFolder.perTest()
            testee.beforeExecuteTest(testScope)
            val tmpDir = testee.tmpDir

            it("created the tmpDir") {
                assert(tmpDir).exists()
            }

            var tmpFile = Paths.get("needs to be initialized")
            test("calling newFile creates a file with the corresponding name") {
                val fileName = "test.txt"
                tmpFile = testee.newFile(fileName)
                assert(tmpFile) {
                    name.toBe(fileName)
                    exists()
                }
            }

            var tmpFolder = Paths.get("needs to be initialized")
            test("calling newFolder creates a folder with the corresponding name") {
                val folderName = "testDir"
                tmpFolder = testee.newFolder(folderName)
                assert(tmpFolder) {
                    name.toBe(folderName)
                    exists()
                }
            }

            test("calling afterExecuteTest, deletes tmpDir and the created file and folder") {
                testee.afterExecuteTest(testScope)
                assert(tmpDir).existsNot()
                assert(tmpFile).existsNot()
                assert(tmpFolder).existsNot()
            }
        }
    }


    val groupAndAction: (TempFolder) -> Unit =
        { it.beforeExecuteGroup(groupScope); it.beforeExecuteAction(actionScope) }
    val group: (TempFolder) -> Unit = { it.beforeExecuteGroup(groupScope) }

    mapOf(
        "test" to TempFolder.perTest() to ("beforeExecuteGroup and beforeExecuteAction" to groupAndAction),
        "action" to TempFolder.perAction() to ("beforeExecuteGroup" to group),
        "group" to TempFolder.perGroup() to ("<no setup method called>" to { _ -> })
    ).forEach { (nameTestee, callingSetup) ->
        val (name, testee) = nameTestee
        val (calling, setup) = callingSetup
        describe("usage outside of $name scope") {

            action("calling $calling") {
                setup(testee)
                mapOf<Pair<String, String>, () -> Any>(
                    "accessing" to "tmpDir" to { testee.tmpDir },
                    "accessing" to "tmpDir" to { testee.tmpDir },
                    "calling" to "newFile" to { testee.newFile("test") },
                    "calling" to "newFolder" to { testee.newFolder("test") }
                ).forEach { (pair, act) ->
                    val (verb, identifier) = pair
                    it("throws an IllegalStateException when $verb `$identifier` where the message contains $identifier") {
                        expect {
                            act()
                        }.toThrow<IllegalStateException> { message { contains(identifier) } }
                    }
                }
            }
        }
    }
})
