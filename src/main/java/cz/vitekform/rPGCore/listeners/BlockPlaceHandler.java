package cz.vitekform.rPGCore.listeners;

import cz.vitekform.rPGCore.BlockDictionary;
import cz.vitekform.rPGCore.objects.RPGBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

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
        
        // Set the block to the correct type with correct properties
        Block placedBlock = event.getBlockPlaced();
        
        if (rpgBlock.blockType == Material.NOTE_BLOCK) {
            placedBlock.setType(Material.NOTE_BLOCK);
            
            // Set the note value to match the RPGBlock's state
            if (placedBlock.getBlockData() instanceof NoteBlock noteBlock) {
                noteBlock.setNote(org.bukkit.Note.natural(0, org.bukkit.Note.Tone.values()[rpgBlock.blockStateValue % 7]));
                placedBlock.setBlockData(noteBlock);
            }
        } else {
            placedBlock.setType(rpgBlock.blockType);
        }
    }
}
