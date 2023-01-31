package com.sevenreup.fhir.server.sockets

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.*
import kotlin.collections.LinkedHashSet

class SocketTransport {
    private val connections: MutableSet<Connection> = Collections.synchronizedSet(LinkedHashSet())

    suspend fun handle(con: DefaultWebSocketServerSession, receive: (connect: Connection, data: String) -> String) {
        println("Adding user!")
        val thisConnection = Connection(con)
        connections += thisConnection
        try {
            con.send("You are connected! There are ${connections.count()} users here.")
            for (frame in con.incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText()
                val textWithUsername = "[${thisConnection.name}]: $receivedText"
                connections.forEach {
                    it.session.send(receive(it, receivedText))
                }
            }
        } catch (e: Exception) {
            println(e.localizedMessage)
        } finally {
            println("Removing $thisConnection!")
            connections -= thisConnection
        }
    }
}