package com.sevenreup.fhir.core.utils

fun String.replaceTemplate(values: Map<String, String>): String {
    var string = this
    values.forEach { (key, value) ->
        string = string.replace("{{$key}}", value)
    }
    return string
}