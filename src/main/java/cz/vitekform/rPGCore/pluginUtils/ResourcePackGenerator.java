package cz.vitekform.rPGCore.pluginUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cz.vitekform.rPGCore.BlockDictionary;
import cz.vitekform.rPGCore.ItemDictionary;
import cz.vitekform.rPGCore.objects.RPGBlock;
import cz.vitekform.rPGCore.objects.RPGItem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Generates a resource pack containing custom models and textures for RPGCore items.
 * <p>
 * Items can specify:
 * - texture.path: Path to texture file in /plugins/RPGCore/items/textures/
 * - model.path: Path to custom model file in /plugins/RPGCore/items/models/
 * - model.type: Auto-generate model based on type (e.g., "handheld", "generated")
 * <p>
 * If neither model.path nor model.type is present, vanilla model is used.
 * <p>
 * The generated resource pack is automatically uploaded to catbox.moe and the
 * download URL and SHA-1 hash are stored for player resource pack loading.
 */
public class ResourcePackGenerator {

    private static final String NAMESPACE = "rpgcore";
    private static final int PACK_FORMAT = 46; // Minecraft 1.21.4
    private static final String RESOURCEPACK_CONFIG = "resourcepack.yml";

    private final JavaPlugin plugin;
    private final Logger logger;
    private final File dataFolder;
    private final File texturesFolder;
    private final File modelsFolder;
    private final File armorTexturesFolder;
    private final File blockTexturesFolder;
    private final File blockModelsFolder;
    private final File generatedFolder;
    private final Gson gson;
    private final CatboxUploader uploader;

    // Stored resource pack info for player loading
    private static String resourcePackUrl;
    private static byte[] resourcePackHash;
    private static boolean resourcePackReady = false;

    public ResourcePackGenerator(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dataFolder = plugin.getDataFolder();
        this.texturesFolder = new File(dataFolder, "items/textures");
        this.modelsFolder = new File(dataFolder, "items/models");
        this.armorTexturesFolder = new File(dataFolder, "items/textures/armor");
        this.blockTexturesFolder = new File(dataFolder, "blocks/textures");
        this.blockModelsFolder = new File(dataFolder, "blocks/models");
        this.generatedFolder = new File(dataFolder, "generated");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.uploader = new CatboxUploader(logger);
    }

    /**
     * Gets the resource pack download URL.
     * @return The URL or null if not available
     */
    public static String getResourcePackUrl() {
        return resourcePackUrl;
    }

    /**
     * Gets the resource pack SHA-1 hash as bytes.
     * @return The hash bytes or null if not available
     */
    public static byte[] getResourcePackHash() {
        return resourcePackHash;
    }

    /**
     * Checks if the resource pack is ready to be sent to players.
     * @return true if ready, false otherwise
     */
    public static boolean isResourcePackReady() {
        return resourcePackReady;
    }

    /**
     * Generates the resource pack for all loaded items.
     * Also handles uploading to catbox.moe and verifying the stored URL.
     */
    public void generate() {
        logger.info("Starting resource pack generation...");

        // Ensure directories exist
        ensureDirectories();

        // Load stored resource pack config
        File configFile = new File(dataFolder, RESOURCEPACK_CONFIG);
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Collect items that need custom models/textures
        Map<String, RPGItem> itemsWithCustomAssets = collectCustomAssetItems();
        
        // Collect blocks that need custom models/textures
        Map<String, RPGBlock> blocksWithCustomAssets = collectCustomAssetBlocks();

        if (itemsWithCustomAssets.isEmpty() && blocksWithCustomAssets.isEmpty()) {
            logger.info("No items or blocks with custom textures/models found. Skipping resource pack generation.");
            resourcePackReady = false;
            return;
        }

        logger.info("Found " + itemsWithCustomAssets.size() + " items with custom assets.");
        logger.info("Found " + blocksWithCustomAssets.size() + " blocks with custom assets.");

        // Generate the resource pack
        File tempZip = new File(generatedFolder, "resourcepack.zip.tmp");
        File finalZip = new File(generatedFolder, "resourcepack.zip");

        try {
            generateResourcePack(itemsWithCustomAssets, blocksWithCustomAssets, tempZip);

            String newHash = calculateFileHash(tempZip);
            boolean packChanged = true;

            // Check if we need to replace the existing pack
            if (finalZip.exists()) {
                String existingHash = calculateFileHash(finalZip);

                if (existingHash.equals(newHash)) {
                    logger.info("Resource pack unchanged. Keeping existing file.");
                    Files.delete(tempZip.toPath());
                    packChanged = false;
                } else {
                    logger.info("Resource pack changed. Replacing existing file.");
                    Files.move(tempZip.toPath(), finalZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                Files.move(tempZip.toPath(), finalZip.toPath());
            }

            logger.info("Resource pack generated at: " + finalZip.getAbsolutePath());

            // Calculate SHA-1 hash for the final zip (Minecraft uses SHA-1)
            String sha1Hash = calculateFileSha1(finalZip);
            
            // Check if we have a stored URL and if it's still valid
            String storedUrl = config.getString("url");
            String storedHash = config.getString("sha1");

            boolean needsUpload = packChanged || storedUrl == null || storedHash == null;
            
            // If pack didn't change but we have stored info, verify the URL is still accessible
            if (!needsUpload && storedUrl != null && storedHash != null) {
                if (storedHash.equals(sha1Hash) && uploader.verifyUrl(storedUrl)) {
                    logger.info("Stored resource pack URL is still valid. Using existing upload.");
                    resourcePackUrl = storedUrl;
                    resourcePackHash = hexStringToByteArray(sha1Hash);
                    resourcePackReady = true;
                    return;
                } else {
                    logger.info("Stored resource pack URL is invalid or hash mismatch. Re-uploading...");
                    needsUpload = true;
                }
            }

            // Upload to catbox.moe if needed
            if (needsUpload) {
                logger.info("Uploading resource pack to catbox.moe...");
                String uploadedUrl = uploader.upload(finalZip);
                
                if (uploadedUrl != null) {
                    // Store the URL and hash
                    config.set("url", uploadedUrl);
                    config.set("sha1", sha1Hash);
                    config.save(configFile);
                    
                    resourcePackUrl = uploadedUrl;
                    resourcePackHash = hexStringToByteArray(sha1Hash);
                    resourcePackReady = true;
                    
                    logger.info("Resource pack uploaded successfully!");
                    logger.info("URL: " + uploadedUrl);
                    logger.info("SHA-1: " + sha1Hash);
                } else {
                    logger.severe("Failed to upload resource pack to catbox.moe!");
                    resourcePackReady = false;
                }
            }

        } catch (Exception e) {
            logger.severe("Failed to generate resource pack: " + e.getMessage());
            e.printStackTrace();
            resourcePackReady = false;
        }
    }

    /**
     * Calculates the SHA-1 hash of a file (required by Minecraft for resource packs).
     */
    private String calculateFileSha1(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Converts a hex string to a byte array.
     */
    private static byte[] hexStringToByteArray(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }
        // Ensure even length
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private void ensureDirectories() {
        if (!texturesFolder.exists() && !texturesFolder.mkdirs()) {
            logger.warning("Failed to create textures directory: " + texturesFolder.getAbsolutePath());
        }
        if (!modelsFolder.exists() && !modelsFolder.mkdirs()) {
            logger.warning("Failed to create models directory: " + modelsFolder.getAbsolutePath());
        }
        if (!armorTexturesFolder.exists() && !armorTexturesFolder.mkdirs()) {
            logger.warning("Failed to create armor textures directory: " + armorTexturesFolder.getAbsolutePath());
        }
        if (!blockTexturesFolder.exists() && !blockTexturesFolder.mkdirs()) {
            logger.warning("Failed to create block textures directory: " + blockTexturesFolder.getAbsolutePath());
        }
        if (!blockModelsFolder.exists() && !blockModelsFolder.mkdirs()) {
            logger.warning("Failed to create block models directory: " + blockModelsFolder.getAbsolutePath());
        }
        if (!generatedFolder.exists() && !generatedFolder.mkdirs()) {
            logger.warning("Failed to create generated directory: " + generatedFolder.getAbsolutePath());
        }
    }

    private Map<String, RPGItem> collectCustomAssetItems() {
        Map<String, RPGItem> result = new LinkedHashMap<>();

        for (Map.Entry<String, RPGItem> entry : ItemDictionary.items.entrySet()) {
            RPGItem item = entry.getValue();
            // Item needs custom assets if it has modelPath, modelType, or armorTexturePath defined
            if (item.modelPath != null || item.modelType != null || item.armorTexturePath != null) {
                result.put(entry.getKey(), item);
            }
        }

        return result;
    }

    private Map<String, RPGBlock> collectCustomAssetBlocks() {
        Map<String, RPGBlock> result = new LinkedHashMap<>();

        for (Map.Entry<String, RPGBlock> entry : BlockDictionary.blocks.entrySet()) {
            RPGBlock block = entry.getValue();
            // Block needs custom assets if it has modelPath, modelType, or texturePath defined
            if (block.modelPath != null || block.modelType != null || block.texturePath != null) {
                result.put(entry.getKey(), block);
            }
        }

        return result;
    }

    private void generateResourcePack(Map<String, RPGItem> items, Map<String, RPGBlock> blocks, File outputFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile))) {
            // Add pack.mcmeta
            addPackMcmeta(zos);

            // Process each item
            for (Map.Entry<String, RPGItem> entry : items.entrySet()) {
                String itemKey = entry.getKey();
                RPGItem item = entry.getValue();

                processItem(zos, itemKey, item);
            }
            
            // Process each block
            for (Map.Entry<String, RPGBlock> entry : blocks.entrySet()) {
                String blockKey = entry.getKey();
                RPGBlock block = entry.getValue();

                processBlock(zos, blockKey, block);
            }
        }
    }

    private void addPackMcmeta(ZipOutputStream zos) throws IOException {
        JsonObject packMcmeta = new JsonObject();
        JsonObject pack = new JsonObject();
        pack.addProperty("pack_format", PACK_FORMAT);
        pack.addProperty("description", "RPGCore Custom Resource Pack");
        packMcmeta.add("pack", pack);

        zos.putNextEntry(new ZipEntry("pack.mcmeta"));
        zos.write(gson.toJson(packMcmeta).getBytes());
        zos.closeEntry();
    }

    private void processItem(ZipOutputStream zos, String itemKey, RPGItem item) throws IOException {
        String modelKeyName = itemKey.toLowerCase().replace(" ", "_");
        boolean modelAdded = false;

        // Handle texture
        if (item.texturePath != null) {
            addTexture(zos, itemKey, item.texturePath);
        }

        // Handle model
        if (item.modelPath != null) {
            // Use custom model file
            if (addCustomModel(zos, itemKey, item.modelPath)) {
                modelAdded = true;
            }
        } else if (item.modelType != null) {
            // Validate that texturePath is present for model.type
            if (item.texturePath == null) {
                logger.warning("Item " + itemKey + " has model.type but no texture.path. Skipping model generation.");
            } else {
                // Generate model based on type
                generateModel(zos, itemKey, item);
                modelAdded = true;
            }
        }
        
        // Only set the customModelKey and create item definition if a model was actually added
        if (modelAdded) {
            item.customModelKey = NAMESPACE + ":" + modelKeyName;
            // Add item definition file for Minecraft 1.21.4+
            addItemDefinition(zos, itemKey, modelKeyName);
        }
        
        // Handle armor textures (for when armor is worn)
        if (item.armorTexturePath != null) {
            if (addArmorTexture(zos, itemKey, item)) {
                // Create equipment model and set the equipment model key
                addEquipmentModel(zos, itemKey, item);
                item.equipmentModelKey = NAMESPACE + ":" + modelKeyName;
            }
        }
    }

    /**
     * Creates an item definition JSON file required by Minecraft 1.21.4+.
     * This file is stored at assets/<namespace>/items/<item_name>.json and references the model.
     */
    private void addItemDefinition(ZipOutputStream zos, String itemKey, String modelKeyName) throws IOException {
        String zipPath = "assets/" + NAMESPACE + "/items/" + modelKeyName + ".json";

        JsonObject itemDefinition = new JsonObject();
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty("model", NAMESPACE + ":item/" + modelKeyName);
        itemDefinition.add("model", model);

        zos.putNextEntry(new ZipEntry(zipPath));
        zos.write(gson.toJson(itemDefinition).getBytes());
        zos.closeEntry();

        logger.info("Added item definition for " + itemKey + ": " + zipPath);
    }

    private void addTexture(ZipOutputStream zos, String itemKey, String texturePath) throws IOException {
        File textureFile = new File(texturesFolder, texturePath);

        if (!textureFile.exists()) {
            logger.warning("Texture file not found for item " + itemKey + ": " + texturePath);
            return;
        }

        String textureKeyName = itemKey.toLowerCase().replace(" ", "_");
        String zipPath = "assets/" + NAMESPACE + "/textures/item/" + textureKeyName + getFileExtension(texturePath);

        zos.putNextEntry(new ZipEntry(zipPath));
        Files.copy(textureFile.toPath(), zos);
        zos.closeEntry();

        logger.info("Added texture for " + itemKey + ": " + zipPath);
    }

    private boolean addCustomModel(ZipOutputStream zos, String itemKey, String modelPath) throws IOException {
        File modelFile = new File(modelsFolder, modelPath);

        if (!modelFile.exists()) {
            logger.warning("Model file not found for item " + itemKey + ": " + modelPath);
            return false;
        }

        String modelKeyName = itemKey.toLowerCase().replace(" ", "_");
        String zipPath = "assets/" + NAMESPACE + "/models/item/" + modelKeyName + ".json";

        zos.putNextEntry(new ZipEntry(zipPath));
        Files.copy(modelFile.toPath(), zos);
        zos.closeEntry();

        logger.info("Added custom model for " + itemKey + ": " + zipPath);
        return true;
    }

    private void generateModel(ZipOutputStream zos, String itemKey, RPGItem item) throws IOException {
        String modelKeyName = itemKey.toLowerCase().replace(" ", "_");
        String zipPath = "assets/" + NAMESPACE + "/models/item/" + modelKeyName + ".json";

        JsonObject model = createModelJson(item.modelType, modelKeyName);

        zos.putNextEntry(new ZipEntry(zipPath));
        zos.write(gson.toJson(model).getBytes());
        zos.closeEntry();

        logger.info("Generated model for " + itemKey + " (type: " + item.modelType + "): " + zipPath);
    }

    private JsonObject createModelJson(String modelType, String itemKeyName) {
        JsonObject model = new JsonObject();

        // Determine the parent based on model type
        String parent = switch (modelType.toLowerCase()) {
            case "handheld" -> "minecraft:item/handheld";
            case "handheld_rod" -> "minecraft:item/handheld_rod";
            case "generated" -> "minecraft:item/generated";
            case "bow" -> "minecraft:item/bow";
            case "crossbow" -> "minecraft:item/crossbow";
            case "shield" -> "minecraft:builtin/entity";
            case "trident" -> "minecraft:item/trident_in_hand";
            default -> "minecraft:item/generated";
        };

        model.addProperty("parent", parent);

        // Add textures - the texture reference always points to our item's texture
        // which is stored at the item key location in the resourcepack
        JsonObject textures = new JsonObject();
        String textureRef = NAMESPACE + ":item/" + itemKeyName;

        textures.addProperty("layer0", textureRef);
        model.add("textures", textures);

        return model;
    }

    /**
     * Adds an armor texture to the resource pack for when the armor is worn.
     * Armor textures go in assets/<namespace>/textures/entity/equipment/<layer_type>/<armor_name>.png
     * where layer_type is either "humanoid" (for helmet, chestplate, boots) or "humanoid_leggings" (for leggings).
     */
    private boolean addArmorTexture(ZipOutputStream zos, String itemKey, RPGItem item) throws IOException {
        File textureFile = new File(armorTexturesFolder, item.armorTexturePath);

        if (!textureFile.exists()) {
            logger.warning("Armor texture file not found for item " + itemKey + ": " + item.armorTexturePath);
            return false;
        }

        String armorKeyName = itemKey.toLowerCase().replace(" ", "_");
        
        // Determine layer type - default to humanoid if not specified
        String layerType = item.armorLayerType != null ? item.armorLayerType.toLowerCase() : "humanoid";
        if (!layerType.equals("humanoid") && !layerType.equals("humanoid_leggings")) {
            logger.warning("Invalid armor layer type for item " + itemKey + ": " + layerType + ". Using 'humanoid'.");
            layerType = "humanoid";
        }

        String zipPath = "assets/" + NAMESPACE + "/textures/entity/equipment/" + layerType + "/" + armorKeyName + getFileExtension(item.armorTexturePath);

        zos.putNextEntry(new ZipEntry(zipPath));
        Files.copy(textureFile.toPath(), zos);
        zos.closeEntry();

        logger.info("Added armor texture for " + itemKey + ": " + zipPath);
        return true;
    }

    /**
     * Creates an equipment model JSON file that defines how armor looks when worn.
     * Equipment models go in assets/<namespace>/equipment/<armor_name>.json
     * The model references the armor texture in the entity/equipment folder.
     */
    private void addEquipmentModel(ZipOutputStream zos, String itemKey, RPGItem item) throws IOException {
        String armorKeyName = itemKey.toLowerCase().replace(" ", "_");
        String zipPath = "assets/" + NAMESPACE + "/equipment/" + armorKeyName + ".json";

        JsonObject equipmentModel = new JsonObject();
        JsonObject layers = new JsonObject();
        
        // Determine layer type - default to humanoid if not specified
        String layerType = item.armorLayerType != null ? item.armorLayerType.toLowerCase() : "humanoid";
        if (!layerType.equals("humanoid") && !layerType.equals("humanoid_leggings")) {
            layerType = "humanoid";
        }
        
        // Create the layer array with texture reference
        JsonArray layerArray = new JsonArray();
        JsonObject layerEntry = new JsonObject();
        layerEntry.addProperty("texture", NAMESPACE + ":" + armorKeyName);
        layerArray.add(layerEntry);
        
        layers.add(layerType, layerArray);
        equipmentModel.add("layers", layers);

        zos.putNextEntry(new ZipEntry(zipPath));
        zos.write(gson.toJson(equipmentModel).getBytes());
        zos.closeEntry();

        logger.info("Added equipment model for " + itemKey + ": " + zipPath);
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot);
        }
        return ".png";
    }

    private String calculateFileHash(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * Processes a block for the resource pack.
     * Handles textures and models. Block states need to be configured manually or via external tools.
     */
    private void processBlock(ZipOutputStream zos, String blockKey, RPGBlock block) throws IOException {
        String modelKeyName = blockKey.toLowerCase().replace(" ", "_");
        boolean modelAdded = false;

        // Handle texture
        if (block.texturePath != null) {
            addBlockTexture(zos, blockKey, block.texturePath);
        }

        // Handle model
        if (block.modelPath != null) {
            // Use custom model file
            if (addCustomBlockModel(zos, blockKey, block.modelPath)) {
                modelAdded = true;
            }
        } else if (block.modelType != null) {
            // Validate that texturePath is present for model.type
            if (block.texturePath == null) {
                logger.warning("Block " + blockKey + " has model.type but no texture.path. Skipping model generation.");
            } else {
                // Generate model based on type
                generateBlockModel(zos, blockKey, block);
                modelAdded = true;
            }
        }
        
        // Set the customModelKey if a model was actually added
        if (modelAdded) {
            block.customModelKey = NAMESPACE + ":" + modelKeyName;
            logger.info("Block " + blockKey + " model available at: " + block.customModelKey);
            logger.info("Note: Resource pack integration requires custom configuration. For blocks using " +
                    "BARRIER or similar TileState-supporting materials, the customBlockModel value (" + 
                    block.customBlockModel + ") is stored in the block's PDC and can be used by resource " +
                    "pack predicates to render different models.");
        }
    }
    
    /**
     * Adds a block texture to the resource pack.
     */
    private void addBlockTexture(ZipOutputStream zos, String blockKey, String texturePath) throws IOException {
        File textureFile = new File(blockTexturesFolder, texturePath);

        if (!textureFile.exists()) {
            logger.warning("Block texture file not found for block " + blockKey + ": " + texturePath);
            return;
        }

        String textureKeyName = blockKey.toLowerCase().replace(" ", "_");
        String zipPath = "assets/" + NAMESPACE + "/textures/block/" + textureKeyName + getFileExtension(texturePath);

        zos.putNextEntry(new ZipEntry(zipPath));
        Files.copy(textureFile.toPath(), zos);
        zos.closeEntry();

        logger.info("Added block texture for " + blockKey + ": " + zipPath);
    }
    
    /**
     * Adds a custom block model from a JSON file.
     */
    private boolean addCustomBlockModel(ZipOutputStream zos, String blockKey, String modelPath) throws IOException {
        File modelFile = new File(blockModelsFolder, modelPath);

        if (!modelFile.exists()) {
            logger.warning("Block model file not found for block " + blockKey + ": " + modelPath);
            return false;
        }

        String modelKeyName = blockKey.toLowerCase().replace(" ", "_");
        String zipPath = "assets/" + NAMESPACE + "/models/block/" + modelKeyName + ".json";

        zos.putNextEntry(new ZipEntry(zipPath));
        Files.copy(modelFile.toPath(), zos);
        zos.closeEntry();

        logger.info("Added custom block model for " + blockKey + ": " + zipPath);
        return true;
    }
    
    /**
     * Generates a block model based on the model type.
     */
    private void generateBlockModel(ZipOutputStream zos, String blockKey, RPGBlock block) throws IOException {
        String modelKeyName = blockKey.toLowerCase().replace(" ", "_");
        String zipPath = "assets/" + NAMESPACE + "/models/block/" + modelKeyName + ".json";

        JsonObject model = createBlockModelJson(block.modelType, modelKeyName);

        zos.putNextEntry(new ZipEntry(zipPath));
        zos.write(gson.toJson(model).getBytes());
        zos.closeEntry();

        logger.info("Generated block model for " + blockKey + " (type: " + block.modelType + "): " + zipPath);
    }
    
    /**
     * Creates a JSON model for a block based on its type.
     */
    private JsonObject createBlockModelJson(String modelType, String blockKeyName) {
        JsonObject model = new JsonObject();

        // Determine the parent based on model type
        String parent = switch (modelType.toLowerCase()) {
            case "cube_all" -> "minecraft:block/cube_all";
            case "cube" -> "minecraft:block/cube";
            case "cube_bottom_top" -> "minecraft:block/cube_bottom_top";
            case "cube_column" -> "minecraft:block/cube_column";
            case "cross" -> "minecraft:block/cross";
            case "orientable" -> "minecraft:block/orientable";
            default -> {
                logger.warning("Unknown model type '" + modelType + "' for block. Falling back to cube_all.");
                yield "minecraft:block/cube_all";
            }
        };

        model.addProperty("parent", parent);

        // Add textures
        JsonObject textures = new JsonObject();
        String textureRef = NAMESPACE + ":block/" + blockKeyName;

        // Apply texture based on model type
        switch (modelType.toLowerCase()) {
            case "cube_all" -> textures.addProperty("all", textureRef);
            case "cube" -> {
                textures.addProperty("particle", textureRef);
                textures.addProperty("north", textureRef);
                textures.addProperty("south", textureRef);
                textures.addProperty("east", textureRef);
                textures.addProperty("west", textureRef);
                textures.addProperty("up", textureRef);
                textures.addProperty("down", textureRef);
            }
            case "cube_bottom_top" -> {
                textures.addProperty("top", textureRef);
                textures.addProperty("bottom", textureRef);
                textures.addProperty("side", textureRef);
            }
            case "cube_column" -> {
                textures.addProperty("end", textureRef);
                textures.addProperty("side", textureRef);
            }
            case "orientable" -> {
                textures.addProperty("front", textureRef);
                textures.addProperty("side", textureRef);
                textures.addProperty("top", textureRef);
            }
            case "cross" -> textures.addProperty("cross", textureRef);
            default -> textures.addProperty("all", textureRef);
        }

        model.add("textures", textures);

        return model;
    }
}
