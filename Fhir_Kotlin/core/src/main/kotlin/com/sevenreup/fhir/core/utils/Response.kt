package com.sevenreup.fhir.core.utils

data class CoreResponse<T>(val data: T? = null, val error: CoreException? = null)

data class CoreException(val m: String) : Exception(m)