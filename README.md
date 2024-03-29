<!-- for master -->

[![Download](https://img.shields.io/badge/Download-1.2.1-%23007ec6)](https://search.maven.org/artifact/ch.tutteli.tutteli-spek-extensions/tutteli-spek-extensions/1.2.1/jar)
[![Apache license](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](http://opensource.org/licenses/Apache2.0)
[![Build Status Ubuntu](https://github.com/robstoll/tutteli-spek-extensions/workflows/Ubuntu/badge.svg?event=push)](https://github.com/robstoll/tutteli-spek-extensions/actions?query=workflow%3AUbuntu+branch%3Amaster)
[![Build Status Windows](https://github.com/robstoll/tutteli-spek-extensions/workflows/Windows/badge.svg?event=push)](https://github.com/robstoll/tutteli-spek-extensions/actions?query=workflow%3AWindows+branch%3Amaster)
[![SonarCloud Status](https://sonarcloud.io/api/project_badges/measure?project=robstoll_tutteli-spek-extensions&metric=alert_status)](https://sonarcloud.io/dashboard?id=robstoll_tutteli-spek-extensions)
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=robstoll_tutteli-spek-extensions&metric=coverage)](https://sonarcloud.io/dashboard?id=robstoll_tutteli-spek-extensions)

<!-- for a specific release -->
<!--
[![Download](https://img.shields.io/badge/Download-1.2.1-%23007ec6)](https://search.maven.org/artifact/ch.tutteli.tutteli-spek-extensions/tutteli-spek-extensions/1.2.1/jar)
[![Apache license](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](http://opensource.org/licenses/Apache2.0)
-->

# Tutteli spek extension
A set of [Spek](http://spekframework.org/) extensions such as [MemoizedTempFolder](#MemoizedTempFolder).

# Installation

tutteli-spek-extensions is published to maven central.

```
repositories { mavenCentral() }
dependencies {
    testImplementation("ch.tutteli.spek:tutteli-spek-extensions:1.2.1")
}
```

# Features

## MemoizedTempFolder

`memoizedTempFolder` provides -- similar to TemporaryFolder in junit4 -- utility methods to create temp files and folders and takes care of deleting them.

Specify a `memoizedTempFolder` within a group like scope near to the test you are going to use the tempFolder (default `CachingMode` is per `TEST`, so each test gets its own temporary directory)

```kotlin
import memoizedTempFolder
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object MySpec: Spek({
    
    describe("...") {
        val tempFolder by memoizedTempFolder()

        it ("...") {
            val file = tempFolder.newFile("test.txt")
        }
    }
})
```

Pass a `CachingMode` if required (see [Caching modes @ spekframework.org](https://www.spekframework.org/core-concepts/#caching-modes))
For instance: 
```kotlin
import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.verbs.expect
import memoizedTempFolder
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.spekframework.spek2.lifecycle.CachingMode

object MySpec: Spek({
    
    describe("...") {
        val tempFolder by memoizedTempFolder(CachingMode.SCOPE)
        
        it ("test1") {
            val file = tempFolder.newFile("test.txt")
            file.delete()
        }
        it("test2"){
            expect(file).exists() // would fail
        }       
    }
})
```
And you can use the second argument of `memoizedTempFolder` for additional setup:


```kotlin
import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.verbs.expect
import memoizedTempFolder
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.spekframework.spek2.lifecycle.CachingMode

object MySpec: Spek({
    
    describe("...") {
        val tempFolder by memoizedTempFolder(CachingMode.TEST) {
            val f = newDirectory("folderWithinTempFolder")
            newSymbolicLink("link", f)
        }
        
        it ("test1") {
            expect(tempFolder.resolve("folderWithinTempFolder")).exists()
            expect(tempFolder.resolve("link")).exists()   
        }    
    }
})
```

There are a few other utility methods defined on `MemoizedTempFolder`: `newDirectory`, `newSymbolicLink`, 
`resolves` and `withinTmpDir`.

Tutteli spek extension works best in combination with [Niok](https://github.com/robstoll/niok)
which enhances `Path` with methods like `createDirectories`, `setAttribute`, `writeLines` and many more (not only useful in tests but also in production code).
With Niok in place, more complicated setup can be defined easily:
```kotlin
import memoizedTempFolder
import ch.tutteli.niok.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.spekframework.spek2.lifecycle.CachingMode

object MySpec: Spek({
    
    describe("...") {
        val tempFolder by memoizedTempFolder(CachingMode.TEST) {
            withinTmpDir {
                val subDir = resolve("dir1/dir2/dir3").createDirectories()
                subDir.resolve("a.txt").writeLines(listOf("a", "b", "c"))
            }
        }
    }
})
```
And if you like to assert certain properties of a Path, then we recommend using [Atrium](https://github.com/robstoll/atrium).

# License
tutteli-spek-extensions is licensed under [Apache 2.0](https://opensource.org/licenses/Apache2.0).
