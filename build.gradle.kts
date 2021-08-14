import java.net.URL

buildscript {
    // needs to be defined in here because otherwise tutteli-publish plugin does not have this information when applied
    rootProject.group = "ch.tutteli.spek"
    rootProject.version = "1.3.0-SNAPSHOT"
    rootProject.description = "A set of Spek extensions"
}

plugins {
    kotlin("jvm") version "1.5.21"
    id("org.jetbrains.dokka") version "1.5.0"
    val tutteliGradleVersion = "4.0.2"
    id("ch.tutteli.gradle.plugins.kotlin.module.info") version tutteliGradleVersion
    id("ch.tutteli.gradle.plugins.publish") version tutteliGradleVersion
    id("ch.tutteli.gradle.plugins.spek") version tutteliGradleVersion
    id("io.gitlab.arturbosch.detekt") version "1.18.0"
    id("org.sonarqube") version "3.3"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    // necessary because spek is an unnamed module, drop again in case https://github.com/spekframework/spek/issues/981 is resolved
    id("de.jjohannes.extra-java-module-info") version "0.9"
}

repositories {
    mavenCentral()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        //TODO change to jdk11 with 2.0.0
        jvmTarget = "1.6"

        // so that consumers of this library using 1.3 are still happy, we don't use specific features of 1.5
        //TODO change to 1.5 with 2.0.0
        apiVersion = "1.4"
        languageVersion = "1.4"
    }
}
val spekVersion by extra("2.0.16")
dependencies {
    api("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    testImplementation("ch.tutteli.atrium:atrium-fluent-en_GB:0.16.0")
    testImplementation("ch.tutteli.niok:niok:1.4.7")
}

// add module information for all direct and transitive dependencies that are not modules
extraJavaModuleInfo {
     failOnMissingModuleInfo.set(false)
     automaticModule("spek-dsl-jvm-$spekVersion.jar", "spek.dsl.jvm")
}

val docsDir = projectDir.resolve("docs/kdoc")
tasks.dokkaHtml.configure {
    outputDirectory.set(docsDir)
}
tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    dokkaSourceSets {
        configureEach {
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(URL("https://github.com/robstoll/${rootProject.name}/blob/master/src/main/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}

val dokka = tasks.register("dokka") {
    dependsOn(tasks.dokkaHtml)
}
tasks.register<Jar>("javaDoc") {
    archiveClassifier.set("javadoc")
    dependsOn(dokka)
    doFirst {
        from(docsDir)
    }
}

detekt {
    allRules = true
    config = files("${rootProject.projectDir}/gradle/scripts/detekt.yml")
    reports {
        xml.enabled = true
        html.enabled = false
        sarif.enabled = false
        txt.enabled = false
    }
}

sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "robstoll-github")
        property("sonar.projectKey", "robstoll_${rootProject.name}")
        property("sonar.projectVersion", rootProject.version)
        property("sonar.kotlin", "detekt.reportPaths=build/reports/detekt/detekt.xml")
        property("sonar.sources", "src/main/kotlin")
        property("sonar.tests", "src/test/kotlin")
        property("sonar.coverage", "jacoco.xmlReportPaths=build/reports/jacoco/report.xml")
        property("sonar.verbose", "true")
    }
}
tasks.named("sonarqube").configure {
    dependsOn(tasks.named("detekt"))
}

nexusPublishing {
    repositories {
        sonatype()
    }
}

/*

Release & deploy a commit
--------------------------------
export SPEK_EXTENSION_PREVIOUS_VERSION=1.2.1
export SPEK_EXTENSION_VERSION=1.3.0
find ./ -name "*.md" | xargs perl -0777 -i \
   -pe "s@$SPEK_EXTENSION_PREVIOUS_VERSION@$SPEK_EXTENSION_VERSION@g;" \
   -pe "s@tree/master@tree/v$SPEK_EXTENSION_VERSION@g;";
perl -0777 -i \
  -pe "s@$SPEK_EXTENSION_PREVIOUS_VERSION@$SPEK_EXTENSION_VERSION@g;" \
  -pe "s/rootProject.version = '$SPEK_EXTENSION_VERSION-SNAPSHOT'/rootProject.version = '$SPEK_EXTENSION_VERSION'/;" \
  ./build.gradle
perl -0777 -i \
  -pe "s@$SPEK_EXTENSION_PREVIOUS_VERSION@$SPEK_EXTENSION_VERSION@g;" \
  -pe 's/(<!-- for master -->\n)\n([\S\s]*?)(\n<!-- for a specific release -->\n)<!--\n([\S\s]*?)-->\n(\n# Tutteli spek extension)/$1<!--\n$2-->$3\n$4\n$5/;' \
  ./README.md
git commit -a -m "v$SPEK_EXTENSION_VERSION"

1. change search for X.Y.Z-SNAPSHOT and change to X.Y.Z
2. search for current version and replace with new
3. update master:
    a) point to the tag
        1) update badge versions
        2) search for `tree/master` and replace it with `tree/vX.Y.Z` (README.md)
    b) commit (modified build.gradle, README.md) c) git tag vX.Y.Z
    d) git push origin vX.Y.Z
4. deploy to bintray:
    a) java -version 2>&1 | grep "version \"11" && CI=true gr clean publishToBintray
    b) Log in to bintray, check that there are 10 artifacts and publish new jars
5. create release on github

Prepare next dev cycle
-----------------------

export SPEK_EXTENSION_VERSION=1.2.1
export SPEK_EXTENSION_NEXT_VERSION=1.3.0
find ./ -name "*.md" | xargs perl -0777 -i \
   -pe "s@tree/v$SPEK_EXTENSION_VERSION@tree/master@g;";
perl -0777 -i \
  -pe "s/rootProject.version = '$SPEK_EXTENSION_VERSION'/rootProject.version = '$SPEK_EXTENSION_NEXT_VERSION-SNAPSHOT'/;" \
  -pe "s/SPEK_EXTENSION_VERSION=$SPEK_EXTENSION_VERSION/SPEK_EXTENSION_VERSION=$SPEK_EXTENSION_NEXT_VERSION/;" \
  ./build.gradle
perl -0777 -i \
  -pe 's/(<!-- for master -->\n)<!--\n([\S\s]*?)-->(\n<!-- for a specific release -->)\n([\S\s]*?)\n(\n# Tutteli spek extension)/$1\n$2$3\n<!--$4-->\n$5/;' \
  ./README.md
git commit -a -m "prepare dev cycle of $SPEK_EXTENSION_NEXT_VERSION"

1. change version in build.gradle to X.Y.Z-SNAPSHOT
2. point to master
   a) search for `tag=vX.Y.Z` and replace it with `branch=master`
   b) search for `tree/vX.Y.Z` and replace it with `tree/master`
3. commit & push changes

*/
