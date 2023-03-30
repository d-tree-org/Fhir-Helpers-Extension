package com.sevenreup.fhir.core.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun parseDate(value: String?, useIso: Boolean = false): LocalDateTime? {
    return try {
        value?.let {
            LocalDateTime.parse(
                it,
                if (useIso) DateTimeFormatter.ISO_LOCAL_DATE_TIME else DateTimeFormatter.ofPattern("dd-MM-yyyy")
            )
        }
    } catch (e: Exception) {
        null
    }
}