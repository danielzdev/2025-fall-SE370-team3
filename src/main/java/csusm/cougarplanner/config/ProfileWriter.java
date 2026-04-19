package csusm.cougarplanner.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Writes application config to the .profile file in key=value format.
 * Implements atomic writes and maintains fixed key order with authToken always first.
 *
 * Part of T02: Build .profile writer that persists keys in fixed order with atomic writes.
 */
public class ProfileWriter
{
    private final Path profilePath; // Path to the .profile config file for writing

    /**
     * Constructs a ProfileWriter for the specified .profile file.
     *
     * @param profilePath the filesystem path to the .profile configuration file
     */
    public ProfileWriter(Path profilePath)
    {
        this.profilePath = profilePath;
    }

    /**
     * Creates parent directories if they don't exist
     * Writes to a temp file first to prevent data corruption
     * Uses atomic move operation to replace the target file
     * Maintains fixed key order with authToken always first
     *
     * @param profile the Profile object containing configuration to write
     * @throws IOException if the file cannot be written or directory cannot be created
     */
    public void writeProfile(Profile profile) throws IOException
    {
        // Creates directory if it doesn't exist
        Path parentDir = profilePath.getParent();
        if (parentDir != null && !Files.exists(parentDir))
        {
            Files.createDirectories(parentDir);
        }

        // Creates temporary file
        Path tempFile = profilePath.resolveSibling(profilePath.getFileName() + ".tmp");

        // Writes to temporary file with fixed order
        List<String> lines = buildProfileLines(profile);
        Files.write(tempFile, lines);

        // Atomic move
        Files.move(tempFile, profilePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * Updates specific fields in the existing .profile file while preserving other settings.
     * Reads current profile, applies updates, and writes back the modified configuration.
     * This method is designed for use by SettingsController and TokenController
     * to modify individual settings without affecting other configuration values.
     *
     * @param updates Map of key-value pairs to update in the profile
     * @throws IOException if the profile cannot be read or written
     */
    public void updateProfileFields(Map<String, String> updates) throws IOException
    {
        // Reads existing profile
        ProfileReader reader = new ProfileReader(profilePath);
        ProfileReader.ReadResult result = reader.readProfile();
        Profile profile = result.getProfile();

        // Applies updates
        Map<String, String> allProperties = profile.getAllProperties();
        allProperties.putAll(updates);

        // Writes updated profile
        writeProfile(new Profile(allProperties));
    }

    /**
     * Builds lines for the .profile file in fixed order.
     *
     * @param profile the Profile object containing config values
     * @return List of strings representing lines in the .profile file
     */
    private List<String> buildProfileLines(Profile profile)
    {
        List<String> lines = new ArrayList<>();
        Map<String, String> properties = profile.getAllProperties();

        // Fixed order with authToken always first
        String[] keyOrder =
                {
                        "authToken",                    // Always first line for security
                        "loginCompleted",               // Login status
                        "storeData",                    // CSV storage preference
                        "storeToken",                   // Token persistence preference
                        "weekStart",                    // Week start day (monday/sunday)
                        "defaultView",                  // Default view (week/day)
                        "openTo",                       // Open behavior (current_week/last_viewed)
                        "defaultSort.mode",             // Primary sort method
                        "defaultSort.difficultyOrder",  // Difficulty sort direction
                        "showRefreshCountdown",         // Countdown display preference
                        "lastViewedWeekStart",          // Last viewed week context
                        "lastViewedDay",                // Last viewed day context
                        "theme",                        // UI theme (cougar/dark/sunset)
                        "selectedView"                  // Last selected main view
                };

        for (String key : keyOrder)
        {
            String value = properties.get(key);
            if (value != null)
            {
                lines.add(key + "=" + value);
            }
        }

        return lines;
    }
}