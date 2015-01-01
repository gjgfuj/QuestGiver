package com.awesomesauce.android.questgiver.android;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.awesomesauce.android.questgiver.R;
import com.awesomesauce.android.questgiver.game.Quest;


public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    QuestGiverService service = null;
    ServiceConnection connection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
        public void onServiceConnected(ComponentName componentName, IBinder binder){

            service = ((QuestGiverService.ServiceBinder) binder).getService();

        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        recreateView();
        startTimers();
    }
    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(this, QuestGiverService.class));
        bindService(new Intent(this, QuestGiverService.class), connection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(
                        actionBar.getThemedContext(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[]{
                                getString(R.string.title_section1),
                                getString(R.string.title_section2),
                                getString(R.string.title_section3),
                        }),
                this);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getSupportActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getSupportActionBar().getSelectedNavigationIndex());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_delete_progress)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Confirm");
            builder.setMessage("Are you sure you wish to delete all your quest progress?");

            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    service.reset();
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intent);
                    dialog.dismiss();
                }

            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
            return true;
        }
        else if (id == R.id.action_reload_quests)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Confirm");
            builder.setMessage("Are you sure you wish to reload all your quests?\nThis will reset your quest progress as well.");

            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    service.resetQuestsJson();
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intent);
                    dialog.dismiss();
                }

            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startTimers() {
        if (service != null) {
            for (int i = 0; i < service.manager.getQuestAmount(); i++) {
                Quest quest = service.manager.getQuest(i);
                if (service.manager.isQuestVisible(quest)) {
                    new CountDownTimer((quest.getTimeTaken() + 1) * 1000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            recreateView();
                        }

                        public void onFinish() {
                            recreateView();
                        }
                    }.start();
                }
            }
        }
    }
    @Override
    public boolean onNavigationItemSelected(int position, long id) {

        // When the given dropdown item is selected, show its contents in the
        // container view.
        Fragment fragment;
        switch (position) {
            case 1:
                fragment = new AvailableQuestsFragment();
                startTimers();
                break;
            default:
                fragment = new OverviewFragment();
                startTimers();
                break;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        return true;
    }
    public void recreateView() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.questsList);
        if (layout != null) {
            layout.removeAllViewsInLayout();
            for (int i = 0; i < service.manager.getQuestAmount(); i++) {
                Quest quest = service.manager.getQuest(i);
                if (service.manager.isQuestVisible(quest))
                    layout.addView(QuestUtils.makeQuestButton(MainActivity.this, service.manager, quest));
            }
        }
        layout = (LinearLayout) findViewById(R.id.overviewInformation);
        if (layout != null) {
            layout.removeAllViewsInLayout();
            TextView view = new TextView(this);
            view.setText("Completed Quests: "+service.manager.getCompleteQuestCount());
            view.setTextColor(Color.GREEN);
            layout.addView(view);
            view = new TextView(this);
            view.setText("Possible Quests: "+service.manager.getTotalAccessibleQuestCount());
            view.setTextColor(Color.MAGENTA);
            layout.addView(view);
            for (int i = 0; i < service.manager.getQuestAmount(); i++) {
                Quest quest = service.manager.getQuest(i);
                if (quest.isStarted() && !quest.isDone())
                    layout.addView(QuestUtils.makeQuestButton(MainActivity.this, service.manager, quest));
            }
        }
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class OverviewFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";


        public OverviewFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_overview, container, false);
            return rootView;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class AvailableQuestsFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        public AvailableQuestsFragment() {
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_available_quests, container, false);
            return rootView;
        }
    }
}
