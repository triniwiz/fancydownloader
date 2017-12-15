package co.fitcom.fancydownloader;

import java.util.HashMap;

/**
 * Created by triniwiz on 12/12/17.
 */

public class Request {
    private HashMap<String, String> headers = new HashMap<>();
    private String url;
    private DownloadListener listener;

    public Request(String url) {
        this.url = url;
    }

    private String fileName;
    private String filePath;

    public Request(String url, HashMap<String, String> headers) {
        this.url = url;
        this.headers = headers;
    }

    public Request(String url, HashMap<String, String> headers, DownloadListener listener) {
        this.url = url;
        this.headers = headers;
        this.listener = listener;
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
