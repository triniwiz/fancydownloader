package co.fitcom.downloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

import co.fitcom.fancydownloader.DownloadListener;
import co.fitcom.fancydownloader.DownloadListenerUI;
import co.fitcom.fancydownloader.Manager;
import co.fitcom.fancydownloader.Request;

public class MainActivity extends AppCompatActivity {
    String imageDownloadId;
    String fileDownloadId;
    Manager downloadManager;
    String SMALL_IMAGE = "https://wallpaperscraft.com/image/hulk_wolverine_x_men_marvel_comics_art_99032_3840x2400.jpg";
    String LARGE_IMAGE = "https://images.unsplash.com/photo-1513025186107-2688cad44a98?ixlib=rb-0.3.5&q=85&fm=jpg&crop=entropy&cs=srgb&dl=sean-pierce-477569.jpg&s=17dbae12dc69dc9be30404a13f85b5da";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Manager.init(this);
    }


    public void generateDownloads(android.view.View view) {
        downloadManager = Manager.getInstance();
        final ImageView imageView = (ImageView) findViewById(R.id.image);
        final ProgressBar imageProgressBar = (ProgressBar) findViewById(R.id.imageProgress);
        imageProgressBar.setMax(100);
        final TextView imageTextView = (TextView) findViewById(R.id.imageTextProgress);
        final ProgressBar fileProgressBar = (ProgressBar) findViewById(R.id.fileProgress);
        fileProgressBar.setMax(100);
        final TextView fileTextView = (TextView) findViewById(R.id.fileTextProgress);
        final Request request = new Request(LARGE_IMAGE);

        request.setListener(new DownloadListener() {
            @Override
            public void onComplete(String task) {
                String path = request.getFilePath().concat("/");
                final String imagePath = path.concat(request.getFileName());
               final Bitmap bitmap = decodeSampledBitmapFromFile(imagePath,200,200);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
            @Override
            public void onError(String task, Exception exception) {
            }

            @Override
            public void onProgress(String task, final long currentBytes, final long totalBytes,long speed) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int progress = (int) ((currentBytes * 100L) / totalBytes);
                        imageProgressBar.setProgress(progress);
                        String percent =  "".concat(Integer.toString(progress) + "%");
                        imageTextView.setText(percent);
                    }
                });
            }
        });


        Request fileRequest = new Request("http://ipv4.download.thinkbroadband.com/20MB.zip");
        fileRequest.setListener(new DownloadListener() {
            @Override
            public void onComplete(String task) {


            }
            @Override
            public void onError(String task, Exception exception) {
            }
            @Override
            public void onProgress(String task, final long currentBytes, final long totalBytes,long speed) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int progress = (int) ((currentBytes * 100L) / totalBytes);
                        fileProgressBar.setProgress(progress);
                        String percent =  "".concat(Integer.toString(progress) + "%");
                        fileTextView.setText(percent);
                    }
                });
            }
        });


        imageDownloadId = downloadManager.create(request);
        fileDownloadId = downloadManager.create(fileRequest);
    }


    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    public static Bitmap decodeSampledBitmapFromFile(String file,int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file,options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file,options);
    }

    public void downloadImage(android.view.View view) {
        downloadManager.start(imageDownloadId);
    }

    public void downloadFile(android.view.View view) {
        downloadManager.start(fileDownloadId);
    }

    public void pauseImageDownload(android.view.View view){
        downloadManager.pause(imageDownloadId);
    }
    public void pauseFileDownload(android.view.View view){
        downloadManager.pause(fileDownloadId);
    }

    public void resumeImageDownload(android.view.View view){
        downloadManager.resume(imageDownloadId);
    }
    public void resumeFileDownload(android.view.View view){
        downloadManager.resume(fileDownloadId);
    }

    /*
    public void retryImageDownload(android.view.View view){
        downloadManager.retry(imageDownloadId);
    }
    public void retryFileDownload(android.view.View view){
        downloadManager.retry(fileDownloadId);
    }
    */
    public void cancelImageDownload(android.view.View view){
        downloadManager.cancel(imageDownloadId);
    }
    public void cancelFileDownload(android.view.View view){
        downloadManager.cancel(fileDownloadId);
    }
}
