package cz.vitekform.rPGCore.listeners;

import cz.vitekform.rPGCore.BlockDictionary;
import cz.vitekform.rPGCore.blockdisplay.CustomBlockManager;
import cz.vitekform.rPGCore.objects.RPGBlock;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Handles block place events for custom RPG blocks using BARRIER + BlockDisplay.
 */
public class BlockPlaceHandler implements Listener {

    private static final NamespacedKey BLOCK_ID_KEY = new NamespacedKey("rpgcore", "rpg_block_id");
    
    private final CustomBlockManager blockManager;

    public BlockPlaceHandler(CustomBlockManager blockManager) {
        this.blockManager = blockManager;
    }

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
        
        if (!pdc.has(BLOCK_ID_KEY, PersistentDataType.STRING)) {
            return;
        }
        
        String blockId = pdc.get(BLOCK_ID_KEY, PersistentDataType.STRING);
        RPGBlock rpgBlock = BlockDictionary.getBlock(blockId);
        
        if (rpgBlock == null) {
            return;
        }
        
        // Cancel the event to prevent default block placement
        event.setCancelled(true);
        
        // Place the custom block using our manager
        boolean placed = blockManager.placeCustomBlock(event.getBlock().getLocation(), rpgBlock);
        
        // If placement failed, give the item back
        if (!placed) {
            return;
        }
        
        // Remove one item from the player's hand (if not in creative)
        if (event.getPlayer().getGameMode() != org.bukkit.GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }
    }
}
