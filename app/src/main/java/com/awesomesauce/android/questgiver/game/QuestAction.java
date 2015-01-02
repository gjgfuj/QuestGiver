package com.awesomesauce.android.questgiver.game;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class QuestAction {
    String actionType = "none";
    JSONObject object;
    public QuestAction(String json) {
        try {
            object = (JSONObject) new JSONTokener(json).nextValue();
            actionType = object.getString("type");
        }
        catch (JSONException e)
        {

        }
    }
    public void executeAction(Quest quest, QuestManager manager) {
        Quest targetQuest = manager.getQuest(object.optInt("target", manager.getQuestIndex(quest)));
        try {
            switch (actionType) {
                case "setVisible":
                    if (targetQuest instanceof JSONQuest)
                    {
                        ((JSONQuest) targetQuest).visible = object.optBoolean("value", true);
                    }
                    break;
                case "setVisibleOverride":
                    if (targetQuest instanceof JSONQuest)
                    {
                        ((JSONQuest) targetQuest).visibleOverride = object.optBoolean("value", true);
                    }
                    break;
                case "setMaxTimeTaken":
                    if (targetQuest instanceof JSONQuest)
                    {
                        ((JSONQuest) targetQuest).maxTimeTaken = object.getLong("value");
                    }
                    break;
                case "setTimeTaken":
                    targetQuest.setTimeTaken(object.getLong("value"));
                    break;
                case "completeQuest":
                    manager.completeQuest(targetQuest);
                    break;
                default:
                    break;
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
