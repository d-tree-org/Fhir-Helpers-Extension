package com.sevenreup.fhir.core.utils

import java.time.LocalDateTime

fun parseDate(value: String?, useIso: Boolean = false): LocalDateTime? {
    return try {
        value?.let {
            var date = it
            if (it.contains("+")) {
                date = it.split("+").firstOrNull() ?: date
            }
            LocalDateTime.parse(
                date,
                // TODO: Fix this
               // if (useIso) DateTimeFormatter.ISO_LOCAL_DATE_TIME else DateTimeFormatter.ofPattern("dd-MM-yyyy")
            )
        }
    } catch (e: Exception) {
        null
    }
}