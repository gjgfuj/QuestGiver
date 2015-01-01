package com.awesomesauce.android.questgiver.android;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.awesomesauce.android.questgiver.R;
import com.awesomesauce.android.questgiver.game.AbstractQuest;
import com.awesomesauce.android.questgiver.game.BasicQuest;
import com.awesomesauce.android.questgiver.game.Quest;
import com.awesomesauce.android.questgiver.game.QuestManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class QuestGiverService extends Service {
    public QuestManager manager = new QuestManager();
    public void downloadQuestsJson() {
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("url", "https://raw.githubusercontent.com/gjgfuj/QuestGiver/master/app/src/main/res/raw/quests.json");
        startService(intent);
    }
    public void loadQuests() {
        try {
            if (!new File(getFilesDir(), "quests.json").exists()) {
                InputStream inputStream = getResources().openRawResource(R.raw.quests);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                {
                    builder.append(line);
                }
                FileOutputStream outputStream = openFileOutput("quests.json", Context.MODE_PRIVATE);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                writer.write(builder.toString());
                writer.close();
                outputStream.close();
                inputStream.close();

            }
        }
        catch (IOException e)
        {

        }
        try {
            FileInputStream inputStream = openFileInput("quests.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                builder.append(line);
            }
            JSONArray array = (JSONArray) new JSONTokener(builder.toString()).nextValue();
            manager.setQuestAmount(array.length());
            for (int i=0;i<array.length();i++)
            {
                manager.putQuest(i, new BasicQuest(array.getJSONObject(i).toString()));
            }
            inputStream.close();
        }
        catch (JSONException e)
        {

        }
        catch (IOException e)
        {

        }
    }
    public void reset() {
        manager = new QuestManager();
        deleteFile("questManager.json");
        loadQuests();
    }
    public void resetQuestsJson() {
        deleteFile("quests.json");
        downloadQuestsJson();
        reset();
    }
    public void startTimer(final Quest quest) {

        new CountDownTimer(quest.getTimeTaken()*1000, 1000) {

            public void onTick(long millisUntilFinished) {
                quest.setTimeTaken(millisUntilFinished/1000);
            }

            public void onFinish() {
                if (manager.canCompleteQuest(quest)) {
                    Intent intent = new Intent(QuestGiverService.this, QuestDisplayActivity.class);
                    intent.putExtra("questId", manager.getQuestIndex(quest));
                    if (android.os.Build.VERSION.SDK_INT >= 16) {
                        Notification.Builder note = new Notification.Builder(QuestGiverService.this)
                                .setContentTitle(quest.getDisplayName())
                                .setContentText("Quest Complete").setSmallIcon(R.drawable.ic_launcher)
                                .setContentIntent(PendingIntent.getActivity(QuestGiverService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(0, note.build());
                    }

                    manager.completeQuest(quest);
                }
                else
                {
                    startQuest(quest);
                }
            }
        }.start();
    }
    public void startQuest(final Quest quest) {
        if (manager.startQuest(quest))
        {
            startTimer(quest);
        }
    }
    public QuestGiverService() {
    }
    @Override
    public void onCreate() {
        Log.i("Service", "Created service");
        try {
            FileInputStream inputStream = openFileInput("questManager.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String json = reader.readLine();
            if (json != null) {
                Log.i("JSON", json);
                manager.fromJSON(json);
            }
            else
            {
                loadQuests();
            }
            reader.close();
            inputStream.close();
        }
        catch (IOException e)
        {
            Log.e("JSON", e.toString());
            loadQuests();
        }
        for (int i=0;i<manager.getQuestAmount();i++)
        {
            Quest quest = manager.getQuest(i);
            if (quest.isStarted() && !quest.isDone())
            {
                startTimer(quest);
            }
        }
    }
    @Override
    public void onDestroy() {
        Log.i("Service", "Destroyed service");
        try {
            FileOutputStream outputStream = openFileOutput("questManager.json", Context.MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            String json = manager.toJSON();
            Log.i("JSON", json);
            writer.write(json);
            writer.close();
            outputStream.close();
        }
        catch (IOException e)
        {
            Log.e("JSON", e.toString());
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder();
    }

    public class ServiceBinder extends Binder {
        public QuestGiverService getService() {
            return QuestGiverService.this;
        }
    }
}
