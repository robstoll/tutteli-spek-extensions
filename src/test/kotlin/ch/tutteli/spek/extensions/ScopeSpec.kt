package ch.tutteli.spek.extensions

import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.expect
import ch.tutteli.niok.delete
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe

object ScopeSpec : Spek({

    describe("memoized default is TEST") {
        val tempFolder by memoizedTempFolder {
            newFile("bla-default.txt")
        }
        context("first context") {
            it("it can be deleted") {
                val bla = tempFolder.resolve("bla-default.txt")
                bla.delete()
                expect(bla).notToExist()
            }
            it("it is recreated each time") {
                expect(tempFolder.resolve("bla-default.txt")).toExist()
            }
        }
    }


    describe("memoized per test") {
        val tmpFolder by memoizedTempFolder(CachingMode.TEST) {
            newFile("bla.txt")
        }
        context("first context") {
            it("it exists at the beginning") {
                expect(tmpFolder.resolve("bla.txt")).toExist()
            }
            it("it can be deleted") {
                val bla = tmpFolder.resolve("bla.txt")
                bla.delete()
                expect(bla).notToExist()
            }
            it("it is recreated each time") {
                val bla = tmpFolder.resolve("bla.txt")
                expect(bla).toExist()
                bla.delete()
                expect(bla).notToExist()
            }
        }
        context("second context") {
            it("it is also recreated for the second context") {
                expect(tmpFolder.resolve("bla.txt")).toExist()
            }
        }
    }

    describe("memoized per scope") {
        val tempFolder by memoizedTempFolder(CachingMode.SCOPE) {
            newFile("bla-per-scope.txt")
        }
        context("first context") {
            it("file from `memoized per test` does not exist") {
                expect(tempFolder.resolve("bla.txt")).notToExist()
            }
            it("it exists at the beginning") {
                expect(tempFolder.resolve("bla-per-scope.txt")).toExist()
            }
            it("it can be deleted") {
                val bla = tempFolder.resolve("bla-per-scope.txt")
                bla.delete()
                expect(bla).notToExist()
            }
            it("it does not exist afterwards") {
                expect(tempFolder.resolve("bla-per-scope.txt")).notToExist()
            }
        }
        context("second context") {
            it("it does also not exist in a second scope") {
                expect(tempFolder.resolve("bla-per-scope.txt")).notToExist()
            }
        }
    }

    describe("memoized per group") {
        val tmpFolder by memoizedTempFolder(CachingMode.EACH_GROUP) {
            newFile("bla-per-group.txt")
        }
        context("first context") {
            it("file from `memoized per test` does not exist") {
                expect(tmpFolder.resolve("bla.txt")).notToExist()
            }
            it("file from `memoized per scope` does not exist") {
                expect(tmpFolder.resolve("bla-per-scope.txt")).notToExist()
            }
            it("it exists at the beginning") {
                expect(tmpFolder.resolve("bla-per-group.txt")).toExist()
            }
            it("it can be deleted") {
                val bla = tmpFolder.resolve("bla-per-group.txt")
                bla.delete()
                expect(bla).notToExist()
            }
            it("it does not exist afterwards") {
                expect(tmpFolder.resolve("bla-per-group.txt")).notToExist()
            }
            context("nested context") {
                it("it is recreated in a nested context") {
                    expect(tmpFolder.resolve("bla-per-group.txt")).toExist()
                }
                it("it can be deleted") {
                    val bla = tmpFolder.resolve("bla-per-group.txt")
                    bla.delete()
                    expect(bla).notToExist()
                }
                it("it does not exist afterwards") {
                    expect(tmpFolder.resolve("bla-per-group.txt")).notToExist()
                }
            }
        }
        context("second context") {
            it("it is recreated per context") {
                expect(tmpFolder.resolve("bla-per-group.txt")).toExist()
            }
            it("it can be deleted") {
                val bla = tmpFolder.resolve("bla-per-group.txt")
                bla.delete()
                expect(bla).notToExist()
            }
            it("it does not exist afterwards") {
                expect(tmpFolder.resolve("bla-per-group.txt")).notToExist()
            }
        }
    }
})
