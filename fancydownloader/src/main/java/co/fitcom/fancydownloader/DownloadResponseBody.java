package co.fitcom.fancydownloader;

import android.support.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by triniwiz on 12/12/17.
 */

public class DownloadResponseBody extends ResponseBody {
    private final ResponseBody responseBody;
    private BufferedSource bufferedSource;
    private final DownloadListener listener;
    private final Object tag;
    private long skipBytes = 0;
    private Headers headers;

    DownloadResponseBody(Object tag, Headers headers, ResponseBody responseBody, DownloadListener listener) {
        this.responseBody = responseBody;
        this.listener = listener;
        this.tag = tag;
        this.headers = headers;
    }

    DownloadResponseBody(Object tag, Headers headers, ResponseBody responseBody, DownloadListener listener, long skipBytes) {
        this.responseBody = responseBody;
        this.listener = listener;
        this.tag = tag;
        this.skipBytes = skipBytes;
        this.headers = headers;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {

        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        String range = headers.get("Content-Range");
        if(range != null){
            skipBytes = Integer.valueOf(range.replace("bytes ","").substring(0,range.indexOf("-") - 6));
        }

        return new ForwardingSource(source) {
            long totalBytesRead = 0;
            long skippedBytes = 0;
            String task = tag.toString();
            long length = contentLength();
            long skippedLength = length += skipBytes;
            @Override public long read(@NonNull Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                if(listener != null) {
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;

                    if (length < 0) {
                        listener.onProgress(task, -1, -1);
                    }
                    if (bytesRead >= 0) {
                        if (skipBytes > 0) {
                            skippedBytes = (skipBytes + totalBytesRead);
                            listener.onProgress(task, skippedBytes, skippedLength);
                        } else {
                            listener.onProgress(task, totalBytesRead, length);
                        }
                    }
                    if (bytesRead == -1) {
                        listener.onComplete(task);
                    }
                }
                return bytesRead;
            }
        };
    }

}
