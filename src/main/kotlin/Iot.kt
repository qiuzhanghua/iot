package io.bx

import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object Iot {

    private val logger = KotlinLogging.logger {}
    val DefaultPort = 8585

    fun start() {
        logger.info { "hello from Iot" }
        GlobalScope.launch {
            val serverSocket = aSocket(selectorManager).tcp().bind(port = DefaultPort)
            println("Echo Server Iot 1 listening at ${serverSocket.localAddress}")
            while (true) {
                val socket: Socket = serverSocket.accept()
                val socketAddress = socket.remoteAddress as java.net.InetSocketAddress
//                println("Remote as ${socketAddress.hostString}:${socketAddress.port}")
//                println(socket.remoteAddress.javaClass)
//                println(socket.localAddress)
//                println("Accepted $socket")
                socketCreatedChannel.send(socketAddress to socket)
                launch {
                    val reader = socket.openReadChannel()
                    val writer = socket.openWriteChannel(autoFlush = true)
                    val readBytes = ByteArray(1024);
                    try {
                        while (true) {
                            val count = reader.readAvailable(readBytes)
                            println(count)
                            if (count == -1) {
                                println("closed")
                                break
                            }
                            if (count == 0) {
                                println("none")
                                break
                            }
                            println(readBytes[0])
                            transaction {
                                Messages.insert { it[msg] = String(readBytes.sliceArray(0 until count)).trim() }
                                //TODO exception
                            }
                            writer.writeFully(readBytes, 0, count)
//                            writer.writeByte('\n'.toByte())
//                            writer.flush()
//                            val line = reader.readUTF8Line() ?: break
//                            println(line)
//                            writer.writeStringUtf8("hello\n")
                        }
                    } catch (e: Throwable) {
                        logger.error { "$e" }
                    } finally {
                        try {
                            socketClosedChannel.send(socketAddress to socket)
                            socket.close()
                        } catch (e: Throwable) {
                            logger.error { "$e" }
                        }
                    }
                }
            }
        }

    }


}