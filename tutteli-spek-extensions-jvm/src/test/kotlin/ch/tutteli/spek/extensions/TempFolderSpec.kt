package ch.tutteli.spek.extensions

import ch.tutteli.atrium.api.cc.en_GB.isNotSameAs
import ch.tutteli.atrium.api.cc.en_GB.messageContains
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.expect
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.lifecycle.GroupScope
import org.spekframework.spek2.lifecycle.TestScope
import org.spekframework.spek2.style.specification.describe
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

object TempFolderSpec : Spek({

    val testScope: TestScope = object : TestScope {
        override val parent: GroupScope get() = throw NotImplementedError()
    }
    val groupScope: GroupScope = object : GroupScope {
        override val parent: GroupScope get() = throw NotImplementedError()
    }

    describe("beforeExecute.../afterExecute...") {
        listOf(
            Triple(
                "Test",
                { val t = TempFolder.perTest(); t.beforeExecuteTest(testScope); t },
                { t: TempFolder -> t.afterExecuteTest(testScope) }),
            Triple(
                "Group",
                { val t = TempFolder.perGroup(); t.beforeExecuteGroup(groupScope); t },
                { t: TempFolder -> t.afterExecuteGroup(groupScope) })
        ).forEach { (description, setUp, tearDown) ->
            context("context $description") {
                context("calling beforeExecute$description") {
                    lateinit var testee: TempFolder
                    beforeGroup {
                        testee = setUp()
                    }

                    it("created the tmpDir") {
                        expect(testee.tmpDir).exists()
                    }

                    var tmpFile = Paths.get("needs to be initialized")
                    it("calling newFile creates a file with the corresponding name") {
                        val fileName = "test.txt"
                        tmpFile = testee.newFile(fileName)
                        expect(tmpFile) {
                            name.toBe(fileName)
                            exists()
                            parent.toBe(testee.tmpDir)
                        }
                    }

                    var tmpFolder = Paths.get("needs to be initialized")
                    it("calling newFolder creates a folder with the corresponding name") {
                        val folderName = "testDir"
                        tmpFolder = testee.newFolder(folderName)
                        expect(tmpFolder) {
                            name.toBe(folderName)
                            exists()
                            parent.toBe(testee.tmpDir)
                        }
                    }

                    var tmpSymlink = Paths.get("needs to be initialized")
                    it("calling newSymbolicLink creates a folder with the corresponding name") {
                        val linkName = "testLink"
                        tmpSymlink = testee.newSymbolicLink(linkName, tmpFolder)
                        expect(tmpSymlink) {
                            name.toBe(linkName)
                            exists(NOFOLLOW_LINKS)
                            parent.toBe(testee.tmpDir)
                        }
                    }

                    it("calling afterExecuteTest, deletes tmpDir and the created file and folder any symlink") {
                        val tmpDir = testee.tmpDir
                        expect(tmpDir).exists()
                        expect(tmpFile).exists()
                        expect(tmpFolder).exists()
                        expect(tmpSymlink).exists(NOFOLLOW_LINKS)

                        tearDown(testee)
                        expect(tmpDir).existsNot()
                        expect(tmpFile).existsNot()
                        expect(tmpFolder).existsNot()
                        expect(tmpSymlink).existsNot(NOFOLLOW_LINKS)
                    }
                }
            }
        }
    }

    describe("nested groups") {
        context("calling beforeExecuteGroup") {
            val testee by memoized(CachingMode.SCOPE) { TempFolder.perGroup() }
            lateinit var first: Path
            beforeGroup {
                testee.beforeExecuteGroup(groupScope)
                first = testee.tmpDir
            }

            it("created the first tmpDir") {
                expect(first).exists()
            }
            context("in nested group") {
                lateinit var second: Path
                beforeGroup {
                    testee.beforeExecuteGroup(groupScope)
                    second = testee.tmpDir
                }
                it("created the second tmpDir") {
                    expect(second).exists()
                }

                it("first is not the same as the second") {
                    expect(first).isNotSameAs(second)
                }

                it("calling afterExecuteGroup, deletes second tmpDir but not first") {
                    expect(first).exists()
                    expect(second).exists()
                    testee.afterExecuteGroup(groupScope)

                    expect(first).exists()
                    expect(second).existsNot()
                }
                it("calling afterExecuteGroup a second time, deletes also first tmpDir") {
                    expect(first).exists()
                    expect(second).existsNot()
                    testee.afterExecuteGroup(groupScope)

                    expect(first).existsNot()
                    expect(second).existsNot()
                }
                it("calling afterExecuteGroup a third time, throws IllegalState") {
                    expect(first).existsNot()
                    expect(second).existsNot()

                    expect {
                        testee.afterExecuteGroup(groupScope)
                    }.toThrow<EmptyStackException> {}
                }
            }
        }
    }

    val group: (TempFolder) -> Unit = { it.beforeExecuteGroup(groupScope) }

    mapOf(
        "test" to TempFolder.perTest() to ("beforeExecuteGroup and beforeExecuteAction" to group),
        "group" to TempFolder.perGroup() to ("<no setup method called>" to { _ -> })
    ).forEach { (nameTestee, callingSetup) ->
        val (name, testee) = nameTestee
        val (calling, setup) = callingSetup
        describe("usage outside of $name scope") {

            context("calling $calling") {
                setup(testee)
                mapOf<Pair<String, String>, () -> Any>(
                    "accessing" to "tmpDir" to { testee.tmpDir },
                    "calling" to "newFile" to { testee.newFile("test") },
                    "calling" to "newFolder" to { testee.newFolder("test") },
                    "calling" to "newSymbolicLink" to { testee.newSymbolicLink("test", Paths.get("test")) }
                ).forEach { (pair, act) ->
                    val (verb, identifier) = pair
                    it("throws an IllegalStateException when $verb `$identifier` where the message contains $identifier") {
                        expect {
                            act()
                        }.toThrow<IllegalStateException> { messageContains(identifier) }
                    }
                }
            }
        }
    }
})
