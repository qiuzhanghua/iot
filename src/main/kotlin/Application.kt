package io.bx

import io.ktor.network.selector.ActorSelectorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import java.net.InetSocketAddress
import io.ktor.network.sockets.Socket
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction


val selectorManager = ActorSelectorManager(Dispatchers.IO)

val socketCreatedChannel = Channel<Pair<InetSocketAddress, Socket>>()
val socketClosedChannel = Channel<Pair<InetSocketAddress, Socket>>()

object Messages : Table() {
    val id = integer("id").autoIncrement().primaryKey() // Column<Int>
    val msg = varchar("msg", 50) // Column<String>
}


fun main(args: Array<String>) = runBlocking<Unit>  {
    Database.connect("jdbc:mysql://localhost:3306/app", driver = "com.mysql.cj.jdbc.Driver", user = "app", password = "app")
    // com.mysql.jdbc.Driver
    transaction {
        SchemaUtils.create (Messages)
//        Messages.insert {
//            it[msg] = "first"
//        }
    }

        Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            println("Shutting ...")
            selectorManager.close()
        }
    })
    Iot.start()
    Iot2.start()

    while (true) {
        select<Unit> {
           socketCreatedChannel.onReceive {
               println("$it socket created")
           }
            socketClosedChannel.onReceive {
                println("$it socket closed")
            }
        }
    }
}
