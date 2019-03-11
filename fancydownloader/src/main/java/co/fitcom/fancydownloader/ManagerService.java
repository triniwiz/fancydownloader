/*
 * Created By Osei Fortune on 15/17/17 3:30 AM
 * Copyright (c) 2017 - 2018
 * Last modified 2/28/18 12:16 PM
 */

package co.fitcom.fancydownloader;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class ManagerService extends Service {
    private Map<String, Task> tasks;
    private final IBinder mBinder = new ManagerBinder();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private Handler mHandler;
    private HandlerThread mThread;
    private static long mTimeout;
    class ManagerBinder extends Binder {
        ManagerService getService() {
            return ManagerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.tasks = new HashMap<>();
        mThread = new HandlerThread("Fancy Downloader", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public static void setTimeout(long timeout){
        mTimeout = timeout;
    }

    public String create(final Request request) {
        String id = this.generateId();
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        builder.tag(id);
        builder.url(request.getUrl());
        if (request.getHeaders().size() > 0) {
            builder.headers(Headers.of(request.getHeaders()));
        }
        if (request.getFileName() == null) {
            request.setFileName(URLUtil.guessFileName(request.getUrl(), null, null));
        }
        if (request.getFilePath() == null) {
            String path = getApplicationContext().getApplicationInfo().dataDir.concat("/fancy_downloader");
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdir();
            }
            request.setFilePath(path);
        }
        okhttp3.Request dRequest = builder
                .build();
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.connectTimeout(mTimeout, TimeUnit.SECONDS);
        clientBuilder.readTimeout(mTimeout,TimeUnit.SECONDS);
        clientBuilder.writeTimeout(mTimeout,TimeUnit.SECONDS);
        clientBuilder.authenticator(new Authenticator() {
            @Nullable
            @Override
            public okhttp3.Request authenticate(Route route, Response response) throws IOException {
                if(request.getHeaders().get("Authorization") == null){
                    return null;
                }
                if (response.request().header("Authorization") != null) {
                    return null;
                }

                return response.request().newBuilder()
                        .header("Authorization", request.getHeaders().get("Authorization"))
                        .build();

            }
        });
        Call call = clientBuilder.build().newCall(dRequest);
        this.tasks.put(id, new Task(dRequest, request, call));

        return id;
    }

    public void start(final String id) {
        final Task task = tasks.get(id);
        if (task != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final Request request = task.getDownloaderRequest();
                    final Call call = task.getCall();
                    if(call.isExecuted()) return;
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            if (!call.isCanceled()) {
                                handleIOException(request.getListener(), call.request().tag().toString(), e);
                            }
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull final Response response) {
                            DownloadListener listener = request.getListener();
                            String taskId = call.request().tag().toString();
                            if (!response.isSuccessful()) {
                                //Handle error
                                handleIOException(listener, taskId, new IOException());
                            } else {
                                //Handle success
                            ResponseBody responseBody = new DownloadResponseBody(id, response.headers(), response.body(), request.getListener());
                            BufferedSource bufferedSource = responseBody.source();
                            String originalName = request.getFileName();
                            String originalExt = originalName.substring(originalName.lastIndexOf("."));
                            String tempName = request.getFileName().replace(originalExt, ".tmp");
                            File file = new File(request.getFilePath(), tempName);
                            BufferedSink sink = null;





                            try {
                                sink = Okio.buffer(Okio.sink(file));
                                sink.writeAll(Okio.source(bufferedSource.inputStream()));

                                if (file.exists()) {
                                    File toMove = new File(request.getFilePath(), request.getFileName());
                                    boolean moved = file.renameTo(toMove);
                                    if (moved) {
                                        request.getListener().onComplete(id);
                                    }
                                }
                            } catch (FileNotFoundException e) {
                                handleIOException(listener, taskId, e);
                            } catch (IOException e) {
                                handleIOException(listener, taskId, e);
                            } finally {
                                if (sink != null) {
                                    try {
                                        sink.close();
                                    } catch (IOException e) {
                                        handleIOException(listener, taskId, e);
                                    }
                                }
                                if (bufferedSource != null) {
                                    try {
                                        bufferedSource.close();
                                    } catch (IOException e) {
                                        handleIOException(listener, taskId, e);
                                    }
                                }
                                responseBody.close();
                            }
                        }
                        }
                    });
                }
            });
        }
    }

    private void handleIOException(DownloadListener listener, String taskId, Exception e) {
        // Ignore when cancel is called;
        if (e.getLocalizedMessage() != null) {
            if (!e.getLocalizedMessage().contains("Socket closed") || !e.getLocalizedMessage().contains("CANCEL")) {
                if (listener != null) {
                    listener.onError(taskId, new Exception(e.getMessage(), e.getCause()));
                }
            }
        }
    }

    public void cleanUp() {
        cancelAll();
        stopSelf();
    }

    private void cancel(String id, Boolean delete) {
        Task task = tasks.get(id);
        Call call = task.getCall();
        if (call != null && !call.isCanceled() && call.isExecuted()) {
            call.cancel();
        }
        if (delete) {
            tasks.remove(id);
        }
    }

    public void cancel(String id) {
        cancel(id, true);
    }

    public void cancelAll() {
        for (String key : tasks.keySet()) {
            Task task = tasks.get(key);
            Call call = task.getCall();
            if(call != null && !call.isCanceled() && call.isExecuted()){
                call.cancel();
            }
            tasks.remove(key);
        }
    }

    public void pause(String id) {
        cancel(id, false);
    }

    public void pauseAll() {
        for (String key : tasks.keySet()) {
            Task task = tasks.get(key);
            Call call = task.getCall();
            if(call != null && !call.isCanceled() && call.isExecuted()){
                call.cancel();
            }
        }
    }

    public void resume(final String id) {
        final Task task = tasks.get(id);
        if (task != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final Request request = task.getDownloaderRequest();
                    String originalName = request.getFileName();
                    String originalExt = originalName.substring(originalName.lastIndexOf("."));
                    String tempName = request.getFileName().replace(originalExt,".tmp");
                    final File file = new File(request.getFilePath(), tempName);
                    okhttp3.Request okRequest = task.getOkRequest().newBuilder()
                            .header("Range", "bytes=" + file.length() + "-")
                            .build();
                    task.setOkRequest(okRequest);
                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    builder.connectTimeout(mTimeout, TimeUnit.SECONDS);
                    builder.readTimeout(mTimeout,TimeUnit.SECONDS);
                    builder.writeTimeout(mTimeout,TimeUnit.SECONDS);
                    builder.authenticator(new Authenticator() {
                        @Nullable
                        @Override
                        public okhttp3.Request authenticate(Route route, Response response) throws IOException {
                            if(request.getHeaders().get("Authorization") == null){
                                return null;
                            }
                            if (response.request().header("Authorization") != null) {
                                return null;
                            }

                            return response.request().newBuilder()
                                    .header("Authorization", request.getHeaders().get("Authorization"))
                                    .build();

                        }
                    });
                    final Call call = builder.build().newCall(okRequest);
                    task.setCall(call);
                    tasks.put(id, new Task(okRequest, request, call));
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            if (!call.isCanceled()) {
                                handleIOException(request.getListener(), id, e);
                            }
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull final Response response) {
                            ResponseBody responseBody = new DownloadResponseBody(id, response.headers(), response.body(), request.getListener());
                            BufferedSource bufferedSource = responseBody.source();
                            BufferedSink sink = null;
                            try {
                                sink = Okio.buffer(Okio.appendingSink(file));
                                sink.writeAll(Okio.source(responseBody.byteStream()));
                                if(file.exists()){
                                    File toMove = new File(request.getFilePath(),request.getFileName());
                                    boolean moved = file.renameTo(toMove);
                                    if(moved){
                                        request.getListener().onComplete(id);
                                    }
                                }
                            } catch (FileNotFoundException e) {
                                handleIOException(request.getListener(), id, e);
                            } catch (IOException e) {
                                handleIOException(request.getListener(), id, e);
                            } finally {

                                if (sink != null) {
                                    try {
                                        sink.close();
                                    } catch (IOException e) {
                                        handleIOException(request.getListener(), id, e);
                                    }
                                }
                                if (bufferedSource != null) {
                                    try {
                                        bufferedSource.close();
                                    } catch (IOException e) {
                                        handleIOException(request.getListener(), id, e);
                                    }
                                }
                                responseBody.close();
                            }
                        }
                    });
                }
            });
        }
    }

    public String generateId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

}
