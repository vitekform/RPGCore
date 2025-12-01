package cz.vitekform.rPGCore.pluginUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import cz.vitekform.rPGCore.ItemDictionary;
import cz.vitekform.rPGCore.objects.RPGItem;
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
 */
public class ResourcePackGenerator {

    private static final String NAMESPACE = "rpgcore";
    private static final int PACK_FORMAT = 46; // Minecraft 1.21.4

    private final JavaPlugin plugin;
    private final Logger logger;
    private final File dataFolder;
    private final File texturesFolder;
    private final File modelsFolder;
    private final File generatedFolder;
    private final Gson gson;

    public ResourcePackGenerator(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dataFolder = plugin.getDataFolder();
        this.texturesFolder = new File(dataFolder, "items/textures");
        this.modelsFolder = new File(dataFolder, "items/models");
        this.generatedFolder = new File(dataFolder, "generated");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Generates the resource pack for all loaded items.
     */
    public void generate() {
        logger.info("Starting resource pack generation...");

        // Ensure directories exist
        ensureDirectories();

        // Collect items that need custom models/textures
        Map<String, RPGItem> itemsWithCustomAssets = collectCustomAssetItems();

        if (itemsWithCustomAssets.isEmpty()) {
            logger.info("No items with custom textures/models found. Skipping resource pack generation.");
            return;
        }

        logger.info("Found " + itemsWithCustomAssets.size() + " items with custom assets.");

        // Generate the resource pack
        File tempZip = new File(generatedFolder, "resourcepack.zip.tmp");
        File finalZip = new File(generatedFolder, "resourcepack.zip");

        try {
            generateResourcePack(itemsWithCustomAssets, tempZip);

            // Check if we need to replace the existing pack
            if (finalZip.exists()) {
                String existingHash = calculateFileHash(finalZip);
                String newHash = calculateFileHash(tempZip);

                if (existingHash.equals(newHash)) {
                    logger.info("Resource pack unchanged. Keeping existing file.");
                    Files.delete(tempZip.toPath());
                } else {
                    logger.info("Resource pack changed. Replacing existing file.");
                    Files.move(tempZip.toPath(), finalZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                Files.move(tempZip.toPath(), finalZip.toPath());
            }

            logger.info("Resource pack generated at: " + finalZip.getAbsolutePath());

        } catch (Exception e) {
            logger.severe("Failed to generate resource pack: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ensureDirectories() {
        if (!texturesFolder.exists() && !texturesFolder.mkdirs()) {
            logger.warning("Failed to create textures directory: " + texturesFolder.getAbsolutePath());
        }
        if (!modelsFolder.exists() && !modelsFolder.mkdirs()) {
            logger.warning("Failed to create models directory: " + modelsFolder.getAbsolutePath());
        }
        if (!generatedFolder.exists() && !generatedFolder.mkdirs()) {
            logger.warning("Failed to create generated directory: " + generatedFolder.getAbsolutePath());
        }
    }

    private Map<String, RPGItem> collectCustomAssetItems() {
        Map<String, RPGItem> result = new LinkedHashMap<>();

        for (Map.Entry<String, RPGItem> entry : ItemDictionary.items.entrySet()) {
            RPGItem item = entry.getValue();
            // Item needs custom assets if it has either model.path or model.type (and texture.path for model.type)
            if (item.modelPath != null || item.modelType != null) {
                result.put(entry.getKey(), item);
            }
        }

        return result;
    }

    private void generateResourcePack(Map<String, RPGItem> items, File outputFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile))) {
            // Add pack.mcmeta
            addPackMcmeta(zos);

            // Process each item
            for (Map.Entry<String, RPGItem> entry : items.entrySet()) {
                String itemKey = entry.getKey();
                RPGItem item = entry.getValue();

                processItem(zos, itemKey, item);
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
        
        // Only set the customModelKey if a model was actually added
        if (modelAdded) {
            item.customModelKey = NAMESPACE + ":" + modelKeyName;
        }
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
}
