package cz.vitekform.rPGCore.listeners;

import cz.vitekform.rPGCore.blockdisplay.CustomBlockManager;
import cz.vitekform.rPGCore.blockdisplay.MiningProgressTracker;
import cz.vitekform.rPGCore.objects.RPGBlock;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Handles block break events for custom RPG blocks with custom mining progress.
 */
public class BlockBreakHandler implements Listener {

    private final CustomBlockManager blockManager;
    private final MiningProgressTracker progressTracker;

    public BlockBreakHandler(CustomBlockManager blockManager, MiningProgressTracker progressTracker) {
        this.blockManager = blockManager;
        this.progressTracker = progressTracker;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        
        // Check if this is a barrier (custom block)
        if (block.getType() != Material.BARRIER) {
            return;
        }
        
        // Check if it's a custom block
        RPGBlock rpgBlock = blockManager.getRPGBlock(block);
        if (rpgBlock == null) {
            // Not a custom block, allow vanilla behavior
            return;
        }
        
        // Cancel the event - we handle breaking ourselves
        event.setCancelled(true);
        
        Player player = event.getPlayer();
        
        // Creative mode = instant break
        if (player.getGameMode() == GameMode.CREATIVE) {
            handleBlockBreak(player, block, rpgBlock);
            return;
        }
        
        // For survival/adventure, check mining progress
        float progress = progressTracker.getProgress(player.getUniqueId());
        if (progress >= 1.0f) {
            handleBlockBreak(player, block, rpgBlock);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockDamage(BlockDamageEvent event) {
        Block block = event.getBlock();
        
        // Check if this is a barrier (custom block)
        if (block.getType() != Material.BARRIER) {
            return;
        }
        
        // Check if it's a custom block
        RPGBlock rpgBlock = blockManager.getRPGBlock(block);
        if (rpgBlock == null) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        
        // Check if player can mine this block
        if (!rpgBlock.canBeMinedWith(tool)) {
            // Wrong tool or not allowed
            progressTracker.resetProgress(player.getUniqueId());
            return;
        }
        
        // Creative mode = instant break (handled in onBlockBreak)
        if (player.getGameMode() == GameMode.CREATIVE) {
            event.setInstaBreak(true);
            return;
        }
        
        // Update mining progress
        float progress = progressTracker.updateMiningProgress(player, block, rpgBlock);
        
        // Send block break animation to player
        // Stage 0-9 representing 0-100% progress
        int stage = (int) (progress * 10);
        stage = Math.min(stage, 9);
        
        // Send block damage packet (visual feedback)
        player.sendBlockDamage(block.getLocation(), stage);
        
        // Check if block should break
        if (progress >= 1.0f) {
            handleBlockBreak(player, block, rpgBlock);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up mining progress when player quits
        progressTracker.resetProgress(event.getPlayer().getUniqueId());
    }
    
    /**
     * Handles the actual breaking of a custom block.
     * 
     * @param player The player breaking the block
     * @param block The block being broken
     * @param rpgBlock The RPG block definition
     */
    private void handleBlockBreak(Player player, Block block, RPGBlock rpgBlock) {
        Location dropLocation = block.getLocation().add(0.5, 0.5, 0.5);
        
        // Drop items
        List<ItemStack> drops = rpgBlock.getDrops();
        for (ItemStack drop : drops) {
            block.getWorld().dropItemNaturally(dropLocation, drop);
        }
        
        // Remove the custom block (BARRIER + BlockDisplay)
        blockManager.removeCustomBlock(block.getLocation());
        
        // Reset mining progress
        progressTracker.resetProgress(player.getUniqueId());
        
        // Clear block damage visual
        player.sendBlockDamage(block.getLocation(), -1);
    }
}
