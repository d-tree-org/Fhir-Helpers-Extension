package com.sevenreup.fhir.server

import com.github.arteam.simplejsonrpc.server.JsonRpcServer
import com.sevenreup.fhir.server.sockets.SocketTransport
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

fun main(args: Array<String>) {
    var portNumber = 9090
    args.forEach {
        if (it.contains("port")) {
            portNumber = it.split("=").last().toIntOrNull() ?: portNumber
        }
    }
    val service = FhirService()
    val rpcServer = JsonRpcServer()

    embeddedServer(Netty, port = portNumber) {
        install(WebSockets)
        routing {
            val sock = SocketTransport()
            webSocket("/") {
                sock.handle(this) { _, data ->
                    rpcServer.handle(data, service)
                }
            }
            get("/") {
                print("er")
                val text = call.receiveText()
                call.respondText(rpcServer.handle(text, service))
            }
        }
    }.start(wait = true)
}