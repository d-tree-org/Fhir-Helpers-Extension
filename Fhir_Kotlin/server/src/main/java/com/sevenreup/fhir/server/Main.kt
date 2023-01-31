package com.sevenreup.fhir.server

import com.github.arteam.simplejsonrpc.server.JsonRpcServer
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*

fun main(args: Array<String>) {
    val service = FhirService()
    val rpcServer = JsonRpcServer()

    embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                print("er")
                val text = call.receiveText()
                call.respondText(rpcServer.handle(text, service))
            }
        }
    }.start(wait = true)
}