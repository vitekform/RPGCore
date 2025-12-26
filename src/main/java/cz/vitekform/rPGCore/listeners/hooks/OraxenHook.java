package cz.vitekform.rPGCore.listeners.hooks;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import cz.vitekform.rPGCore.RPGCore;
import io.th0rgal.oraxen.api.OraxenBlocks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class OraxenHook implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (OraxenBlocks.isOraxenBlock(event.getBlock())) {
            String oraxenID = OraxenBlocks.getOraxenBlock(event.getBlock().getLocation()).getItemID();
            if (RPGCore.oraxenBlocksRPGDropsCache.containsKey(oraxenID)) {
                for (int i = 0; i < RPGCore.oraxenBlocksRPGDropsCache.get(oraxenID).size(); i++) {
                    ItemStack drop = RPGCore.oraxenBlocksRPGDropsCache.get(oraxenID).get(i);
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
                }
            }
            if (RPGCore.oraxenBlocksVanillaDropsCache.containsKey(oraxenID)) {
                for (int i = 0; i < RPGCore.oraxenBlocksVanillaDropsCache.get(oraxenID).size(); i++) {
                    ItemStack drop = RPGCore.oraxenBlocksVanillaDropsCache.get(oraxenID).get(i);
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
                }
            }
            if (RPGCore.oraxenBlocksExperienceCache.containsKey(oraxenID)) {
                int experience = RPGCore.oraxenBlocksExperienceCache.get(oraxenID);
                RPGCore.playerStorage.get(event.getPlayer().getUniqueId()).handleExpAdd(experience);
            }
        }
    }
}
