[![Apache license](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](http://opensource.org/licenses/Apache2.0)

# Tutteli spek extension
A set of [Spek](http://spekframework.org/) extensions such as [TempFolder](#tempfolder).

## TempFolder
Provides, similar to TemporaryFolder in junit4, utility methods create temp files and folder and takes care of deleting them.

**Usage**

You have to register it in your Spek as follows:
```kotlin
object MySpec: Spek({
    val tempFolder = TempFolder()
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
