package server

import com.github.arteam.simplejsonrpc.server.JsonRpcServer
import server.sockets.Connection
import server.sockets.SocketTransport
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import java.util.*

fun main(args: Array<String>) {
    val service = FhirService()
    val rpcServer = JsonRpcServer()

    embeddedServer(Netty, port = 8080) {
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