package com.sevenreup.fhir.core.tests.inputs


enum class PathResultType {
    ARRAY,
    STRING
}

data class PathResult(val type: PathResultType, val value: Any?)

enum class ValueTypes {
    String,
    Array,
    Range
}