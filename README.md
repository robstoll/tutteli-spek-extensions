[![Download](https://api.bintray.com/packages/robstoll/tutteli-jars/tutteli-spek-extensions/images/download.svg) ](https://bintray.com/robstoll/tutteli-jars/tutteli-spek-extensions/_latestVersion)
[![Apache license](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](http://opensource.org/licenses/Apache2.0)
[![Build Status Travis](https://travis-ci.org/robstoll/tutteli-spek-extensions.svg?tag=v1.0.0)](https://travis-ci.org/robstoll/tutteli-spek-extensions/branches)
[![Build status GitHub Actions](https://github.com/robstoll/tutteli-spek-extensions/workflows/Windows/badge.svg)](https://github.com/robstoll/tutteli-spek-extensions/actions/)
[![SonarCloud Status](https://sonarcloud.io/api/project_badges/measure?project=robstoll_tutteli-spek-extensions&metric=alert_status)](https://sonarcloud.io/dashboard?id=robstoll_tutteli-spek-extensions)
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=robstoll_tutteli-spek-extensions&metric=coverage)](https://sonarcloud.io/dashboard?id=robstoll_tutteli-spek-extensions)

# Tutteli spek extension
A set of [Spek](http://spekframework.org/) extensions such as [TempFolder](#tempfolder).

#Installation

*gradle*
```groovy
repositories {
    jcenter()
    // or the following repo    
    //maven {
    //    url  "https://dl.bintray.com/robstoll/tutteli-jars" 
    //}
    
}

dependencies {
    testImplementation 'ch.tutteli.spek:tutteli-spek-extensions:1.0.0'
}
```
Use `tutteli-spek-extensions-android` in case you deal with android (does not contain a module-info.java which d8 cannot cope with).

## TempFolder
Provides, similar to TemporaryFolder in junit4, utility methods to create temp files and folders and takes care of deleting them.

**Usage**

Specify a `memoizedTempFolder` within a group like scope or on top of your spec
```kotlin
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object MySpec: Spek({
    
    describe("...") {
        val tempFolder = memoizedTempFolder()

        it ("...") {
            val file = tempFolder.newFile("test.txt")
        }
    }
})
```

Pass a `CachingMode` if required (see [Caching modes @ spekframework.org](https://www.spekframework.org/core-concepts/#caching-modes))
For instance: 
```kotlin
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.spekframework.spek2.lifecycle.CachingMode

object MySpec: Spek({
    
    describe("...") {
        val tempFolder = memoizedTempFolder(CachingMode.SCOPE)
        
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
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.spekframework.spek2.lifecycle.CachingMode

object MySpec: Spek({
    
    describe("...") {
        val tempFolder = memoizedTempFolder(CachingMode.TEST) {
            val f = newFolder("folderWithinTempFolder")
            newSymbolicLink("link", f)
        }
        
        it ("test1") {
            expect(tempFolder.resolve("folderWithinTempFolder")).exists()
            expect(tempFolder.resolve("link")).exists()   
        }    
    }
})
```

There are a few other utility methods defined on `MemoizedTempFolder`: `newFolder`, `newSymbolicLink` and `resolves`
Please open an issue if you want more or create a pull request.

In case you want to operate on `Path` we recommend using [Niok](https://github.com/robstoll/niok) and
for assertions use [Atrium](https://github.com/robstoll/atrium) with the jdk8 extension.

# License
tutteli-spek-extensions is licensed under [Apache 2.0](https://opensource.org/licenses/Apache2.0).
