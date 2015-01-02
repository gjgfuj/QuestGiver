package com.awesomesauce.android.questgiver.game;


public interface Quest {
    /**
     *
     * @return The display name for the quest.
     */
    public String getDisplayName();

    /**
     *
     * @return The description for the quest.
     */
    public String getDescription();
    public long getMaxTimeTaken();
    /**
     *
     * @return the time taken in seconds to complete the quest.
     */
    public long getTimeTaken();

    /**
     * Sets the time in seconds until the quest is complete.
     */
    public void setTimeTaken(long time);

    /**
     *
     * @return Whether the quest is finished or not.
     */
    public boolean isDone();

    /**
     *
     * @return the quest numbers that are required to start this quest.
     */
    public int[] getRequiredQuests();

    /**
     * @return Whether the quest is possible to start.
     */
    public boolean canStart(QuestManager manager);
    /**
     * Starts the quest.
     */
    public void start(QuestManager manager);
    public boolean isStarted();
    /**
     * @return Whether the quest can be completed.
     */
    public boolean canComplete(QuestManager manager);
    /**
     * Completes the quest.
     * This is called after the time taken is taken.
     */
    public void complete(QuestManager manager);

    /**
     * Saves any neccessary information to JSON.
     * @return the JSON string.
     */
    public String toJSON();

    /**
     * Reads any neccessary information from JSON.
     * @param json the JSON string.
     */
    public void fromJSON(String json);
    public boolean isVisibleOverride();
    public boolean isVisible();
}
