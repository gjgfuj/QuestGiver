package com.awesomesauce.android.questgiver.game;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gjgfuj on 12/31/14.
 */
public class JSONQuest extends AbstractQuest {
    public static int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }
    long maxTimeTaken;
    String displayName;
    String description;
    List<Integer> deps = new ArrayList<>();
    public JSONQuest() {
        this(1, "Unknown Quest", "Unknown Quest");
    }
    public JSONQuest(long mtt, String dn, String d) {
        maxTimeTaken = mtt;
        displayName = dn;
        description = d;
    }
    public JSONQuest(String jsonQuestDetail) {
        try {
            JSONObject object = (JSONObject) new JSONTokener(jsonQuestDetail).nextValue();
            maxTimeTaken = object.getLong("maxTimeTaken");
            displayName = object.getString("displayName");
            description = object.getString("description");
            JSONArray array = object.getJSONArray("requirements");
            for (int i=0;i<array.length();i++)
            {
                addDependency(array.getInt(i));
            }
        }
        catch (JSONException e)
        {

        }
    }
    public void addDependency(int dep) {
        deps.add(dep);
    }
    public int[] getRequiredQuests() {
        return convertIntegers(deps);
    }
    public long getMaxTimeTaken() { return maxTimeTaken; }
    public String getDisplayName() {
        return displayName;
    }
    public String getDescription() {
        return description;
    }
}
