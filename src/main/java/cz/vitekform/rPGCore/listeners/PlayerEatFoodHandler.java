package cz.vitekform.rPGCore.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class PlayerEatFoodHandler implements Listener {

    @EventHandler
    public void onPlayerEatFood(org.bukkit.event.player.PlayerItemConsumeEvent event) {
        ItemStack i = event.getItem();
        if (i == null || i.getType().isAir()) {
            return; // Ignore if the item is null or air
        }
        if (i.getItemMeta().getPersistentDataContainer().has(new NamespacedKey("rpgcore", "is_food"))) {
            event.setCancelled(true); // Cancel the default eating behavior

            // Get the item from the player's inventory and consume it
            ItemStack inventoryItem = event.getPlayer().getInventory().getItem(event.getPlayer().getInventory().getHeldItemSlot());
            if (inventoryItem != null && !inventoryItem.getType().isAir()) {
                inventoryItem.setAmount(inventoryItem.getAmount() - 1); // Consume one item
            }

            int foodAmount = i.getItemMeta().getPersistentDataContainer()
                    .get(new NamespacedKey("rpgcore", "food_amount"), org.bukkit.persistence.PersistentDataType.INTEGER);
            float saturationAmount = i.getItemMeta().getPersistentDataContainer()
                    .get(new NamespacedKey("rpgcore", "saturation_amount"), org.bukkit.persistence.PersistentDataType.FLOAT);
            if (foodAmount > 0) {
                event.getPlayer().setFoodLevel(event.getPlayer().getFoodLevel() + foodAmount);
            }
            if (saturationAmount > 0) {
                event.getPlayer().setSaturation(event.getPlayer().getSaturation() + saturationAmount);
            }
        }
    }
}
