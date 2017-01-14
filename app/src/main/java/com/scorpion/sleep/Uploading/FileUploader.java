package com.scorpion.sleep.Uploading;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by stephen on 2015-11-05.
 */
public class FileUploader {

    public interface Listener {
        void onUploadStarted();
        void onUploadProgressChange(int completionPercentage);
        void onUploadComplete();
        void onUploadFailed(Throwable e);
    }

    public static String sendFileToServer(String filename, String targetUrl, Listener listener) {
        String response = "error";
        Log.e("Image filename", filename);
        Log.e("url", targetUrl);
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        // DataInputStream inputStream = null;

        String pathToOurFile = filename;
        String urlServer = targetUrl;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");

        int totalFileSize;
        int percentageCompleted = 0;
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024;
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(
                    pathToOurFile));

            URL url = new URL(urlServer);
            connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs & Outputs
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setChunkedStreamingMode(1024);
            // Enable POST method
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            String connstr = null;
            connstr = "Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
                    + pathToOurFile + "\"" + lineEnd;
            Log.i("Connstr", connstr);

            outputStream.writeBytes(connstr);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            totalFileSize = bytesAvailable;
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            Log.e("Image length", bytesAvailable + "");
            try {
                listener.onUploadStarted();
                while (bytesRead > 0) {
                    try {
                        outputStream.write(buffer, 0, bufferSize);
                    } catch (OutOfMemoryError e) {
                        listener.onUploadFailed(e);
                        e.printStackTrace();
                        response = "outofmemoryerror";
                        return response;
                    }
                    bytesAvailable = fileInputStream.available();

                    // Only post updates at every 1 percent
                    int currentPercentage = getPercentage(totalFileSize, bytesAvailable);
                    if (currentPercentage >= percentageCompleted + 1) {
                        percentageCompleted = currentPercentage;
                        listener.onUploadProgressChange(currentPercentage);
                    }

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
            } catch (Exception e) {
                listener.onUploadFailed(e);
                e.printStackTrace();
                response = "error";
                return response;
            }
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                    + lineEnd);

            // Responses from the server (code and message)
            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();
            Log.i("Server Response Code ", "" + serverResponseCode);
            Log.i("Server Response Message", serverResponseMessage);

            if (serverResponseCode == 200) {
                response = "true";
            }

            String CDate = null;
            Date serverTime = new Date(connection.getDate());
            try {
                CDate = df.format(serverTime);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Date Exception", e.getMessage() + " Parse Exception");
            }
            Log.i("Server Response Time", CDate + "");

            filename = CDate
                    + filename.substring(filename.lastIndexOf("."),
                    filename.length());
            Log.i("File Name in Server : ", filename);

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception ex) {
            // Exception handling
            listener.onUploadFailed(ex);
            response = "error";
            Log.e("Send file Exception", ex.getMessage() + "");
            ex.printStackTrace();
        }
        listener.onUploadComplete();
        return response;
    }

    private static int getPercentage(int initialSize, int remainingSize) {
        int uploadedAmount = initialSize - remainingSize;
        float ratio = (float) uploadedAmount / initialSize;
        return (int) (ratio * 100);
    }
}
