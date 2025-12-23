package cz.vitekform.rPGCore.objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RPGBlock {

    public final String MODEL_PATH;
    public final String MODEL_TYPE;
    public final String TEXTURE_PATH; // Only important in case that model path is not supplied (and model type is)
    public List<Material> minableWithMat;
    public List<RPGItem> minableWithR;
    public List<RPGItem> itemDropsR;
    public List<Material> itemDropsM;
    public Material blockType; // This is either the block type (in case that custom texture/model isnt provided) or it is set to blocks such as Noteblock (because we can use them to add textures)
    public List<ItemStack> getDrops() {
        List<ItemStack> items = new ArrayList<>();
        for (RPGItem item : itemDropsR) {
            items.add(item.build());
        }
        for (Material mat : itemDropsM) {
            items.add(new ItemStack(mat, 1));
        }
        return items;
    }

    public RPGBlock() {
        // Init empty instance
        minableWithMat = List.of(Material.DEBUG_STICK); // any because we use this for any
        minableWithR = new ArrayList<>();
        itemDropsR = new ArrayList<>();
        itemDropsM = new ArrayList<>();
        MODEL_PATH = "";
        MODEL_TYPE = "";
        TEXTURE_PATH = "";
        blockType = Material.STONE;
    }

    public boolean canBeMinedWith(ItemStack item) {
        boolean minableWithRB = false;
        // To Do:
        // Implement RPG item handling
        return minableWithMat.contains(item.getType()) || minableWithRB;
    }

    /* ToDo:
    Basically everything
    Custom texture handling
    Block toughness and resistance
    Block mining speed handling (using mining fatigue and stuff like this)
    Item dropping
    Loading from blocks.yml
     */
}
