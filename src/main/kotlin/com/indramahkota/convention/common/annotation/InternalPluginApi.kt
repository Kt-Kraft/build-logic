package com.indramahkota.convention.common.annotation

/**
 * This annotation is used to mark APIs that are internal to the plugin and are not intended to be used in client code.
 * The usage of these APIs in client code can lead to unpredictable results and is generally discouraged.
 *
 * The annotation has a target of CLASS, FUNCTION, and PROPERTY, meaning it can be used to annotate a class, a function, or a property.
 *
 * The RequiresOptIn level is set to ERROR. This means that the use of the annotated element will be reported as an error.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
public annotation class InternalPluginApi
