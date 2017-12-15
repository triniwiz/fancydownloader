# NativeScript Downloader

## Installation

```
compile 'co.fitcom:fancydownloader:0.0.1'
```

## Usage

```java
import co.fitcom.fancydownloader.Manager;
import co.fitcom.fancydownloader.Request;
Manager downloadManager;

protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Manager.init(this);
        downloadManager = Manager.getInstance();
    }

    String fileDownloadId;
    Request fileRequest = new Request("http://ipv4.download.thinkbroadband.com/20MB.zip");
            fileRequest.setListener(new DownloadListener() {
                public void onComplete(String task) {


                }

                public void onError(String task) {
                }

                public void onProgress(String task, final long currentBytes, final long totalBytes,long speed) {
                    int progress = (int) ((currentBytes * 100L) / totalBytes);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Update UI
                        }
                    });
                }
            });
    fileDownloadId = downloadManager.create(fileRequest);
    downloadManager.start(fileDownloadId);
```

## Api

| Method                  | Default | Type    | Description                                           |
| ----------------------- | ------- | ------- | ----------------------------------------------------- |
| create(Request request) |         | String  | Creates a download task it returns the id of the task |
| start(String id)        |         | void    | Starts a download task.                               |
| resume(String id)       |         | void    | Resumes a download task.                              |
| cancel(String id)       |         | void    | Cancels a download task.                              |
| cancelAll(String)       |         | void    | Cancels all download task.                            |
| pause(String id)        |         | void    | Pauses a download task.                               |
| pauseAll()              |         | void    | Pauses all download task.                             |
| init(Context context)   |         | void    | Initiates the download manager                        |
| getInstance()           |         | Manager | Get an instance of the download manager               |

# TODO

* [ ] Local Notifications
