package cz.vitekform.rPGCore.objects;

import cz.vitekform.rPGCore.ItemDictionary;
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
    private static final NamespacedKey CUSTOM_MODEL_KEY = new NamespacedKey("rpgcore", "custom_block_model");

    public String blockId; // Unique identifier for this block
    public String blockName;
    public Material blockType; // Base block type (e.g., NOTE_BLOCK for custom blocks)
    public int customBlockModel; // Custom block model number for resource pack (stored in block PDC)
    public List<Material> minableWithMat;
    public List<String> minableWithItems; // RPGItem keys that can mine this block
    public List<String> itemDropsRPG; // RPGItem keys for drops
    public List<Material> itemDropsMat;
    public float hardness; // Block mining hardness
    public float resistance; // Block explosion resistance
    
    // Resource pack related properties
    public String texturePath;  // Path to texture file in /plugins/RPGCore/blocks/textures/
    public String modelPath;    // Path to model file in /plugins/RPGCore/blocks/models/
    public String modelType;    // Model type for auto-generation (e.g., "cube", "cube_all")
    public String customModelKey; // The key used in the resourcepack for this block's model
    
    public RPGBlock() {
        this.blockId = "";
        this.blockName = "";
        this.blockType = Material.NOTE_BLOCK;
        this.customBlockModel = 0;
        this.minableWithMat = new ArrayList<>();
        this.minableWithItems = new ArrayList<>();
        this.itemDropsRPG = new ArrayList<>();
        this.itemDropsMat = new ArrayList<>();
        this.hardness = 1.0f;
        this.resistance = 1.0f;
        this.texturePath = null;
        this.modelPath = null;
        this.modelType = null;
        this.customModelKey = null;
    }
    
    /**
     * Gets all drops for this block
     * @return List of ItemStacks representing the drops
     */
    public List<ItemStack> getDrops(java.util.logging.Logger logger) {
        List<ItemStack> items = new ArrayList<>();
        for (String itemKey : itemDropsRPG) {
            RPGItem item = ItemDictionary.getItem(itemKey);
            if (item != null) {
                items.add(item.build());
            } else {
                logger.warning("RPG item key '" + itemKey + "' not found in drops for block '" + blockId + "'");
            }
        }
        for (Material mat : itemDropsMat) {
            items.add(new ItemStack(mat, 1));
        }
        return items;
    }
    
    /**
     * Builds an ItemStack representation of this block for player inventory
     * @return ItemStack with proper metadata
     */
    public ItemStack build() {
        ItemStack itemStack = new ItemStack(blockType);
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
            
            // Store custom block model number
            if (customBlockModel > 0) {
                pdc.set(CUSTOM_MODEL_KEY, PersistentDataType.INTEGER, customBlockModel);
            }
            
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
