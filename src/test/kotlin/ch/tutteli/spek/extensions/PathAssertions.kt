package ch.tutteli.spek.extensions

import ch.tutteli.atrium.api.cc.en_GB.*
import ch.tutteli.atrium.core.evalOnce
import ch.tutteli.atrium.creating.Assert
import ch.tutteli.atrium.domain.builders.AssertImpl
import ch.tutteli.atrium.reporting.translating.StringBasedTranslatable
import ch.tutteli.atrium.translations.DescriptionBasic
import java.nio.file.Files
import java.nio.file.Path

/**
 * TODO replace with method from Atrium as soon as it is provided
 */
fun Assert<Path>.exists(): Assert<Path> {
    val test = { Files.exists(subject) }.evalOnce()
    return addAssertion(
        AssertImpl.builder.feature
            .withDescriptionAndRepresentation(DescriptionPath.EXISTS, test)
            .withAssertion(AssertImpl.builder.createDescriptive(DescriptionBasic.IS, true, test))
            .build()
    )
}

/**
 * TODO replace with method from Atrium as soon as it is provided
 */
fun Assert<Path>.existsNot(): Assert<Path> {
    val test = { !Files.exists(subject) }.evalOnce()
    return addAssertion(
        AssertImpl.builder.feature
            .withDescriptionAndRepresentation(DescriptionPath.EXISTS_NOT, test)
            .withAssertion(AssertImpl.builder.createDescriptive(DescriptionBasic.IS, true, test))
            .build()
    )
}

/**
 * TODO replace with method from Atrium as soon as it is provided
 */
val Assert<Path>.name get() : Assert<String> {
    val subjectProvider = { subject.fileName.toString() }.evalOnce()
    return AssertImpl.feature.property(this, subjectProvider, subjectProvider, DescriptionPath.NAME)
}
val Assert<Path>.parent get(): Assert<Path>
    = returnValueOf(Path::getParent)


enum class DescriptionPath(override val value: String) : StringBasedTranslatable {
    EXISTS("exists"),
    EXISTS_NOT("does not exist"),
    NAME("name")
}
