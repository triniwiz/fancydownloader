package co.fitcom.fancydownloader;

/**
 * Created by triniwiz on 12/12/17.
 */


public interface DownloadResponseListener {

    void onProgress(String task, long currentBytes, long totalBytes);

    void onComplete(String task);

    void onError(String task);
}
