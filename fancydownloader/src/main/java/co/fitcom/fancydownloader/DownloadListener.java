package co.fitcom.fancydownloader;

/**
 * Created by triniwiz on 12/13/17.
 */

import java.util.HashMap;
import java.util.ArrayList;

public abstract class DownloadListener implements DownloadCallback {
    private long lastRefreshTime = 0;
    private long lastBytesWritten = 0;

    public abstract void onProgress(String task, long currentBytes, long totalBytes, long speed);

    void onProgress(String task, long currentBytes, long totalBytes) {
        float percent = (currentBytes * 1.0F) / totalBytes;
        long currentTime = System.currentTimeMillis();
        int minTime = 100;
        if (currentTime - lastRefreshTime >= minTime || currentBytes == totalBytes || percent >= 1F) {
            long intervalTime = (currentTime - lastRefreshTime);
            if (intervalTime == 0) {
                intervalTime += 1;
            }
            long updateBytes = currentBytes - lastBytesWritten;
            final long networkSpeed = updateBytes / intervalTime;
            onProgress(task,currentBytes, totalBytes,networkSpeed);
            lastRefreshTime = System.currentTimeMillis();
            lastBytesWritten = currentBytes;
        }
   }

   public void onComplete(String task, int statusCode, HashMap<String, ArrayList<String>> headers) { }
   public void onHeaders(String task, int statusCode, HashMap<String, ArrayList<String>> headers) {}
   public void onError(String task, Exception e){};
}
