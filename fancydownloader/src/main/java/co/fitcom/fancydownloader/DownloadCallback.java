/*
 * Created By Osei Fortune on 15/17/17 3:30 AM
 * Copyright (c) 2017 - 2018
 * Last modified 2/28/18 2:19 PM
 */

package co.fitcom.fancydownloader;


interface DownloadCallback{
    void onProgress(String task, long currentBytes, long totalBytes, long speed);
    void onComplete(String task);
    void onError(String task, Exception e);
}
