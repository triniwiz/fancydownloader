package co.fitcom.fancydownloader;

import java.util.HashMap;

/**
 * Created by triniwiz on 12/12/17.
 */

public class Request{
    private HashMap<String,String> headers = new HashMap<>();
    private String url;
    private DownloadListener listener;
    public Request(String url){
        this.url = url;
    }
    private String fileName;
    private String filePath;
    public Request(String url,HashMap<String,String> headers){
        this.url = url;
        this.headers = headers;
    }
    public Request(String url,HashMap<String,String> headers,DownloadListener listener){
        this.url = url;
        this.headers = headers;
        this.listener = listener;
    }
    public void addHeader(String key,String value){
        this.headers.put(key,value);
    }
    HashMap<String,String> getHeaders(){
        return this.headers;
    }
    public void setHeaders(HashMap<String,String> headers){
        this.headers = headers;
    }
    String getUrl(){
        return this.url;
    }
    public void setUrl(String url){
        this.url = url;
    }
    public void setListener(DownloadListener listener){
        this.listener = listener;
    }
    DownloadListener getListener(){
        return this.listener;
    }
    public void removeListener(){
        this.listener = null;
    }
    public String getFileName(){
        return fileName;
    }
    void setFileName(String name){
        fileName = name;
    }
    public String getFilePath(){
        return filePath;
    }
    void setFilePath(String path){
        filePath = path;
    }
}
