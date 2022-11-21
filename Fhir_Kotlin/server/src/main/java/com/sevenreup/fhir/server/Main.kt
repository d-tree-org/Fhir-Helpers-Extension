package com.sevenreup.fhir.server

import com.github.arteam.simplejsonrpc.server.JsonRpcServer


fun main(args: Array<String>) {
    val service = FhirService()
    val server = JsonRpcServer()
    server.
}