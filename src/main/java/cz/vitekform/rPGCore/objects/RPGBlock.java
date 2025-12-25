package cz.vitekform.rPGCore.objects;

import cz.vitekform.rPGCore.ItemDictionary;
// import io.th0rgal.oraxen.api.OraxenItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class RPGBlock {

    private static final NamespacedKey BLOCK_ID_KEY = new NamespacedKey("rpgcore", "rpg_block_id");

    public String blockId; // Unique identifier for this block
    public String blockName;
    public Material blockType; // Base block type (e.g., BARRIER for custom blocks)
    public String oraxenItemId; // Oraxen item ID for this block's visual representation
    public List<Material> minableWithMat;
    public List<String> minableWithItems; // RPGItem keys that can mine this block
    public List<String> itemDropsRPG; // RPGItem keys for drops
    public List<Material> itemDropsMat;
    // TODO: hardness and resistance are currently configuration-only properties.
    // Implementation pending: These will be used to influence mining speed (via mining fatigue effects)
    // and explosion handling once those mechanics are implemented in BlockBreakHandler and explosion handlers.
    public float hardness; // Block mining hardness
    public float resistance; // Block explosion resistance
    
    public RPGBlock() {
        this.blockId = "";
        this.blockName = "";
        this.blockType = Material.BARRIER; // Default to BARRIER for custom blocks
        this.oraxenItemId = null;
        this.minableWithMat = new ArrayList<>();
        this.minableWithItems = new ArrayList<>();
        this.itemDropsRPG = new ArrayList<>();
        this.itemDropsMat = new ArrayList<>();
        this.hardness = 1.0f;
        this.resistance = 1.0f;
    }
    
    /**
     * Gets all drops for this block.
     * Invalid RPG item keys are silently skipped (validation happens during block loading).
     * @return List of ItemStacks representing the drops
     */
    public List<ItemStack> getDrops() {
        List<ItemStack> items = new ArrayList<>();
        for (String itemKey : itemDropsRPG) {
            RPGItem item = ItemDictionary.getItem(itemKey);
            if (item != null) {
                items.add(item.build());
            }
            // Invalid keys are silently skipped - validation happens in BlockLoader
        }
        for (Material mat : itemDropsMat) {
            items.add(new ItemStack(mat, 1));
        }
        return items;
    }
    
    /**
     * Builds an ItemStack representation of this block for player inventory.
     * Uses Oraxen item if oraxenItemId is specified, otherwise creates a vanilla item.
     * Note: Oraxen API calls are commented out - uncomment when Oraxen dependency is available.
     * @return ItemStack with proper metadata
     */
    public ItemStack build() {
        ItemStack itemStack;
        
        // Try to use Oraxen item if specified
        if (oraxenItemId != null && !oraxenItemId.isEmpty()) {
            /*
            var oraxenItem = OraxenItems.getItemById(oraxenItemId);
            if (oraxenItem != null) {
                itemStack = oraxenItem.build();
                
                // Add RPGCore block ID to the Oraxen item's PDC
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    PersistentDataContainer pdc = meta.getPersistentDataContainer();
                    pdc.set(BLOCK_ID_KEY, PersistentDataType.STRING, blockId);
                    itemStack.setItemMeta(meta);
                }
                
                return itemStack;
            }
            */
            // TODO: Uncomment above when Oraxen is available
        }
        
        // Fallback to vanilla item if no Oraxen item
        itemStack = new ItemStack(blockType);
        ItemMeta meta = itemStack.getItemMeta();
        
        if (meta != null) {
            // Set display name
            Component displayName = Component.text(blockName)
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false);
            meta.displayName(displayName);
            
            // Store block ID in PDC for identification when placed
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(BLOCK_ID_KEY, PersistentDataType.STRING, blockId);
            
            itemStack.setItemMeta(meta);
        }
        
        return itemStack;
    }

    /**
     * Checks if the block can be mined with the given item
     * @param item The ItemStack being used to mine
     * @return true if the block can be mined with this item
     */
    public boolean canBeMinedWith(ItemStack item) {
        // If no restrictions are defined, block can be mined with anything
        if (minableWithMat.isEmpty() && minableWithItems.isEmpty()) {
            return true;
        }
        
        // Null item means bare hand - only allowed if no restrictions
        if (item == null) {
            return false;
        }
        
        // Check if vanilla material matches
        if (minableWithMat.contains(item.getType())) {
            return true;
        }
        
        // Check if RPGItem matches
        // Note: RPGItem detection from ItemStack is intentionally not implemented in this version.
        // Blocks that specify minableWithItems currently rely solely on vanilla material checks above.
        
        return false;
    }
}
