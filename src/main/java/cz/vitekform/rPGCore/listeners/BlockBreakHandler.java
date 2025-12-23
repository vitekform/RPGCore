package cz.vitekform.rPGCore.listeners;

import cz.vitekform.rPGCore.BlockDictionary;
import cz.vitekform.rPGCore.objects.RPGBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Handles block break events for custom RPG blocks.
 */
public class BlockBreakHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        
        // Check if this is a note block (our custom block type)
        if (block.getType() != Material.NOTE_BLOCK) {
            return;
        }
        
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof NoteBlock)) {
            return;
        }
        
        NoteBlock noteBlock = (NoteBlock) blockData;
        int noteValue = noteBlock.getNote().getId();
        
        // Find matching RPGBlock
        RPGBlock rpgBlock = findRPGBlockByNoteValue(noteValue);
        if (rpgBlock == null) {
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
        List<ItemStack> drops = rpgBlock.getDrops();
        
        for (ItemStack drop : drops) {
            block.getWorld().dropItemNaturally(dropLocation, drop);
        }
    }
    
    /**
     * Finds an RPGBlock by its note value.
     * @param noteValue The note value (0-24)
     * @return The RPGBlock or null if not found
     */
    private RPGBlock findRPGBlockByNoteValue(int noteValue) {
        for (RPGBlock block : BlockDictionary.blocks.values()) {
            if (block.blockType == Material.NOTE_BLOCK && block.blockStateValue == noteValue) {
                return block;
            }
        }
        return null;
    }
}
