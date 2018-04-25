/*
 *  Copyright (C)  2016 android@19code.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.unistrong.demo.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;


/**
 * Created by Gh0st on 2016/6/7 007.
 * https://github.com/ZhaoKaiQiang/KLog
 */
public class L {
    private static String TAG = "gh0st";
    private static boolean LOG_DEBUG = false;
    private static boolean LOG_TOAST = false;
    private static final int VERBOSE = 2;
    private static final int DEBUG = 3;
    private static final int INFO = 4;
    private static final int WARN = 5;
    private static final int ERROR = 6;
    private static final int ASSERT = 7;

    public static void init(boolean isDebug, String tag) {
        TAG = tag;
        LOG_DEBUG = isDebug;
        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "debug.txt");
        if (file.exists()) {
            LOG_DEBUG = true;
        }
        File toast = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "toast.txt");
        if (toast.exists()) {
            LOG_TOAST = true;
        }
    }

    static void showToast(Context context, String msg) {
        if (LOG_TOAST) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
    }

    public static void v(String msg) {
        log(VERBOSE, null, msg);
    }

    public static void v(String tag, String msg) {
        log(VERBOSE, tag, msg);
    }

    public static void d(String msg) {
        log(DEBUG, null, msg);
    }

    public static void d(String tag, String msg) {
        log(DEBUG, tag, msg);
    }

    public static void i(Object... msg) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : msg) {
            sb.append(obj);
            sb.append(",");
        }
        log(INFO, null, String.valueOf(sb));
    }

    public static void w(String msg) {
        log(WARN, null, msg);
    }

    public static void w(String tag, String msg) {
        log(WARN, tag, msg);
    }

    public static void e(String msg) {
        log(ERROR, null, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        log(ERROR, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    public static void a(String msg) {
        log(ASSERT, null, msg);
    }

    public static void a(String tag, String msg) {
        log(ASSERT, tag, msg);
    }

    private static void log(int logType, String tagStr, Object objects) {
        String[] contents = wrapperContent(tagStr, objects);
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];
        if (LOG_DEBUG) {
            switch (logType) {
                case VERBOSE:
                case DEBUG:
                case INFO:
                case WARN:
                case ERROR:
                case ASSERT:
                    printDefault(logType, tag, headString + msg);
                    break;
            }
        }
    }

    private static void printDefault(int type, String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        printLine(tag, true);
        int maxLength = 4000;
        int countOfSub = msg.length();
        if (countOfSub > maxLength) {
            for (int i = 0; i < countOfSub; i += maxLength) {
                if (i + maxLength < countOfSub) {
                    printSub(type, tag, msg.substring(i, i + maxLength));
                } else {
                    printSub(type, tag, msg.substring(i, countOfSub));
                }
            }
        } else {
            printSub(type, tag, msg);
        }
        printLine(tag, false);
    }

    private static void printSub(int type, String tag, String msg) {
        switch (type) {
            case VERBOSE:
                Log.v(tag, msg);
                break;
            case DEBUG:
                Log.d(tag, msg);
                break;
            case INFO:
                Log.i(tag, msg);
                break;
            case WARN:
                Log.w(tag, msg);
                break;
            case ERROR:
                Log.e(tag, msg);
                break;
            case ASSERT:
                Log.wtf(tag, msg);
                break;
        }
    }


    private static String[] wrapperContent(String tag, Object... objects) {
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement targetElement = stackTrace[5];
        String className = targetElement.getClassName();
        String[] classNameInfo = className.split("\\.");
        if (classNameInfo.length > 0) {
            className = classNameInfo[classNameInfo.length - 1] + ".java";
        }
        String methodName = targetElement.getMethodName();
        int lineNumber = targetElement.getLineNumber();
        if (lineNumber < 0) {
            lineNumber = 0;
        }
        String methodNameShort = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
        String msg = (objects == null) ? "Log with null object" : getObjectsString(objects);
        String headString = "[(" + className + ":" + lineNumber + ")#" + methodNameShort + " ] ";
        return new String[]{tag, msg, headString};
    }

    private static String getObjectsString(Object... objects) {

        if (objects.length > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n");
            for (int i = 0; i < objects.length; i++) {
                Object object = objects[i];
                if (object == null) {
                    stringBuilder.append("param").append("[").append(i).append("]").append(" = ").append("null").append("\n");
                } else {
                    stringBuilder.append("param").append("[").append(i).append("]").append(" = ").append(object.toString()).append("\n");
                }
            }
            return stringBuilder.toString();
        } else {
            Object object = objects[0];
            return object == null ? "null" : object.toString();
        }
    }

    private static void printLine(String tag, boolean isTop) {
        if (isTop) {
            Log.d(tag, "----------------------------------------------------------------------------------");
        } else {
            Log.d(tag, "----------------------------------------------------------------------------------");
        }
    }
}
