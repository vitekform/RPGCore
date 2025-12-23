package cz.vitekform.rPGCore.objects;

import cz.vitekform.rPGCore.ItemDictionary;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RPGBlock {

    public String blockName;
    public Material blockType; // Base block type (e.g., NOTE_BLOCK for custom blocks)
    public int blockStateValue; // Block state value for note block properties
    public List<Material> minableWithMat;
    public List<String> minableWithItems; // RPGItem keys that can mine this block
    public List<String> itemDropsRPG; // RPGItem keys for drops
    public List<Material> itemDropsMaterial;
    public float hardness; // Block mining hardness
    public float resistance; // Block explosion resistance
    
    // Resource pack related properties
    public String texturePath;  // Path to texture file in /plugins/RPGCore/blocks/textures/
    public String modelPath;    // Path to model file in /plugins/RPGCore/blocks/models/
    public String modelType;    // Model type for auto-generation (e.g., "cube", "cube_all")
    public String customModelKey; // The key used in the resourcepack for this block's model
    
    public RPGBlock() {
        this.blockName = "";
        this.blockType = Material.NOTE_BLOCK;
        this.blockStateValue = 0;
        this.minableWithMat = new ArrayList<>();
        this.minableWithItems = new ArrayList<>();
        this.itemDropsRPG = new ArrayList<>();
        this.itemDropsMaterial = new ArrayList<>();
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
    public List<ItemStack> getDrops() {
        List<ItemStack> items = new ArrayList<>();
        for (String itemKey : itemDropsRPG) {
            RPGItem item = ItemDictionary.getItem(itemKey);
            if (item != null) {
                items.add(item.build());
            }
        }
        for (Material mat : itemDropsMaterial) {
            items.add(new ItemStack(mat, 1));
        }
        return items;
    }

    /**
     * Checks if the block can be mined with the given item
     * @param item The ItemStack being used to mine
     * @return true if the block can be mined with this item
     */
    public boolean canBeMinedWith(ItemStack item) {
        // If no restrictions are defined, block can be mined with anything
        if ((minableWithMat.isEmpty() && minableWithItems.isEmpty()) || item == null) {
            return true;
        }
        
        // Check if vanilla material matches
        if (minableWithMat.contains(item.getType())) {
            return true;
        }
        
        // Check if RPGItem matches
        // TODO: Implement RPGItem detection from ItemStack
        // This would check if the item is an RPGItem and if its key is in minableWithItems
        
        return false;
    }
}
