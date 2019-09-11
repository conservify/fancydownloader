/*
 * Created By Osei Fortune on 15/17/17 3:30 AM
 * Copyright (c) 2017 - 2018
 * Last modified 12/15/17 3:30 AM
 */

package co.fitcom.fancydownloader;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public abstract class DownloadListenerUI extends DownloadListener {
    private Handler handler;
    private static final int WHAT_ERROR = 0x01;
    private static final int WHAT_PROGRESS = 0x02;
    private static final int WHAT_FINISH = 0x03;
    private static final int WHAT_HEADERS = 0x04;
    private static final String TASK = "task";
    private static final String CURRENT_BYTES = "currentBytes";
    private static final String TOTAL_BYTES = "totalBytes";
    private static final String SPEED = "speed";
    private static final String EXCEPTION = "exception";
    private static final String HEADERS = "headers";
    private static final String STATUS_CODE = "statusCode";


    private void ensureHandler() {
        if (handler != null) {
            return;
        }
        synchronized (DownloadListenerUI.class) {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        String task;
                        int statusCode;
                        HashMap<String, ArrayList<String>> headers;
                        switch (msg.what) {
                            case WHAT_ERROR:
                                Bundle errorData = msg.getData();
                                if (errorData == null) {
                                    return;
                                }
                                task = errorData.getString(TASK);
                                Exception exception = (Exception) errorData.getSerializable(EXCEPTION);
                                onUIError(task, exception);
                                break;
                            case WHAT_HEADERS:
                                Bundle headersData = msg.getData();
                                if (headersData == null) {
                                    return;
                                }
                                task = headersData.getString(TASK);
                                statusCode = headersData.getInt(STATUS_CODE);
                                headers = (HashMap<String, ArrayList<String>>)headersData.getSerializable(HEADERS);
                                onUIHeaders(task, statusCode, headers);
                                break;
                            case WHAT_PROGRESS:
                                Bundle progressData = msg.getData();
                                if (progressData == null) {
                                    return;
                                }
                                task = progressData.getString(TASK);
                                long currentByes = progressData.getLong(CURRENT_BYTES);
                                long totalByes = progressData.getLong(TOTAL_BYTES);
                                long speed = progressData.getLong(SPEED);
                                onUIProgress(task, currentByes, totalByes, speed);
                                break;
                            case WHAT_FINISH:
                                Bundle finishData = msg.getData();
                                if (finishData == null) {
                                    return;
                                }
                                task = finishData.getString(TASK);
                                statusCode = finishData.getInt(STATUS_CODE);
                                headers = (HashMap<String, ArrayList<String>>)finishData.getSerializable(HEADERS);
                                onUIComplete(task, statusCode, headers);
                                break;
                        }
                    }
                };
            }
        }
    }

    public final void onProgress(String task, long currentBytes, long totalBytes, long speed) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onUIProgress(task, currentBytes, totalBytes, speed);
            return;
        }
        ensureHandler();
        Message message = handler.obtainMessage();
        message.what = WHAT_PROGRESS;
        Bundle bundle = new Bundle();
        bundle.putString(TASK, task);
        bundle.putLong(CURRENT_BYTES, currentBytes);
        bundle.putLong(TOTAL_BYTES, totalBytes);
        bundle.putLong(SPEED, speed);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public final void onHeaders(String task, int statusCode, HashMap<String, ArrayList<String>> headers) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onUIHeaders(task, statusCode, headers);
            return;
        }
        ensureHandler();
        Message message = handler.obtainMessage();
        message.what = WHAT_HEADERS;
        Bundle bundle = new Bundle();
        bundle.putString(TASK, task);
        bundle.putInt(STATUS_CODE, statusCode);
        bundle.putSerializable(HEADERS, headers);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public final void onComplete(String task, int statusCode, HashMap<String, ArrayList<String>> headers) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onUIComplete(task, statusCode, headers);
            return;
        }
        ensureHandler();
        Message message = handler.obtainMessage();
        message.what = WHAT_FINISH;
        Bundle bundle = new Bundle();
        bundle.putString(TASK, task);
        bundle.putInt(STATUS_CODE, statusCode);
        bundle.putSerializable(HEADERS, headers);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public final void onError(String task, Exception e) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onUIError(task, e);
            return;
        }
        ensureHandler();
        Message message = handler.obtainMessage();
        message.what = WHAT_ERROR;
        Bundle bundle = new Bundle();
        bundle.putString(TASK, task);
        bundle.putSerializable(EXCEPTION, e);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public abstract void onUIProgress(String task, long currentBytes, long totalBytes, long speed);

    public abstract void onUIComplete(String task, int statusCode, HashMap<String, ArrayList<String>> headers);

    public abstract void onUIHeaders(String task, int statusCode, HashMap<String, ArrayList<String>> headers);

    public abstract void onUIError(String task, Exception e);

}
