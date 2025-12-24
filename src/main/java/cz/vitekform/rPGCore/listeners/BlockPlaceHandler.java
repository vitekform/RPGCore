package cz.vitekform.rPGCore.listeners;

import cz.vitekform.rPGCore.BlockDictionary;
import cz.vitekform.rPGCore.objects.RPGBlock;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Handles block place events for custom RPG blocks.
 */
public class BlockPlaceHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        
        // Check if the item has RPGBlock metadata
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        
        NamespacedKey blockKey = new NamespacedKey("rpgcore", "rpg_block_id");
        if (!pdc.has(blockKey, PersistentDataType.STRING)) {
            return;
        }
        
        String blockId = pdc.get(blockKey, PersistentDataType.STRING);
        RPGBlock rpgBlock = BlockDictionary.getBlock(blockId);
        
        if (rpgBlock == null) {
            return;
        }
        
        // Set the block to the correct type
        Block placedBlock = event.getBlockPlaced();
        placedBlock.setType(rpgBlock.blockType);
        
        // Store block ID and custom model in block PDC if it's a TileState
        org.bukkit.block.BlockState blockState = placedBlock.getState();
        if (blockState instanceof org.bukkit.block.TileState) {
            org.bukkit.block.TileState tileState = (org.bukkit.block.TileState) blockState;
            PersistentDataContainer blockPdc = tileState.getPersistentDataContainer();
            blockPdc.set(blockKey, PersistentDataType.STRING, blockId);
            
            // Store custom block model if present
            if (rpgBlock.customBlockModel > 0) {
                NamespacedKey modelKey = new NamespacedKey("rpgcore", "custom_block_model");
                blockPdc.set(modelKey, PersistentDataType.INTEGER, rpgBlock.customBlockModel);
            }
            
            // Update the block state
            tileState.update(true, false);
        }
    }
}
