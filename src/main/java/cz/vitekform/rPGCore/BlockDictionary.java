package cz.vitekform.rPGCore;

import cz.vitekform.rPGCore.objects.RPGBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockDictionary {

    public static final Map<String, RPGBlock> blocks = new HashMap<>();
    public static final Map<UUID, RPGBlock> blockRegistry = new HashMap<>();

    /**
     * Get a block by its string key from blocks.yml
     * @param key The block key (e.g., "copper_ore")
     * @return The RPGBlock or null if not found
     */
    public static RPGBlock getBlock(String key) {
        return blocks.get(key);
    }

    /**
     * Get a block by its UUID from blockRegistry
     * @param uuid The block UUID
     * @return The RPGBlock or null if not found
     */
    public static RPGBlock getBlock(UUID uuid) {
        return blockRegistry.get(uuid);
    }
}
