package csusm.cougarplanner.config;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the application configuration profile stored in the .profile file.
 * Contains all user settings and preferences as key-value pairs.
 */
public class Profile
{

    // Storage of all config properties as string key-value pairs
    private final Map<String, String> properties;

    // Default values for all config params
    private static final Map<String, String> DEFAULTS = createDefaults();

    /**
     * Creates and initializes default config values.
     *
     * @return Map containing all default config values
     */
    private static Map<String, String> createDefaults()
    {
        Map<String, String> defaults = new HashMap<>();
        defaults.put("authToken", "");
        defaults.put("loginCompleted", "false");
        defaults.put("storeData", "true");
        defaults.put("storeToken", "true");
        defaults.put("weekStart", "monday");
        defaults.put("defaultView", "week");
        defaults.put("openTo", "current_week");
        defaults.put("defaultSort.mode", "date_time");
        defaults.put("defaultSort.difficultyOrder", "ascending");
        defaults.put("showRefreshCountdown", "false");
        defaults.put("lastViewedWeekStart", "");
        defaults.put("lastViewedDay", "");
        defaults.put("theme", "cougar");
        return defaults;
    }

    /**
     * Constructs a new Profile with all default values.
     * Used when no .profile file exists or during first-time setup.
     */
    public Profile()
    {
        this.properties = new HashMap<>(DEFAULTS);
    }

    /**
     * Constructs a new Profile with custom properties, or defaults for missing values.
     *
     * @param properties custom config properties to override defaults
     */
    public Profile(Map<String, String> properties)
    {
        this.properties = new HashMap<>(DEFAULTS);
        this.properties.putAll(properties);
    }

    /**
     * Gets the Canvas API authentication token.
     *
     * @return the auth token string, or empty string
     */
    public String getAuthToken()
    {
        return properties.get("authToken");
    }

    /**
     * Gets the login completion status as a string.
     *
     * @return true if login completed, false otherwise
     */
    public String getLoginCompleted()
    {
        return properties.get("loginCompleted");
    }

    /**
     * Gets the data storage preference as a string.
     *
     * @return true if local CSV storage is enabled, false otherwise
     */
    public String getStoreData()
    {
        return properties.get("storeData");
    }

    /**
     * Gets the token storage preference as a string.
     *
     * @return true if token should be stored in .profile, false otherwise
     */
    public String getStoreToken()
    {
        return properties.get("storeToken");
    }

    /**
     * Gets the week start day preference.
     *
     * @return monday or sunday indicating the first day of the week
     */
    public String getWeekStart()
    {
        return properties.get("weekStart");
    }

    /**
     * Gets the default view setting.
     *
     * @return week or day indicating the default application view
     */
    public String getDefaultView()
    {
        return properties.get("defaultView");
    }

    /**
     * Gets the application opening behavior.
     *
     * @return current_week or last_viewed indicating where to open the app
     */
    public String getOpenTo()
    {
        return properties.get("openTo");
    }

    /**
     * Gets the default sorting mode for assignments.
     *
     * @return date_time or difficulty indicating primary sort method
     */
    public String getDefaultSortMode()
    {
        return properties.get("defaultSort.mode");
    }

    /**
     * Gets the difficulty sort order preference.
     *
     * @return ascending or descending for difficulty-based sorting
     */
    public String getDefaultSortDifficultyOrder()
    {
        return properties.get("defaultSort.difficultyOrder");
    }

    /**
     * Gets the refresh countdown display preference as a string.
     *
     * @return true if refresh countdown should be shown, false otherwise
     */
    public String getShowRefreshCountdown()
    {
        return properties.get("showRefreshCountdown");
    }

    /**
     * Gets the last viewed week start date as a string.
     *
     * @return YYYY-MM-DD formatted date string, or empty string if not set
     */
    public String getLastViewedWeekStart()
    {
        return properties.get("lastViewedWeekStart");
    }

    /**
     * Gets the last viewed day as a string.
     *
     * @return YYYY-MM-DD formatted date string, or empty string if not set
     */
    public String getLastViewedDay()
    {
        return properties.get("lastViewedDay");
    }

    /**
     * Gets the selected UI theme.
     *
     * @return theme name (cougar, dark, or sunset)
     */
    public String getTheme() {
        return properties.get("theme");
    }

    /**
     * Checks if the user has completed the initial login.
     *
     * @return true if login is completed, false otherwise
     */
    public boolean isLoginCompleted()
    {
        return "true".equals(getLoginCompleted());
    }

    /**
     * Checks if local data storage is enabled.
     *
     * @return true if CSV files should be used for data persistence, false otherwise
     */
    public boolean shouldStoreData()
    {
        return "true".equals(getStoreData());
    }

    /**
     * Checks if the authentication token should be stored in .profile.
     *
     * @return true if token should be persisted, false if user must re-enter each launch
     */
    public boolean shouldStoreToken()
    {
        return "true".equals(getStoreToken());
    }

    /**
     * Checks if the auto-refresh countdown timer should be displayed.
     *
     * @return true if countdown should be shown, false otherwise
     */
    public boolean shouldShowRefreshCountdown()
    {
        return "true".equals(getShowRefreshCountdown());
    }

    /**
     * Gets the last viewed week start date as a LocalDate object.
     *
     * @return LocalDate representing the last viewed week start, or null if not set
     */
    public LocalDate getLastViewedWeekStartAsDate()
    {
        String dateStr = getLastViewedWeekStart();
        return dateStr != null && !dateStr.isEmpty() ? LocalDate.parse(dateStr) : null;
    }

    /**
     * Gets the last viewed day as a LocalDate object.
     *
     * @return LocalDate representing the last viewed day, or null if not set
     */
    public LocalDate getLastViewedDayAsDate()
    {
        String dateStr = getLastViewedDay();
        return dateStr != null && !dateStr.isEmpty() ? LocalDate.parse(dateStr) : null;
    }

    /**
     * Sets the Canvas API authentication token.
     *
     * @param token the authentication token to store
     */
    public void setAuthToken(String token)
    {
        properties.put("authToken", token);
    }

    /**
     * Sets the login completion status.
     *
     * @param completed true if login is completed, false otherwise
     */
    public void setLoginCompleted(boolean completed)
    {
        properties.put("loginCompleted", String.valueOf(completed));
    }

    /**
     * Sets the local data storage preference.
     *
     * @param storeData true to enable CSV storage, false to disable
     */
    public void setStoreData(boolean storeData)
    {
        properties.put("storeData", String.valueOf(storeData));
    }

    /**
     * Sets the token storage preference.
     *
     * @param storeToken true to store token in .profile, false to require re-entry
     */
    public void setStoreToken(boolean storeToken)
    {
        properties.put("storeToken", String.valueOf(storeToken));
    }

    /**
     * Sets the week start day preference.
     *
     * @param weekStart monday or sunday indicating the first day of the week
     */
    public void setWeekStart(String weekStart)
    {
        properties.put("weekStart", weekStart);
    }

    /**
     * Sets the default application view.
     *
     * @param defaultView week or day indicating the default view
     */
    public void setDefaultView(String defaultView)
    {
        properties.put("defaultView", defaultView);
    }

    /**
     * Sets the application opening behavior.
     *
     * @param openTo current_week or last_viewed indicating where to open
     */
    public void setOpenTo(String openTo)
    {
        properties.put("openTo", openTo);
    }

    /**
     * Sets the default assignment sorting mode.
     *
     * @param sortMode date_time or difficulty for primary sort method
     */
    public void setDefaultSortMode(String sortMode)
    {
        properties.put("defaultSort.mode", sortMode);
    }

    /**
     * Sets the difficulty sort order preference.
     *
     * @param difficultyOrder ascending or descending for difficulty sorting
     */
    public void setDefaultSortDifficultyOrder(String difficultyOrder)
    {
        properties.put("defaultSort.difficultyOrder", difficultyOrder);
    }

    /**
     * Sets the refresh countdown display preference.
     *
     * @param showCountdown true to show countdown, false to hide
     */
    public void setShowRefreshCountdown(boolean showCountdown)
    {
        properties.put("showRefreshCountdown", String.valueOf(showCountdown));
    }

    /**
     * Sets the last viewed week start date.
     *
     * @param weekStart YYYY-MM-DD formatted date string for the week start
     */
    public void setLastViewedWeekStart(String weekStart)
    {
        properties.put("lastViewedWeekStart", weekStart);
    }

    /**
     * Sets the last viewed day.
     *
     * @param day YYYY-MM-DD formatted date string for the day
     */
    public void setLastViewedDay(String day)
    {
        properties.put("lastViewedDay", day);
    }

    /**
     * Sets the UI theme.
     *
     * @param theme theme name (cougar, dark, or sunset)
     */
    public void setTheme(String theme) {
        properties.put("theme", theme);
    }

    /**
     * Gets a copy of all config properties for serialization.
     *
     * @return a new HashMap containing all current config key-value pairs
     */
    public Map<String, String> getAllProperties()
    {
        return new HashMap<>(properties);
    }
}
