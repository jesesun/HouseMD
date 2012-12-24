/*
 * Copyright 2013 zhongl
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.zhongl.housemd.duck

import instrument.Instrumentation
import akka.actor.{ActorSystem, IOManager}
import akka.actor.ActorDSL._
import akka.actor.IO._
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

/**
 * Doctor [[com.github.zhongl.housemd.duck.Cameron]] usually diagnose patient with [[com.github.zhongl.housemd.house.House]].
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Cameron(port: String, inst: Instrumentation) {

  def diagnose() {
    val loader = getClass.getClassLoader
    val config = loadConfigFrom(loader)

    implicit val system = ActorSystem("hospital", config, loader)

    actor("iPhone")(new Act {
      val handler = IOManager(context.system).connect("localhost", port.toInt)
      become {
        case Connected(socket, address) => println("Connected!")
        case Read(socket, bytes)        =>
          println(bytes.decodeString("UTF-8").trim)
          socket.asWritable.write(ByteString("Nop, bye!"))
          socket.asWritable.close()
        case Closed(socket, cause)      =>
          handler.close()
          context.system.shutdown()
      }
    })

  }

  private def loadConfigFrom(loader: ClassLoader) = {
    def load(name: String) = ConfigFactory.load(loader, name)

    val defaultConfig = load("default.conf")

    // TODO test it
    if (loader.getResource("housemd.conf") == null) defaultConfig
    else load("housemd.conf").withFallback(defaultConfig)
  }
}
