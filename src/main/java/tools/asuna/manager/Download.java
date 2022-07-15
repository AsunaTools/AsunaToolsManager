package tools.asuna.manager;



import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;


public class Download{

    public static void downloadFile(final String urlf, final String path, final String info) throws IOException {
        URL url = new URL(urlf);
        HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
        long completeFileSize = httpConnection.getContentLength();

        try(InputStream inputStream = url.openStream();
            CountingInputStream cis = new CountingInputStream(inputStream);
            FileOutputStream fileOS = new FileOutputStream(path);
            ProgressBar downloadProgress = new ProgressBar(info, Math.floorDiv(completeFileSize, 1000))) {

            new Thread(() -> {
                try {
                    IOUtils.copyLarge(cis, fileOS);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            while (cis.getByteCount() < completeFileSize) {
                downloadProgress.stepTo(Math.floorDiv(cis.getByteCount(), 1000));
            }

            downloadProgress.stepTo(Math.floorDiv(cis.getByteCount(), 1000));
        }
    }
}
