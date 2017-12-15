package co.fitcom.fancydownloader;

/**
 * Created by triniwiz on 12/12/17.
 */

interface DownloadCallback{
    void onProgress(String task, long currentBytes, long totalBytes, long speed);
    void onComplete(String task);
    void onError(String task, Exception e);
}
