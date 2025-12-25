package cz.vitekform.rPGCore.blockdisplay;

import cz.vitekform.rPGCore.objects.RPGBlock;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks block mining progress for players since BARRIER blocks cannot be mined normally.
 */
public class MiningProgressTracker {
    
    private final Map<UUID, MiningProgress> miningProgress = new HashMap<>();
    
    /**
     * Starts or continues tracking mining progress for a player.
     * 
     * @param player The player mining the block
     * @param block The block being mined
     * @param rpgBlock The RPG block definition
     * @return The current mining progress (0.0 to 1.0)
     */
    public float updateMiningProgress(Player player, Block block, RPGBlock rpgBlock) {
        UUID playerId = player.getUniqueId();
        Location blockLoc = block.getLocation();
        
        MiningProgress progress = miningProgress.get(playerId);
        
        // Check if player is mining a different block
        if (progress == null || !progress.location.equals(blockLoc)) {
            // Start new mining progress
            progress = new MiningProgress(blockLoc);
            miningProgress.put(playerId, progress);
        }
        
        // Update progress
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - progress.lastUpdateTime) / 1000.0f; // Convert to seconds
        progress.lastUpdateTime = currentTime;
        
        // Calculate mining speed
        float miningSpeed = calculateMiningSpeed(player, rpgBlock);
        
        // Update progress
        progress.progress += deltaTime * miningSpeed;
        
        // Clamp progress to [0, 1]
        progress.progress = Math.min(progress.progress, 1.0f);
        
        return progress.progress;
    }
    
    /**
     * Resets mining progress for a player.
     * 
     * @param playerId The player's UUID
     */
    public void resetProgress(UUID playerId) {
        miningProgress.remove(playerId);
    }
    
    /**
     * Gets the current mining progress for a player.
     * 
     * @param playerId The player's UUID
     * @return The progress (0.0 to 1.0), or 0.0 if not mining
     */
    public float getProgress(UUID playerId) {
        MiningProgress progress = miningProgress.get(playerId);
        return progress != null ? progress.progress : 0.0f;
    }
    
    /**
     * Calculates the mining speed for a player based on tool, enchantments, and block hardness.
     * 
     * @param player The player
     * @param rpgBlock The block being mined
     * @return The mining speed (blocks per second)
     */
    private float calculateMiningSpeed(Player player, RPGBlock rpgBlock) {
        // Creative mode = instant break
        if (player.getGameMode() == GameMode.CREATIVE) {
            return Float.MAX_VALUE;
        }
        
        ItemStack tool = player.getInventory().getItemInMainHand();
        float blockHardness = rpgBlock.hardness;
        
        // Base mining speed
        float baseSpeed = 1.0f;
        
        // Tool speed multiplier
        float toolMultiplier = getToolSpeedMultiplier(tool, rpgBlock);
        
        // Efficiency enchantment
        int efficiencyLevel = tool.getEnchantmentLevel(Enchantment.EFFICIENCY);
        float efficiencyMultiplier = 1.0f + (efficiencyLevel * efficiencyLevel + 1.0f);
        
        // Calculate final speed (reciprocal of time to break)
        // Formula: speed = (baseSpeed * toolMultiplier * efficiencyMultiplier) / hardness
        float speed = (baseSpeed * toolMultiplier * efficiencyMultiplier) / Math.max(blockHardness, 0.1f);
        
        return speed;
    }
    
    /**
     * Gets the tool speed multiplier based on the tool type and block requirements.
     * 
     * @param tool The tool being used
     * @param rpgBlock The block being mined
     * @return The speed multiplier
     */
    private float getToolSpeedMultiplier(ItemStack tool, RPGBlock rpgBlock) {
        if (tool == null || tool.getType() == Material.AIR) {
            // Hand mining
            return rpgBlock.canBeMinedWith(null) ? 1.0f : 0.2f;
        }
        
        Material toolType = tool.getType();
        
        // Check if tool is appropriate for this block
        if (!rpgBlock.canBeMinedWith(tool)) {
            return 0.2f; // Wrong tool penalty
        }
        
        // Tool type speed multipliers (vanilla-like)
        return switch (toolType) {
            case NETHERITE_PICKAXE, NETHERITE_AXE, NETHERITE_SHOVEL -> 9.0f;
            case DIAMOND_PICKAXE, DIAMOND_AXE, DIAMOND_SHOVEL -> 8.0f;
            case IRON_PICKAXE, IRON_AXE, IRON_SHOVEL -> 6.0f;
            case STONE_PICKAXE, STONE_AXE, STONE_SHOVEL -> 4.0f;
            case WOODEN_PICKAXE, WOODEN_AXE, WOODEN_SHOVEL -> 2.0f;
            case GOLDEN_PICKAXE, GOLDEN_AXE, GOLDEN_SHOVEL -> 12.0f; // Gold is fast but weak
            default -> 1.0f;
        };
    }
    
    /**
     * Cleans up stale mining progress (for players who stopped mining or logged out).
     */
    public void cleanupStaleProgress() {
        long currentTime = System.currentTimeMillis();
        miningProgress.entrySet().removeIf(entry -> {
            MiningProgress progress = entry.getValue();
            // Remove if not updated in last 5 seconds
            return (currentTime - progress.lastUpdateTime) > 5000;
        });
    }
    
    /**
     * Represents mining progress for a player on a specific block.
     */
    private static class MiningProgress {
        Location location;
        float progress;
        long lastUpdateTime;
        
        MiningProgress(Location location) {
            this.location = location;
            this.progress = 0.0f;
            this.lastUpdateTime = System.currentTimeMillis();
        }
    }
}
