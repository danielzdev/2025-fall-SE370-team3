package csusm.cougarplanner.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates Profile config values and ensures they conform to specifications.
 * Replaces invalid values with defaults and generates warning messages for UI display.
 *
 * Part of T05: Validate Profile values, replace invalid values with defaults,
 * and add warning messages for UI display.
 */
public class SettingsValidator
{
    private static final String DEFAULT_WEEK_START = "monday";
    private static final String DEFAULT_DEFAULT_VIEW = "week";
    private static final String DEFAULT_OPEN_TO = "current_week";
    private static final boolean DEFAULT_STORE_DATA = true;
    private static final boolean DEFAULT_STORE_TOKEN = true;
    private static final boolean DEFAULT_LOGIN_COMPLETED = false;
    private static final boolean DEFAULT_SHOW_REFRESH_COUNTDOWN = false;
    private static final String DEFAULT_SORT_MODE = "date_time";
    private static final String DEFAULT_SORT_DIFFICULTY_ORDER = "ascending";
    private static final String DEFAULT_THEME = "cougar";

    /**
     * Validates all Profile config values and replaces invalid values with defaults,
     * and generates warning messages for any invalid values that were replaced.
     *
     * @param profile the Profile object loaded from .profile file
     * @param existingErrors list of existing parsing errors from ProfileReader
     * @return ValidationResult containing the validated Profile & combined error/warning messages
     */
    public ValidationResult validateProfile(Profile profile, List<String> existingErrors)
    {
        List<String> validationWarnings = new ArrayList<>();

        String weekStart = profile.getWeekStart();
        if (!isValidWeekStart(weekStart))
        {
            profile.setWeekStart(DEFAULT_WEEK_START);
            validationWarnings.add("Invalid value for weekStart: '" + weekStart + "'. Replacing with default value: " + DEFAULT_WEEK_START + ".");
        }

        String defaultView = profile.getDefaultView();
        if (!isValidDefaultView(defaultView))
        {
            profile.setDefaultView(DEFAULT_DEFAULT_VIEW);
            validationWarnings.add("Invalid value for defaultView: '" + defaultView + "'. Replacing with default value: " + DEFAULT_DEFAULT_VIEW + ".");
        }

        String openTo = profile.getOpenTo();
        if (!isValidOpenTo(openTo))
        {
            profile.setOpenTo(DEFAULT_OPEN_TO);
            validationWarnings.add("Invalid value for openTo: '" + openTo + "'. Replacing with default value: " + DEFAULT_OPEN_TO + ".");
        }

        validateBooleanSetting(profile, "storeData", profile.getStoreData(), DEFAULT_STORE_DATA, validationWarnings);
        validateBooleanSetting(profile, "storeToken", profile.getStoreToken(), DEFAULT_STORE_TOKEN, validationWarnings);
        validateBooleanSetting(profile, "loginCompleted", profile.getLoginCompleted(), DEFAULT_LOGIN_COMPLETED, validationWarnings);
        validateBooleanSetting(profile, "showRefreshCountdown", profile.getShowRefreshCountdown(), DEFAULT_SHOW_REFRESH_COUNTDOWN, validationWarnings);

        String sortMode = profile.getDefaultSortMode();
        if (!isValidSortMode(sortMode))
        {
            profile.setDefaultSortMode(DEFAULT_SORT_MODE);
            validationWarnings.add("Invalid value for defaultSort.mode: '" + sortMode + "'. Replacing with default value: " + DEFAULT_SORT_MODE + ".");
        }

        String difficultyOrder = profile.getDefaultSortDifficultyOrder();
        if (!isValidDifficultyOrder(difficultyOrder))
        {
            profile.setDefaultSortDifficultyOrder(DEFAULT_SORT_DIFFICULTY_ORDER);
            validationWarnings.add("Invalid value for defaultSort.difficultyOrder: '" + difficultyOrder + "'. Replacing with default value: " + DEFAULT_SORT_DIFFICULTY_ORDER + ".");
        }

        String theme = profile.getTheme();
        if (!isValidTheme(theme)) {
            profile.setTheme(DEFAULT_THEME);
            validationWarnings.add("Invalid value for theme: '" + theme + "'. Replacing with default value: " + DEFAULT_THEME + ".");
        }

        // Combines existing errors with new validation warnings
        List<String> allMessages = new ArrayList<>();
        if (existingErrors != null)
        {
            allMessages.addAll(existingErrors);
        }
        allMessages.addAll(validationWarnings);

        return new ValidationResult(profile, allMessages);
    }

    /**
     * Validates boolean setting and updates the profile if invalid.
     * Uses string values for validation, but boolean setters for updates.
     *
     * @param profile the Profile object to update
     * @param settingName the name of the setting for error messages
     * @param currentStringValue the current string value for validation
     * @param defaultValue the default boolean value to use if invalid
     * @param warnings list to add warning messages to
     */
    private void validateBooleanSetting(Profile profile, String settingName, String currentStringValue, boolean defaultValue, List<String> warnings)
    {
        if (!isValidBoolean(currentStringValue))
        {
            setBooleanSetting(profile, settingName, defaultValue);
            warnings.add("Invalid value for " + settingName + ": '" + currentStringValue + "'. Replacing with default value: " + defaultValue + ".");
        }
    }

    /**
     * Sets boolean setting on the profile based on the setting name.
     *
     * @param profile the Profile object to update
     * @param settingName the name of the setting to update
     * @param value the boolean value to set
     */
    private void setBooleanSetting(Profile profile, String settingName, boolean value)
    {
        switch (settingName)
        {
            case "storeData":
                profile.setStoreData(value);
                break;
            case "storeToken":
                profile.setStoreToken(value);
                break;
            case "loginCompleted":
                profile.setLoginCompleted(value);
                break;
            case "showRefreshCountdown":
                profile.setShowRefreshCountdown(value);
                break;
        }
    }

    /**
     * Checks if weekStart value is valid.
     *
     * @param weekStart the weekStart value to validate
     * @return true if valid ("monday" or "sunday"), false otherwise
     */
    private boolean isValidWeekStart(String weekStart)
    {
        return weekStart != null && ("monday".equalsIgnoreCase(weekStart) || "sunday".equalsIgnoreCase(weekStart));
    }

    /**
     * Checks if defaultView value is valid.
     *
     * @param defaultView the defaultView value to validate
     * @return true if valid ("week" or "day"), false otherwise
     */
    private boolean isValidDefaultView(String defaultView)
    {
        return defaultView != null && ("week".equalsIgnoreCase(defaultView) || "day".equalsIgnoreCase(defaultView));
    }

    /**
     * Checks if openTo value is valid.
     *
     * @param openTo the openTo value to validate
     * @return true if valid ("current_week" or "last_viewed"), false otherwise
     */
    private boolean isValidOpenTo(String openTo)
    {
        return openTo != null && ("current_week".equalsIgnoreCase(openTo) || "last_viewed".equalsIgnoreCase(openTo));
    }

    /**
     * Checks if a boolean string value is valid.
     *
     * @param value the boolean string to validate
     * @return true if valid ("true" or "false"), false otherwise
     */
    private boolean isValidBoolean(String value)
    {
        return value != null && ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value));
    }

    /**
     * Checks if sort mode value is valid.
     *
     * @param sortMode the sort mode value to validate
     * @return true if valid ("date_time" or "difficulty"), false otherwise
     */
    private boolean isValidSortMode(String sortMode)
    {
        return sortMode != null && ("date_time".equalsIgnoreCase(sortMode) || "difficulty".equalsIgnoreCase(sortMode));
    }

    /**
     * Checks if difficulty order value is valid.
     *
     * @param difficultyOrder the difficulty order value to validate
     * @return true if valid ("ascending" or "descending"), false otherwise
     */
    private boolean isValidDifficultyOrder(String difficultyOrder)
    {
        return difficultyOrder != null && ("ascending".equalsIgnoreCase(difficultyOrder) || "descending".equalsIgnoreCase(difficultyOrder));
    }

    private boolean isValidTheme(String theme) {
        return theme != null && ("cougar".equalsIgnoreCase(theme) || "dark".equalsIgnoreCase(theme) || "sunset".equalsIgnoreCase(theme));
    }

    /**
     * Represents the result of profile validation.
     * Contains the validated Profile object and all error/warning messages.
     */
    public static class ValidationResult
    {
        private final Profile validatedProfile;
        private final List<String> allMessages;

        /**
         * Constructs a new ValidationResult.
         *
         * @param validatedProfile the Profile object with validated/defaulted values
         * @param allMessages combined list of parsing errors and validation warnings
         */
        public ValidationResult(Profile validatedProfile, List<String> allMessages)
        {
            this.validatedProfile = validatedProfile;
            this.allMessages = new ArrayList<>(allMessages);
        }

        /**
         * Gets the validated Profile object.
         *
         * @return the Profile with all invalid values replaced by defaults
         */
        public Profile getValidatedProfile()
        {
            return validatedProfile;
        }

        /**
         * Gets all error and warning messages.
         *
         * @return combined list of parsing errors (from T01) and validation warnings (from T05)
         */
        public List<String> getAllMessages()
        {
            return allMessages;
        }

        /**
         * Checks if any errors or warnings were generated during validation.
         *
         * @return true if one or more messages exist, false if none
         */
        public boolean hasMessages()
        {
            return !allMessages.isEmpty();
        }
    }
}