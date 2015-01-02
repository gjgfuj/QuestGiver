package com.awesomesauce.android.questgiver.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

import com.awesomesauce.android.questgiver.game.Quest;
import com.awesomesauce.android.questgiver.game.QuestManager;

/**
 * Created by gjgfuj on 1/1/15.
 */
public class QuestUtils {
    public static Button makeQuestButton(final Context context, final QuestManager manager, final Quest reqQuest) {

        Button button = new Button(context);
        button.setText(reqQuest.getDisplayName());
        if (reqQuest.isDone()) {
            button.setTextColor(Color.GREEN);
        }
        else if (reqQuest.isStarted())
        {
            button.setTextColor(Color.MAGENTA);
            button.setText(reqQuest.getDisplayName()+" ("+reqQuest.getHumanReadableTimeTaken()+")");
        }
        else
        {
            button.setTextColor(Color.RED);
            if (manager.canStartQuest(reqQuest))
            {
                button.setText(reqQuest.getDisplayName()+" (Can start)");
            }
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, QuestDisplayActivity.class);
                intent.putExtra("questId", manager.getQuestIndex(reqQuest));
                context.startActivity(intent);
            }
        });
        return button;
    }
}
