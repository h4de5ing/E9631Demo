package com.unistrong.uarttest

import android.util.Log


/**
 * 自定义日志类,输出日志格式[线程名+文件名+日志行号]
 */
object Log {

    private val functionName: String
        get() {
            val sts = Thread.currentThread().stackTrace ?: return ""

            for (st in sts) {
                if (st.isNativeMethod) {
                    continue
                }

                if (st.className == Thread::class.java.name) {
                    continue
                }

                if (st.className == javaClass.name) {
                    continue
                }
                return "[" + Thread.currentThread().name + "] " + " (" + st.fileName + ":" + st.lineNumber + ") " + st.methodName + "()"
            }

            return ""
        }


    private val TAG = "日志"

    /**
     * Priority constant for the println method; use Log.v.
     */
    val VERBOSE = 2

    /**
     * Priority constant for the println method; use Log.d.
     */
    val DEBUG = 3

    /**
     * Priority constant for the println method; use Log.i.
     */
    val INFO = 4

    /**
     * Priority constant for the println method; use Log.w.
     */
    val WARN = 5

    /**
     * Priority constant for the println method; use Log.e.
     */
    val ERROR = 6

    /**
     * Priority constant for the println method.
     */
    val ASSERT = 7
    val NONE = 8

    // 日志打印等级
    var logLevel = 0

//    init {
//        //release不打印日志信息
//        if (!BuildConfig.DEBUG) {
//            logLevel = NONE
//        }
//    }


    fun w(message: String, vararg args: Any) {
        if (logLevel <= WARN) {
            val name = functionName
            var str = createMessage(message, *args)
            str = String.format("%s:%s", name, str)
            Log.w(TAG, str)
        }
    }


    fun e(message: String, vararg args: Any) {
        if (logLevel <= ERROR) {
            var str: String
            val name = functionName
            str = createMessage(message, *args)
            str = String.format("%s:%s", name, str)
            Log.e(TAG, str)
        }
    }

    fun e(ex: Throwable) {
        e(ex.toString())
    }

    fun d(message: String = "", vararg args: Any) {
        if (logLevel <= Log.DEBUG) {
            val str = createMessage2(message, *args)
            Log.d(TAG, str)
        }
    }

    @Deprecated("use {@linkplain #createMessage2(String, Object...)}", ReplaceWith("if (args.isEmpty()) message else String.format(message, *args)"))
    private fun createMessage(message: String, vararg args: Any): String {
        return if (args.isEmpty()) message else String.format(message, *args)
    }

    fun createMessage2(message: String, vararg args: Any): String {
        var str = if (args.isEmpty()) message else String.format(message, *args)
        val name = functionName
        str = String.format("%s:%s", name, str)
        return str
    }
}

