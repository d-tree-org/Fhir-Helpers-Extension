package com.sevenreup.fhir.core.tests.inputs

object TestTypes {
    const val Equals = "eq"
    const val EqualsNoCase = "eqi"
    const val NotEquals = "ne"
    const val LessThan = "lt"
    const val LessThanOrEqual = "lte"
    const val GreaterThan = "gt"
    const val GreaterThanOrEqual = "gte"
    const val In = "in"
    const val NotIn = "notIn"
    const val Contains = "contains"
    const val NotContains = "notContains"
    const val ContainsNoCase = "containsi"
    const val NotContainsNoCase = "notContainsi"
    const val Null = "null"
    const val NotNull = "notNull"
    const val Between = "between"
    const val StartsWith = "startsWith"
    const val StartsWithNoCase = "startsWithi"
    const val EndsWith = "endsWith"
    const val EndsWithNoCase = "endsWithi"
}

object  DefaultTestTypes {
    const val Validation = "validation"
}

val testTypeNameMap = mapOf(
    Pair(TestTypes.Equals, "Equal"),
    Pair(TestTypes.EqualsNoCase, "Equal (case-insensitive)"),
    Pair(TestTypes.NotEquals, "Not equal"),
    Pair(TestTypes.LessThan, "Less than"),
    Pair(TestTypes.LessThanOrEqual, "Less than or equal to"),
    Pair(TestTypes.GreaterThan, "Greater than"),
    Pair(TestTypes.GreaterThanOrEqual, "Greater than or equal to"),
    Pair(TestTypes.In, "Included in an array"),
    Pair(TestTypes.NotIn, "Not included in an array"),
    Pair(TestTypes.Contains, "Contains"),
    Pair(TestTypes.NotContains, "Does not contain"),
    Pair(TestTypes.ContainsNoCase, "Contains (case-insensitive)"),
    Pair(TestTypes.NotContainsNoCase, "Does not contain (case-insensitive)"),
    Pair(TestTypes.Null, "Is null"),
    Pair(TestTypes.NotNull, "Is not null"),
    Pair(TestTypes.Between, "Is between"),
    Pair(TestTypes.StartsWith, "Starts with"),
    Pair(TestTypes.StartsWithNoCase, "Starts with (case-insensitive)"),
    Pair(TestTypes.EndsWith, "Ends with"),
    Pair(TestTypes.EndsWithNoCase, "Ends with (case-insensitive)"),
)

val defaultTypeNameMap = mapOf(Pair(DefaultTestTypes.Validation, "Resource Validation"))