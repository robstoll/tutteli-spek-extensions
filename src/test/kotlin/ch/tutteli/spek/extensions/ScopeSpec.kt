package ch.tutteli.spek.extensions

import ch.tutteli.atrium.verbs.expect
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
                expect(bla).existsNot()
            }
            it("it is recreated each time") {
                expect(tempFolder.resolve("bla-default.txt")).exists()
            }
        }
    }


    describe("memoized per test") {
        val tmpFolder by memoizedTempFolder(CachingMode.TEST) {
            newFile("bla.txt")
        }
        context("first context") {
            it("it exists at the beginning") {
                expect(tmpFolder.resolve("bla.txt")).exists()
            }
            it("it can be deleted") {
                val bla = tmpFolder.resolve("bla.txt")
                bla.delete()
                expect(bla).existsNot()
            }
            it("it is recreated each time") {
                val bla = tmpFolder.resolve("bla.txt")
                expect(bla).exists()
                bla.delete()
                expect(bla).existsNot()
            }
        }
        context("second context") {
            it("it is also recreated for the second context") {
                expect(tmpFolder.resolve("bla.txt")).exists()
            }
        }
    }

    describe("memoized per scope") {
        val tempFolder by memoizedTempFolder(CachingMode.SCOPE) {
            newFile("bla-per-scope.txt")
        }
        context("first context") {
            it("file from `memoized per test` does not exist") {
                expect(tempFolder.resolve("bla.txt")).existsNot()
            }
            it("it exists at the beginning") {
                expect(tempFolder.resolve("bla-per-scope.txt")).exists()
            }
            it("it can be deleted") {
                val bla = tempFolder.resolve("bla-per-scope.txt")
                bla.delete()
                expect(bla).existsNot()
            }
            it("it does not exist afterwards") {
                expect(tempFolder.resolve("bla-per-scope.txt")).existsNot()
            }
        }
        context("second context") {
            it("it does also not exist in a second scope") {
                expect(tempFolder.resolve("bla-per-scope.txt")).existsNot()
            }
        }
    }

    describe("memoized per group") {
        val tmpFolder by memoizedTempFolder(CachingMode.EACH_GROUP) {
            newFile("bla-per-group.txt")
        }
        context("first context") {
            it("file from `memoized per test` does not exist") {
                expect(tmpFolder.resolve("bla.txt")).existsNot()
            }
            it("file from `memoized per scope` does not exist") {
                expect(tmpFolder.resolve("bla-per-scope.txt")).existsNot()
            }
            it("it exists at the beginning") {
                expect(tmpFolder.resolve("bla-per-group.txt")).exists()
            }
            it("it can be deleted") {
                val bla = tmpFolder.resolve("bla-per-group.txt")
                bla.delete()
                expect(bla).existsNot()
            }
            it("it does not exist afterwards") {
                expect(tmpFolder.resolve("bla-per-group.txt")).existsNot()
            }
            context("nested context") {
                it("it is recreated in a nested context") {
                    expect(tmpFolder.resolve("bla-per-group.txt")).exists()
                }
                it("it can be deleted") {
                    val bla = tmpFolder.resolve("bla-per-group.txt")
                    bla.delete()
                    expect(bla).existsNot()
                }
                it("it does not exist afterwards") {
                    expect(tmpFolder.resolve("bla-per-group.txt")).existsNot()
                }
            }
        }
        context("second context") {
            it("it is recreated per context") {
                expect(tmpFolder.resolve("bla-per-group.txt")).exists()
            }
            it("it can be deleted") {
                val bla = tmpFolder.resolve("bla-per-group.txt")
                bla.delete()
                expect(bla).existsNot()
            }
            it("it does not exist afterwards") {
                expect(tmpFolder.resolve("bla-per-group.txt")).existsNot()
            }
        }
    }
})
