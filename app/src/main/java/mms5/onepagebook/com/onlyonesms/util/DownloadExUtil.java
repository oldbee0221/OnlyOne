package mms5.onepagebook.com.onlyonesms.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import mms5.onepagebook.com.onlyonesms.common.Constants;

/**
 * Created by jeonghopark on 2020/05/12.
 */
public abstract class DownloadExUtil extends AsyncTask<String, Integer, String> implements Constants {
    private Context mContext;
    private String mUrlStr;
    private String mFileName;

    public DownloadExUtil(Context context, String urlStr, String fileName) {
        mContext = context;
        mUrlStr = urlStr;
        mFileName = fileName;
    }

    @Override
    protected String doInBackground(String... params) {
        int count;
        String ret = "success";

        try {
            URL url = new URL(mUrlStr);
            URLConnection conn = url.openConnection();
            conn.connect();

            InputStream inputStream = new BufferedInputStream(url.openStream());
            File directory = new File(Environment.getExternalStorageDirectory() + DOWNLOAD_DIR);

            if (!directory.exists()) {
                directory.mkdir();
            }
            File file = new File(directory + "/" + mFileName);

            OutputStream outputStream = new FileOutputStream(file);

            byte[] data = new byte[1024];

            while ((count = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, count);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            ret = file.getAbsolutePath();
        } catch(IOException e) {
            ret = "fail";
            Utils.Log("Downloader doInBackground() " + e.toString());
        }

        return ret;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        callback(s);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    public abstract void callback(String result);
}

