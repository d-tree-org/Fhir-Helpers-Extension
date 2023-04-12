package com.sevenreup.fhir.core.tests.inputs

import com.sevenreup.fhir.core.utils.parseDate
import java.math.BigDecimal
import java.time.LocalDateTime

data class ValueRange(val start: String, val end: String) {
    fun isNumber(): Pair<BigDecimal, BigDecimal>? {
        val startP = start.toBigDecimalOrNull()
        val endP = end.toBigDecimalOrNull()

        if (startP != null && endP != null) {
           return Pair(startP, endP)
        }
        return null
    }

    fun isDate(): Pair<LocalDateTime, LocalDateTime>? {
        val startD = parseDate(start, useIso = true)
        val endD = parseDate(end, useIso = true)

        if (startD != null && endD != null) {
            return Pair(startD, endD)
        }

        return null
    }
}