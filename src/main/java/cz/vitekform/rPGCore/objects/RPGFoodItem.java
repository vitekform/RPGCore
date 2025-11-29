package cz.vitekform.rPGCore.objects;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RPGFoodItem extends RPGItem {

    public int foodAmount;
    public float saturationAmount;

    @Override
    public ItemStack build() {
        ItemStack item = super.build();
        if (item != null) {
            List<Component> loreOld = item.lore();
            if (loreOld == null) {
                loreOld = new ArrayList<>();
            }
            loreOld.add(Component.text("Food: " + foodAmount).color(TextColor.color(117, 103, 26)));
            loreOld.add(Component.text("Saturation: " + saturationAmount).color(NamedTextColor.YELLOW));
            item.lore(loreOld);

            NamespacedKey isFoodKey = new NamespacedKey("rpgcore", "is_food");
            NamespacedKey foodAmountKey = new NamespacedKey("rpgcore", "food_amount");
            NamespacedKey saturationAmountKey = new NamespacedKey("rpgcore", "saturation_amount");

            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.getPersistentDataContainer().set(isFoodKey, org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
            itemMeta.getPersistentDataContainer().set(foodAmountKey, org.bukkit.persistence.PersistentDataType.INTEGER, foodAmount);
            itemMeta.getPersistentDataContainer().set(saturationAmountKey, org.bukkit.persistence.PersistentDataType.FLOAT, saturationAmount);
            item.setItemMeta(itemMeta);
        }
        return item;
    }
}
