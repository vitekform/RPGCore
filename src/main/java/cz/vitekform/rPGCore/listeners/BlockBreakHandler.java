package cz.vitekform.rPGCore.listeners;

import cz.vitekform.rPGCore.BlockDictionary;
import cz.vitekform.rPGCore.objects.RPGBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.logging.Logger;

/**
 * Handles block break events for custom RPG blocks.
 */
public class BlockBreakHandler implements Listener {

    private final Logger logger;

    public BlockBreakHandler(Logger logger) {
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        
        // Check if this is a note block (our custom block type)
        if (block.getType() != Material.NOTE_BLOCK) {
            return;
        }
        
        // Try to get RPGBlock from block PDC
        RPGBlock rpgBlock = getRPGBlockFromBlock(block);
        if (rpgBlock == null) {
            // Not a custom block, allow vanilla behavior
            return;
        }
        
        // Check if player can mine this block
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        if (!rpgBlock.canBeMinedWith(tool)) {
            event.setCancelled(true);
            return;
        }
        
        // Handle custom drops
        event.setDropItems(false); // Cancel default drops
        Location dropLocation = block.getLocation().add(0.5, 0.5, 0.5);
        List<ItemStack> drops = rpgBlock.getDrops(logger);
        
        for (ItemStack drop : drops) {
            block.getWorld().dropItemNaturally(dropLocation, drop);
        }
    }
    
    /**
     * Gets the RPGBlock from a placed block's PDC.
     * @param block The block to check
     * @return The RPGBlock or null if not a custom block
     */
    private RPGBlock getRPGBlockFromBlock(Block block) {
        org.bukkit.block.BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.TileState)) {
            return null;
        }
        
        org.bukkit.block.TileState tileState = (org.bukkit.block.TileState) state;
        PersistentDataContainer pdc = tileState.getPersistentDataContainer();
        NamespacedKey blockIdKey = new NamespacedKey("rpgcore", "rpg_block_id");
        
        if (!pdc.has(blockIdKey, PersistentDataType.STRING)) {
            return null;
        }
        
        String blockId = pdc.get(blockIdKey, PersistentDataType.STRING);
        return BlockDictionary.getBlock(blockId);
    }
}
