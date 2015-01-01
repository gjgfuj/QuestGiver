package com.awesomesauce.android.questgiver.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.awesomesauce.android.questgiver.R;
import com.awesomesauce.android.questgiver.game.Quest;
import com.awesomesauce.android.questgiver.game.QuestManager;

import java.util.Timer;
import java.util.TimerTask;

public class QuestDisplayActivity extends ActionBarActivity {
    int questId = -1;
    Quest quest = null;
    QuestGiverService service = null;
    ServiceConnection connection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
        public void onServiceConnected(ComponentName componentName, IBinder binder){
            service = ((QuestGiverService.ServiceBinder) binder).getService();
            prepareQuestDetails();
            startTimer();
        }
    };
    protected void startTimer() {
        if (quest.isStarted() && !quest.isDone())
        {
            new CountDownTimer((quest.getTimeTaken()+1)*1000, 1000) {

                public void onTick(long millisUntilFinished) {
                    prepareQuestDetails();
                }

                public void onFinish() {
                    prepareQuestDetails();
                }
            }.start();
        }
    }
    protected void prepareQuestDetails() {
        final QuestManager manager = service.manager;
        quest = manager.getQuest(questId);

        TextView view = (TextView) findViewById(R.id.quest_title_view);
        view.setText(quest.getDisplayName());

        view = (TextView) findViewById(R.id.quest_description);
        view.setText(quest.getDescription());
        if (manager.canStartQuest(quest))
        {
            findViewById(R.id.start_quest).setEnabled(true);
        }
        view = (TextView) findViewById(R.id.current_status);
        if (!quest.isStarted())
        {
            view.setText("Not Started");
            view.setTextColor(Color.RED);
        }
        else if (!quest.isDone()) {
            view.setText("In Progress, "+quest.getTimeTaken()+"s to go.");
            view.setTextColor(Color.MAGENTA);
        }
        else
        {
            view.setText("Done.");
            view.setTextColor(Color.GREEN);
        }
        LinearLayout layout = (LinearLayout) findViewById(R.id.questRequirements);
        int[] reqQuests = quest.getRequiredQuests();
        if (reqQuests.length >= 1) {
            layout.removeAllViewsInLayout();
            for (int i = 0; i < reqQuests.length; i++) {
                final Quest reqQuest = manager.getQuest(reqQuests[i]);
                Button button = new Button(this);
                button.setText(reqQuest.getDisplayName());
                if (reqQuest.isDone()) {
                    button.setEnabled(false);
                }
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(QuestDisplayActivity.this, QuestDisplayActivity.class);
                        intent.putExtra("questId", manager.getQuestIndex(reqQuest));
                        startActivity(intent);
                    }
                });
                layout.addView(button);

            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_display);
        questId = getIntent().getIntExtra("questId", -1);
        if (questId == -1)
        {
            Toast.makeText(QuestDisplayActivity.this, "Invalid Quest", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(QuestDisplayActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        bindService(new Intent(this, QuestGiverService.class), connection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_quest_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_back) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void startQuest(View view) {
        service.startQuest(quest);
        if (quest.isStarted())
        {
            view.setEnabled(false);
            startTimer();
        }
    }
}
