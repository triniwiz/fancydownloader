/*
 * Created By Osei Fortune on 15/17/17 3:30 AM
 * Copyright (c) 2017 - 2018
 * Last modified 12/15/17 3:30 AM
 */

package co.fitcom.fancydownloader;

import java.util.HashMap;


public class Request {
    private HashMap<String, String> headers = new HashMap<>();
    private String url;
    private DownloadListener listener;
    public Request(String url) {
        this.url = url;
    }
    private String fileName;
    private String filePath;
    private int timeout = 60;

    public Request(String url, HashMap<String, String> headers) {
        this.url = url;
        this.headers = headers;
    }

    public Request(String url, HashMap<String, String> headers, DownloadListener listener) {
        this.url = url;
        this.headers = headers;
        this.listener = listener;
    }

    public void setTimeout(int timeout){
        this.timeout = timeout;
    }

    public int getTimeout(){
        return this.timeout;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getUrl() {
        return url;
    }

    public DownloadListener getListener() {
        return listener;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String path) {
        this.filePath = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public void setListener(DownloadListener listener) {
        this.listener = listener;
    }

    public void setFileName(String name) {
        this.fileName = name;
    }

    public void removeListener() {
        this.listener = null;
    }
}
