package com.awesomesauce.android.questgiver.game;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public abstract class AbstractQuest implements Quest {
    public boolean started = false;
    public boolean done = false;
    long timeTaken = getMaxTimeTaken();
    public void setTimeTaken(long timeTakenl)
    {
        timeTaken = timeTakenl;
    }
    public long getTimeTaken() {
        return timeTaken;
    }
    public int[] getRequiredQuests() {
        return new int[] {};
    }
    public boolean canStart(QuestManager manager) {
        return true;
    }
    public void start(QuestManager manager) {
        started = true; timeTaken = getMaxTimeTaken();
    }
    public boolean isStarted() { return started; }
    public boolean canComplete(QuestManager manager) {
        return true;
    }
    public void complete(QuestManager manager) {
        done = true;
    }
    public boolean isDone() {
        return done;
    }
    @Override
    public String toJSON() {
        try {
            JSONObject object = new JSONObject();
            object.put("started", started);
            object.put("done", done);
            object.put("timeTaken", timeTaken);
            return object.toString();
        }
        catch (JSONException e)
        {
            Log.e("JSON", "Serialization failed.");
        }
        return "";
    }
    public void fromJSON(String json) {
        try {
            JSONObject jsonObject = (JSONObject) new JSONTokener(json).nextValue();
            started = jsonObject.getBoolean("started");
            done = jsonObject.getBoolean("done");
            timeTaken = jsonObject.getLong("timeTaken");
        }
        catch (JSONException e)
        {
            Log.e("JSON", "Loading Failed");
        }
    }
    public boolean isVisible() {return true;}
    public boolean isVisibleOverride() {return false;}
    public String getHumanReadableTimeTaken() {
        StringBuilder builder = new StringBuilder();
        if (timeTaken > 24*60*60)
        {
            builder.append((int) (timeTaken/(24*60*60))+"d, ");
        }
        if (timeTaken > 60*60)
        {
            builder.append(timeTaken/(60*60)%(24)+"h, ");
        }
        if (timeTaken > 60)
        {
            builder.append(timeTaken/60%(60)+"m, ");
        }
        builder.append(timeTaken%60+"s");
        return builder.toString();
    }
    public String getHumanReadableMaxTimeTaken() {
        long localTimeTaken = timeTaken;
        timeTaken = getMaxTimeTaken();
        String hr = getHumanReadableTimeTaken();
        timeTaken = localTimeTaken;
        return hr;
    }
}
