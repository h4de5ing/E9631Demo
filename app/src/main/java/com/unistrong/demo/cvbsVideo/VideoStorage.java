package com.unistrong.demo.cvbsVideo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.util.Log;

import java.io.File;

public class VideoStorage {

    private static final String TAG = "VideoStorage";
    public static final String VIDEO_BASE_URI = "content://media/external/video/media";
    public static final int DELETE_MAX_TIMES = 5;
    public static final long LOW_STORAGE_THRESHOLD_BYTES = 100 * 1024 * 1024;//200M;
    public static final long VIDEO_FILE_MAX_SIZE = 100 * 1024 * 1024;//10M; //最好小于LOW_STORAGE_THRESHOLD_BYTES的大小
    public static final String fileRootPath = "/storage/card";//Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String saveVideoFilePath = fileRootPath + File.separator + Environment.DIRECTORY_DCIM + File.separator + "Camera";
    public static final int OUTPUTFORMAT = 8;

    public interface OnMediaSavedListener {
        void onMediaSaved(Uri uri);
    }

    public static long getStorageSpaceBytes() {
        File dir = new File(saveVideoFilePath);
        dir.mkdirs();
        if (!dir.isDirectory()) {
            Log.e(TAG, "DIR is not exist ");
            return 0;
        }
        if (!dir.canWrite()) {
            Log.e(TAG, "DIR can not be write");
            return 0;
        }
        try {
            StatFs stat = new StatFs(fileRootPath);
            long size = stat.getAvailableBlocks() * (long) stat.getBlockSize();
            Log.d(TAG, fileRootPath + ":getAvailableSpace=" + size / 1024 / 1024 + " M");
            //Log.d(TAG, fileRootPath + "getUsableSpace:" + (new File(fileRootPath).getUsableSpace()));
            return size;
        } catch (Exception e) {
            Log.e(TAG, "Fail to access external storage", e);
        }
        return 0;
    }

    private static int queryOldVideoFile(ContentResolver resolver, int format) {
        int videoId = -1;
        String columns[] = new String[]{Video.Media._ID, Video.Media.DATA, MediaColumns.DATE_MODIFIED};
        Uri uri = Uri.parse(VIDEO_BASE_URI);
        String select;
        if (format == OUTPUTFORMAT) {
            select = Video.Media.DATA + " like '" + saveVideoFilePath + "%.ts'";
        } else {
            select = Video.Media.DATA + " like '" + saveVideoFilePath + "%.mp4'";
        }

        Cursor cur = resolver.query(uri, columns, select, null, MediaColumns.DATE_MODIFIED + " ASC");

        if ((cur != null) && (cur.moveToFirst())) {
            videoId = cur.getInt(cur.getColumnIndex(Video.Media._ID));
            //String id = cur.getString(cur.getColumnIndex(MediaStore.Video.Media._ID));
            String path = cur.getString(cur.getColumnIndex(Video.Media.DATA));
            Log.d(TAG, "find path =" + path + " to delete");
            deleteVideoFile(path);
            //Log.d(TAG,"queryRecentVideoFile id=" + videoId);
            cur.close();
        }
        Log.d(TAG, "queryRecentVideoFile2222 id=" + videoId);
        return videoId;
    }

    public static void deleteVideoFile(String fileName) {
        Log.d(TAG, "Deleting video " + fileName);
        File f = new File(fileName);
        if (!f.delete()) {
            Log.v(TAG, "Could not delete " + fileName);
        }
    }

    private static void deleteVideoFile(ContentResolver resolver, int format) {
        Uri uri = Uri.parse(VIDEO_BASE_URI);
        int deleteId = -1;
        deleteId = queryOldVideoFile(resolver, format);
        if (deleteId > 0) {
            resolver.delete(uri, Video.Media._ID + "=?", new String[]{Integer.toString(deleteId)});
            Log.d(TAG, "delete " + deleteId + " succees");
        }
    }

    public static boolean storageSpaceIsAvailable(ContentResolver resolver, int format) {
        int deleteTimes = 0;
        if (getStorageSpaceBytes() > LOW_STORAGE_THRESHOLD_BYTES) {
            return true;
        }
        while (deleteTimes < DELETE_MAX_TIMES) {
            deleteVideoFile(resolver, format);
            deleteTimes++;
        }
        return true;
    }

    public static String convertOutputFormatToFileExt(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            return ".mp4";
        } else if (outputFileFormat == OUTPUTFORMAT) {
            return ".ts";
        }
        return ".3gp";
    }

    public static String convertOutputFormatToMimeType(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            return "video/mp4";
        } else if (outputFileFormat == OUTPUTFORMAT) {
            return "video/ts";
        }
        return "video/3gpp";
    }

    public static void addVideo(String path, long duration, ContentValues values, OnMediaSavedListener listener, ContentResolver resolver) {
        VideoSaveTask videoSave = new VideoSaveTask(path, duration, values, listener, resolver);
        videoSave.execute();
    }

    private static class VideoSaveTask extends AsyncTask<Void, Void, Uri> {

        private String path;
        private long duration;
        private ContentValues values;
        private OnMediaSavedListener listener;
        private ContentResolver resolver;

        public VideoSaveTask(String path, long duration, ContentValues values, OnMediaSavedListener listener, ContentResolver resolver) {
            this.path = path;
            this.duration = duration;
            this.values = new ContentValues(values);
            this.listener = listener;
            this.resolver = resolver;
        }

        public VideoSaveTask(String path) {
            this.path = path;
        }

        @Override
        protected Uri doInBackground(Void... arg0) {
            values.put(Video.Media.SIZE, new File(path).length());
            values.put(Video.Media.DURATION, duration);
            Uri uri = null;
            try {
                Uri videoTable = Uri.parse(VIDEO_BASE_URI);
                Log.d(TAG, "videoTable=" + videoTable);
                uri = resolver.insert(videoTable, values);
            } catch (Exception e) {
                Log.e(TAG, "failed to add video to media storage:" + e);
            }
            return uri;
        }


        @Override
        protected void onPostExecute(Uri result) {
            if (listener != null && result != null) {
                listener.onMediaSaved(result);
            }
        }
    }
}
