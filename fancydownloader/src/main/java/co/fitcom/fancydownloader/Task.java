/*
 * Created By Osei Fortune on 15/17/17 3:30 AM
 * Copyright (c) 2017 - 2018
 * Last modified 12/15/17 3:30 AM
 */

package co.fitcom.fancydownloader;

import okhttp3.Call;

public class Task {
    private okhttp3.Request mOkRequest;
    private Request mDownloaderRequest;
    private Call mCall;

    Task(okhttp3.Request okRequest, Request downloaderRequest, Call call) {
        mOkRequest = okRequest;
        mDownloaderRequest = downloaderRequest;
        mCall = call;
    }

    public okhttp3.Request getOkRequest() {
        return mOkRequest;
    }

    public void setOkRequest(okhttp3.Request request){
        this.mOkRequest = request;
    }

    public Request getDownloaderRequest() {
        return mDownloaderRequest;
    }

    public void setDownloaderRequest( Request request){
        this.mDownloaderRequest = request;
    }

    public Call getCall() {
        return this.mCall;
    }

    public void setCall(Call call){
        this.mCall = call;
    }

}