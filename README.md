[![Download](https://api.bintray.com/packages/robstoll/tutteli-jars/tutteli-spek-extensions/images/download.svg) ](https://bintray.com/robstoll/tutteli-jars/tutteli-spek-extensions/_latestVersion)
[![Apache license](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](http://opensource.org/licenses/Apache2.0)
[![Build Status Travis](https://travis-ci.org/robstoll/tutteli-spek-extensions.svg?tag=v0.5.0)](https://travis-ci.org/robstoll/tutteli-spek-extensions/branches)
[![Build status GitHub Actions](https://github.com/robstoll/tutteli-spek-extensions/workflows/Windows/badge.svg)](https://github.com/robstoll/tutteli-spek-extensions/actions/)
[![SonarCloud Status](https://sonarcloud.io/api/project_badges/measure?project=robstoll_tutteli-spek-extensions&metric=alert_status)](https://sonarcloud.io/dashboard?id=robstoll_tutteli-spek-extensions)
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=robstoll_tutteli-spek-extensions&metric=coverage)](https://sonarcloud.io/dashboard?id=robstoll_tutteli-spek-extensions)

# Tutteli spek extension
A set of [Spek](http://spekframework.org/) extensions such as [TempFolder](#tempfolder).

#Installation

*gradle*
```groovy
repositories {
    maven {
        url  "https://dl.bintray.com/robstoll/tutteli-jars" 
    }
}

dependencies {
    testImplementation 'ch.tutteli.spek:tutteli-spek-extensions:0.5.0'
}
```


## TempFolder
Provides, similar to TemporaryFolder in junit4, utility methods create temp files and folder and takes care of deleting them.

**Usage**

You have to register it in your Spek as follows:
```kotlin
object MySpec: Spek({
    val tempFolder = TempFolder.perTest() //or perGroup()
    registerListener(tempFolder)
    
    describe("...") {
        it ("...") {
            val file = tempFolder.newFile("test.txt")
        }
    }
})
```

There are a few other utility methods defined on `TempFolder`, e.g. `newFolder`.
Please open an issue if you want more or create a pull request.

In case you want to operate on `Path` we recommend using [Niok](https://github.com/robstoll/niok).

# License
tutteli-spek-extensions is licensed under [Apache 2.0](https://opensource.org/licenses/Apache2.0).
