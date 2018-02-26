[![Apache license](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](http://opensource.org/licenses/Apache2.0)
[![Build Status](https://travis-ci.org/robstoll/tutteli-spek-extensions.svg?brach=master)](https://travis-ci.org/robstoll/tutteli-spek-extensions/branches)
[![Coverage](https://codecov.io/github/robstoll/tutteli-spek-extensions/coverage.svg?brach=master)](https://codecov.io/github/robstoll/tutteli-spek-extensions?brach=master)


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
    testCompile 'ch.tutteli:tutteli-spek-extensions:0.1.0'
}
```


## TempFolder
Provides, similar to TemporaryFolder in junit4, utility methods create temp files and folder and takes care of deleting them.

**Usage**

You have to register it in your Spek as follows:
```kotlin
object MySpec: Spek({
    val tempFolder = TempFolder.perTest() //or perAction() or perGroup()
    registerListener(tempFolder)
    
    it ("..."){
        val file = tempFolder.newFile("test.txt")
    }
})
```

There are a few other utility methods defined on `TempFolder`, e.g. `newFolder`.
Please open an issue if you want more or create a pull request.

# License
tutteli-spek-extensions is published under [Apache 2.0](http://opensource.org/licenses/Apache2.0).
