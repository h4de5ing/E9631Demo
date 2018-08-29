package com.unistrong.uarttest

import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by luowei on 2017/9/14.
 *
 */
abstract class BaseIO {
    companion object {
        val DEBUG = false
    }

    private var isRun: Boolean = false
    private val executor = ThreadPoolExecutor(3, 10, 5, TimeUnit.SECONDS, LinkedBlockingQueue())

    fun start(inputStream: InputStream, outputStream: OutputStream, callback: (buffer: ByteArray, size: Int) -> Unit) {
        isRun = true
        readThread = startReadThread(inputStream, callback)
        writeThread = startWriteThread(outputStream, readThread!!)
    }

    private var readThread: ReadThread? = null
    private var writeThread: WriteThread? = null
    open fun stop() {
        isRun = false
        readThread?.close()
//        readThread?.interrupt()
        readThread = null
        writeThread?.close()
        writeThread = null

    }

    fun write(packet: Packet): Boolean = isRun && (writeThread?.write(packet) == true)

    private fun startWriteThread(outputStream: OutputStream, readThread: ReadThread): WriteThread {
        val writeThread = WriteThread(outputStream, readThread)
        executor.execute(writeThread)
        return writeThread
    }

    private fun startReadThread(inputStream: InputStream, callback: (buffer: ByteArray, size: Int) -> Unit): ReadThread {
        val readThread = ReadThread(inputStream, callback)
        executor.execute(readThread)
        return readThread
    }

    inner class WriteThread(private val outputStream: OutputStream,
                            private val readThread: ReadThread) : Thread() {
        private val queen = LinkedList<Packet>()
        private val objecz = Object()

        init {
            readThread.setWriteThread(this)
        }

        override fun run() {
            if (DEBUG) Log.d("start write..")
            while (isRun) {
                while (queen.isEmpty()) {
                    synchronized(objecz) {
                        if (DEBUG) Log.d("wait")
                        objecz.wait()
                    }
                }
                val poll = queen.poll()
                try {
                    write(poll.buffer)
                    var buffer: ByteArray? = null
                    if (poll.callback != null) {
                        buffer = readThread.get(2000)
                    }
                    poll.callback?.invoke(buffer != null, if (buffer != null) buffer else poll.buffer)
                } catch (e: Exception) {
                    if(DEBUG) Log.e("写线程异常${e.printStackTrace()}")
                    this@BaseIO.stop()
                }
            }
            if (DEBUG) Log.d("start write finish")
        }

        @Synchronized
        private fun write(buffer: ByteArray) {
            if (DEBUG) Log.d("write ${buffer.size}")
            outputStream.write(buffer)
        }

        fun write(packet: Packet): Boolean {
            var rt = false
            if (queen.size < 10) {
                queen.offer(packet)
                rt = true
            }
            synchronized(objecz) {
                objecz.notify()
            }
            return rt
        }

        fun close() {
//            outputStream.close()
            write(Packet(ByteArray(0)))
            interrupt()
        }
    }

    class Packet(val buffer: ByteArray,
                 val callback: ((success: Boolean, buffer: ByteArray) -> Unit)? = null)


    inner class ReadThread(private val inputStream: InputStream,
                           private val callback: (buffer: ByteArray, size: Int) -> Unit) : Thread() {
        private val readBuffer = ByteArray(1024)
        private var readSize = 0
        private val objecz = Object()

        override fun run() {
            if (DEBUG) Log.d("start read..")
            while (isRun) {
                try {
                    if (DEBUG) Log.d("read wait")
                    readSize = inputStream.read(readBuffer)
                    if (readSize > 0) {
                        synchronized(objecz) {
                            objecz.notify()
                        }
                        if (DEBUG) Log.d("read $readSize")
                        callback.invoke(readBuffer, readSize)
                    } else {
//                         if(DEBUG)Log.d("read size <=0")
                    }
                } catch (e: Exception) {
                    if(DEBUG) Log.e("读线程异常${e.printStackTrace()}")
                    this@BaseIO.stop()
                }
            }
            if (DEBUG) Log.d("start read finish")
        }

        fun get(timeout: Long): ByteArray? {
            if (readSize <= 0) {
                synchronized(objecz) {
                    objecz.wait(timeout)
                }
            }
            if (readSize > 0)
                return readBuffer.copyOfRange(0, readSize)
            return null
        }

        private lateinit var writeThread: WriteThread

        fun setWriteThread(writeThread: WriteThread) {
            this.writeThread = writeThread
        }

        fun close() {
//            inputStream.close()
            interrupt()
        }

    }
}