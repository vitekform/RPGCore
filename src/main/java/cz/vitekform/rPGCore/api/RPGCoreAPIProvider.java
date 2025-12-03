package cz.vitekform.rPGCore.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Provider class to obtain an instance of the RPGCoreAPI.
 * Other plugins should use this class to access the RPGCore API.
 * 
 * Example usage:
 * <pre>
 * RPGCoreAPI api = RPGCoreAPIProvider.getAPI();
 * if (api != null) {
 *     RPGItem item = api.getItem("adventurer_sword");
 *     // Use the item...
 * }
 * </pre>
 */
public class RPGCoreAPIProvider {
    
    private static RPGCoreAPI apiInstance = null;
    
    /**
     * Get the RPGCore API instance.
     * @return The RPGCoreAPI instance, or null if RPGCore is not loaded
     */
    public static RPGCoreAPI getAPI() {
        if (apiInstance == null) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("RPGCore");
            if (plugin != null && plugin.isEnabled()) {
                // Try to get API from plugin if it implements a method to get it
                if (plugin instanceof cz.vitekform.rPGCore.RPGCore) {
                    apiInstance = ((cz.vitekform.rPGCore.RPGCore) plugin).getAPI();
                }
            }
        }
        return apiInstance;
    }
    
    /**
     * Set the API instance. This should only be called by RPGCore itself during plugin initialization.
     * <b>WARNING:</b> Do not call this method from external plugins.
     * 
     * @param api The API instance to set
     */
    public static void setAPI(RPGCoreAPI api) {
        apiInstance = api;
    }
    
    /**
     * Check if the RPGCore API is available.
     * @return true if the API is available, false otherwise
     */
    public static boolean isAPIAvailable() {
        return getAPI() != null;
    }
}
