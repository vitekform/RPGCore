package cz.vitekform.rPGCore.listeners;

import cz.vitekform.rPGCore.blockdisplay.CustomBlockManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 * Handles chunk load/unload events to manage BlockDisplay entities for custom blocks.
 */
public class ChunkHandler implements Listener {
    
    private final CustomBlockManager blockManager;
    
    public ChunkHandler(CustomBlockManager blockManager) {
        this.blockManager = blockManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        // Restore BlockDisplay entities for custom blocks in the loaded chunk
        blockManager.restoreDisplaysInChunk(event.getChunk());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        // Clean up cache for the unloaded chunk
        blockManager.cleanupDisplaysInChunk(event.getChunk());
    }
}
