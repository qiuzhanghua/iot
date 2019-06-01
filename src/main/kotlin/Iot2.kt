package io.bx

import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.io.readUTF8Line
import kotlinx.coroutines.io.writeStringUtf8
import kotlinx.coroutines.launch
import mu.KotlinLogging

object Iot2 {

    private val logger = KotlinLogging.logger {}
    val DefaultPort = 8888

    fun start() {
        GlobalScope.launch {
            val serverSocket = aSocket(selectorManager).tcp().bind(port = DefaultPort)
            println("Echo Server Iot 2 listening at ${serverSocket.localAddress}")
            while (true) {
                val socket = serverSocket.accept()
                val socketAddress = socket.remoteAddress as java.net.InetSocketAddress
//                println("Remote as ${socketAddress.hostString}:${socketAddress.port}")
//                println(socket.remoteAddress.javaClass)
//                println(socket.localAddress)
//                println("Accepted $socket")
                launch {
                    val read = socket.openReadChannel()
                    val write = socket.openWriteChannel(autoFlush = true)
                    try {
                        while (true) {
                            val line = read.readUTF8Line() ?: break
                            println(line)
                            write.writeStringUtf8("$line\n")
                        }
                    } catch (e: Throwable) {
                        logger.error { "$e" }
                    } finally {
                        try {
                            socket.close()
                        } catch (e: Throwable) {

                        }
                    }
                }
            }
        }

    }


}