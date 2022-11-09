/*
 * Copyright (C) 2018 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package okio.samples

import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import okio.HashingSink.Companion.sha256
import okio.HashingSource.Companion.sha256
import okio.Path
import okio.Path.Companion.toPath
import okio.blackholeSink
import okio.buffer
import okio.fakefilesystem.FakeFileSystem
import okio.use

class KotlinHashing {
  private val fileSystem = FakeFileSystem()

  fun run() {
    val path = "testFile.txt".toPath()
    val testContent = "Hello World"

    fileSystem.sink(path,true).use {sink->
      testContent.encodeToByteArray().also { bytes->
        Buffer().write(bytes).also {
          sink.write(it,it.size)
        }
      }
    }

    fileSystem.source(path).buffer().use {
      println("TEST CONTENT: ${it.readUtf8Line()}")
    }

    println("ByteString")
    val byteString = readByteString(path)
    println("       md5: " + byteString.md5().hex())
    println("      sha1: " + byteString.sha1().hex())
    println("    sha256: " + byteString.sha256().hex())
    println("    sha512: " + byteString.sha512().hex())
    println()

    println("Buffer")
    val buffer = readBuffer(path)
    println("       md5: " + buffer.md5().hex())
    println("      sha1: " + buffer.sha1().hex())
    println("    sha256: " + buffer.sha256().hex())
    println("    sha512: " + buffer.sha512().hex())
    println()

    println("HashingSource")
    sha256(fileSystem.source(path)).use { hashingSource ->
      hashingSource.buffer().use { source ->
        source.readAll(blackholeSink())
        println("    sha256: " + hashingSource.hash.hex())
      }
    }
    println()

    println("HashingSink")
    sha256(blackholeSink()).use { hashingSink ->
      hashingSink.buffer().use { sink ->
        fileSystem.source(path).use { source ->
          sink.writeAll(source)
          sink.close() // Emit anything buffered.
          println("    sha256: " + hashingSink.hash.hex())
        }
      }
    }
    println()

    println("HMAC")
    val secret = "7065616e7574627574746572".decodeHex()
    println("hmacSha256: " + byteString.hmacSha256(secret).hex())
    println()
  }

  private fun readByteString(path: Path): ByteString =fileSystem.read(path) { readByteString() }


  private fun readBuffer(path: Path): Buffer {
    return fileSystem.read(path) {
      val result = Buffer()
      readAll(result)
       result
    }
  }
}

fun main() {
  KotlinHashing().run()
}
