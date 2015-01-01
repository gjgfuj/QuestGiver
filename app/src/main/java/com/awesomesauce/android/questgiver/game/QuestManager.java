package com.awesomesauce.android.questgiver.game;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class QuestManager {
    private List<Quest> questList = new ArrayList<Quest>();
    public void fromJSON(String json) {
        try {
            JSONObject jsonObject = (JSONObject) new JSONTokener(json).nextValue();
            JSONArray jsonQuestList = jsonObject.getJSONArray("questList");
            for (int i=0;i<jsonQuestList.length();i++)
            {
                if (questList.size() > i)
                    questList.get(i).fromJSON(jsonQuestList.getJSONObject(i).toString());
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            Log.e("JSON", "Loading failed on quest list.");
        }
    }
    public String toJSON() {
        try {
            JSONObject object = new JSONObject();
            JSONArray jsonQuestList = new JSONArray();
            for (Quest quest : questList) {
                jsonQuestList.put(new JSONTokener(quest.toJSON()).nextValue());
            }
            object.put("questList", jsonQuestList);
            return object.toString();
        }
        catch (JSONException e)
        {
            Log.e("JSON", "Serialization failed on quest list.");
        }
        return "";
    }
    public QuestManager() {
        Quest testQuest = new BasicQuest();
        questList.add(testQuest);
    }
    public void putQuest(int index, Quest quest) {
        while (questList.size() <= index)
            questList.add(questList.get(0));
        questList.set(index, quest);
    }
    public int getQuestAmount() { return questList.size(); }
    public void setQuestAmount(int amount) {
        for (int i=0;i<amount;i++)
        {
            putQuest(i, new BasicQuest());
        }
    }
    public int addQuest(Quest quest)
    {
        questList.add(quest);
        return questList.indexOf(quest);
    }
    public Quest getQuest(int index) {
        return questList.get(index);
    }
    public int getQuestIndex(Quest quest) {
        return questList.indexOf(quest);
    }
    public boolean canStartQuest(Quest quest) {
        for (int i=0;i<quest.getRequiredQuests().length;i++)
        {
            if (!getQuest(quest.getRequiredQuests()[i]).isDone())
            {
                return false;
            }
        }
        if (!quest.isDone() && !quest.isStarted())
            return quest.canStart(this);
        return false;
    }
    public boolean startQuest(Quest quest) {
        if (canStartQuest(quest))
        {
            quest.start(this);
            return true;
        }
        return false;
    }
    public boolean canCompleteQuest(Quest quest) {
        return quest.canComplete(this);
    }
    public boolean completeQuest(Quest quest) {
        if (canCompleteQuest(quest))
        {
            quest.complete(this);
            return true;
        }
        return false;
    }
    public boolean isQuestVisible(Quest quest) {
        return canStartQuest(quest) || quest.isStarted();
    }
    public int getCompleteQuestCount() {
        int count = 0;
        for (Quest quest : questList) {
            if (quest.isDone())
            {
                count++;
            }
        }
        return count;
    }
    public int getTotalAccessibleQuestCount() {
        int count = 0;
        for (Quest quest : questList) {
            if (isQuestVisible(quest))
            {
                count++;
            }
        }
        return count;
    }
}
