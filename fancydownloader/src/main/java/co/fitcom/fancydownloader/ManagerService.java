package co.fitcom.fancydownloader;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.webkit.URLUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import okhttp3.*;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by triniwiz on 12/12/17.
 */

public class ManagerService extends Service {
    private Map<String, Task> tasks;
    private final IBinder mBinder = new ManagerBinder();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private OkHttpClient client = new OkHttpClient();
    private Handler mHandler;
    private HandlerThread mThread;

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

    public String create(Request request) {
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
        Call call = client.newCall(dRequest);
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
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            if (!call.isCanceled()) {
                                handleIOException(request.getListener(), call.request().tag().toString(), e);
                            }
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull final Response response) {
                            ResponseBody responseBody = new DownloadResponseBody(id, response.headers(), response.body(), request.getListener());
                            BufferedSource bufferedSource = responseBody.source();
                            File file = new File(request.getFilePath(), request.getFileName());
                            BufferedSink sink = null;
                            DownloadListener listener = request.getListener();
                            String taskId = call.request().tag().toString();
                            try {
                                sink = Okio.buffer(Okio.sink(file));
                                sink.writeAll(Okio.source(bufferedSource.inputStream()));
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
        if (task != null) {
            if (task.getCall().isExecuted()) {
                task.getCall().cancel();
            }
            if (delete) {
                tasks.remove(id);
            }
        } else {
            for (Call call : client.dispatcher().runningCalls()) {
                if (call.request().tag().equals(id)) {
                    call.cancel();
                    break;
                }
            }
        }
    }

    public void cancel(String id) {
        cancel(id, true);
    }

    public void cancelAll() {
        client.dispatcher().cancelAll();
        for (String key : tasks.keySet()) {
            tasks.remove(key);
        }
    }

    public void pause(String id) {
        cancel(id, false);
    }

    public void pauseAll() {
        if (client != null) {
            client.dispatcher().cancelAll();
        }
    }

    public void resume(final String id) {
        final Task task = tasks.get(id);
        if (task != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final Request request = task.getDownloaderRequest();
                    final File file = new File(request.getFilePath(), request.getFileName());
                    okhttp3.Request okRequest = task.getOkRequest().newBuilder()
                            .header("Range", "bytes=" + file.length() + "-")
                            .build();
                    task.setOkRequest(okRequest);
                    final Call call = client.newCall(okRequest);
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
