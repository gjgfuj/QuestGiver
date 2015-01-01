package com.awesomesauce.android.questgiver.android;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;

import com.awesomesauce.android.questgiver.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadService extends IntentService {
    public static final int UPDATE_PROGRESS = 8344;
    QuestGiverService service = null;
    ServiceConnection connection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
        public void onServiceConnected(ComponentName componentName, IBinder binder){
            service = ((QuestGiverService.ServiceBinder) binder).getService();
        }
    };
    public DownloadService() {
        super("QuestDownloadService");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        bindService(new Intent(this, QuestGiverService.class), connection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        String urlToDownload = intent.getStringExtra("url");
        //ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");
        try {
            URL url = new URL(urlToDownload);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(connection.getInputStream());
            OutputStream output = new FileOutputStream(new File(getFilesDir(), "quests.json"));

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                //Bundle resultData = new Bundle();
                //resultData.putInt("progress" ,(int) (total * 100 / fileLength));
                //receiver.send(UPDATE_PROGRESS, resultData);
                output.write(data, 0, count);
                System.out.println(data);
            }

            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            Notification.Builder note = new Notification.Builder(this)
                    .setContentTitle("Quest Giver")
                    .setContentText("Updated Quest Files.").setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, note.build());
        }
        service.reset();
        unbindService(connection);
    }
}