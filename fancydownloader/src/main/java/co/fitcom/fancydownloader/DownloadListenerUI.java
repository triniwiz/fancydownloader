package co.fitcom.fancydownloader;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.Serializable;

/**
 * Created by triniwiz on 12/13/17.
 */

public abstract class DownloadListenerUI extends DownloadListener {
    private Handler handler;
    private static final int WHAT_ERROR = 0x01;
    private static final int WHAT_PROGRESS = 0x02;
    private static final int WHAT_FINISH = 0x03;
    private static final String TASK = "task";
    private static final String CURRENT_BYTES = "currentBytes";
    private static final String TOTAL_BYTES = "totalBytes";
    private static final String SPEED = "speed";
    private static final String EXCEPTION = "exception";


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
                        switch (msg.what) {
                            case WHAT_ERROR:
                                Bundle errorData = msg.getData();
                                if(errorData == null){
                                    return;
                                }
                                task = errorData.getString(TASK);
                                Exception exception =  (Exception) errorData.getSerializable(EXCEPTION);
                                onUIError(task, exception);
                                break;
                            case WHAT_PROGRESS:
                                Bundle progressData = msg.getData();
                                if(progressData == null){
                                    return;
                                }
                                task = progressData.getString(TASK);
                                long currentByes = progressData.getLong(CURRENT_BYTES);
                                long totalByes = progressData.getLong(TOTAL_BYTES);
                                long speed = progressData.getLong(SPEED);
                                onUIProgress(task,currentByes,totalByes,speed);
                                break;
                            case WHAT_FINISH:
                                Bundle finishData = msg.getData();
                                if(finishData == null){
                                    return;
                                }
                                task = finishData.getString(TASK);
                                onUIComplete(task);
                                break;
                        }
                    }
                };
            }
        }
    }


    public void onProgress(String task, long currentBytes, long totalBytes, long speed) {
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

    public void onComplete(String task) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onUIComplete(task);
            return;
        }
        ensureHandler();
        Message message = handler.obtainMessage();
        message.what = WHAT_FINISH;
        Bundle bundle = new Bundle();
        bundle.putString(TASK, task);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public void onError(String task, Exception e) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onUIError(task,e);
            return;
        }
        ensureHandler();
        Message message = handler.obtainMessage();
        message.what = WHAT_ERROR;
        Bundle bundle = new Bundle();
        bundle.putString(TASK, task);
        bundle.putSerializable(EXCEPTION,e);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    abstract void onUIProgress(String task, long currentBytes, long totalBytes, long speed);

    abstract void onUIComplete(String task);

    abstract void onUIError(String task, Exception e);

}
