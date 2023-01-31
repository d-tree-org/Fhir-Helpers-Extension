package com.sevenreup.fhir.server

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcService

@JsonRpcService
class FhirService {
    @JsonRpcMethod
    fun compile(): String {
        return  "Compiling"
    }
}