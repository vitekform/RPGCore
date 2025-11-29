package cz.vitekform.rPGCore.pluginUtils;

import com.google.gson.JsonObject;
import cz.vitekform.rPGCore.objects.ReleaseChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PluginUpdater {

    public static final String pluginVersion = "0.0.1";
    public static final int build = 15;
    private static final ReleaseChannel releaseChannel = ReleaseChannel.DEV;

    public static int latestBuild(String channel) {
        String url = "https://blob.build/api/builds/RPGCore/" + channel + "/latest";
        /*
        Schema of output:
        {
  "projectName": string,
  "releaseChannel": string,
  "buildId": number,
  "checksum": string,
  "fileDownloadUrl": string,
  "supportedVersions": string?,
  "dependencies": string[]?,
  "releaseNotes": string | null?,
  "commitHash": string | null?,
  "commitLink": string | null?,
}
         */

        try {
            URL urlObj = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(urlObj.openStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // Parse the JSON using GSON
            JsonObject jsonObject = new com.google.gson.JsonParser().parse(content.toString()).getAsJsonObject();
            JsonObject dataObject = jsonObject.getAsJsonObject("data");
            return dataObject.get("build_id").getAsInt();

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean isLatest() {
        int latest = latestBuild(buildChannelString());
        return latest == build;
    }

    public static Component update() {
        if (isLatest()) {
            return Component.text("You are already running the latest build of RPGCore.", NamedTextColor.GREEN);
        }
        String url = "https://blob.build/api/builds/RPGCore/" + buildChannelString() + "/latest";
        String downloadUrl = "";
        try {
            URL urlObj = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(urlObj.openStream()));
            String inputLine;
            String content = "";
            while ((inputLine = in.readLine()) != null) {
                content += inputLine + "\n";
            }
            in.close();

            // Parse the JSON using GSON
            JsonObject jsonObject = new com.google.gson.JsonParser().parse(content).getAsJsonObject();
            System.out.println(jsonObject);
            JsonObject dataObject = jsonObject.getAsJsonObject("data");
            downloadUrl = dataObject.get("file_download_url").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
            return Component.text("An error occurred while trying to update the plugin. Error occurred while fetching the latest version build URL!", NamedTextColor.RED);
        }
        // Download the file
        try (BufferedInputStream in = new BufferedInputStream(new URL(downloadUrl).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(System.getProperty("user.dir") + File.separator + "plugins" + File.separator + "RPGCoreNEW.jar")) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Component.text("An error occurred while trying to update the plugin. Error occurred while downloading the new version!", NamedTextColor.RED);
        }
        // Delete current file
        try {
            Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + File.separator + "plugins" + File.separator + "RPGCore.jar"));
        } catch (IOException e) {
            e.printStackTrace();
            return Component.text("An error occurred while trying to update the plugin. Error occurred while deleting the old version!", NamedTextColor.RED);
        }
        // Rename new file
        try {
            File f = new File(System.getProperty("user.dir") + File.separator + "plugins" + File.separator + "RPGCoreNEW.jar");
            f.renameTo(new File(System.getProperty("user.dir") + File.separator + "plugins" + File.separator + "RPGCore.jar"));
        } catch (Exception e) {
            e.printStackTrace();
            return Component.text("An error occurred while trying to update the plugin. Error occurred while renaming the new version!", NamedTextColor.RED);
        }
        return Component.text("Plugin updated successfully to build " + latestBuild(buildChannelString()) + "!", NamedTextColor.GREEN);
    }

    public static String buildChannelString() {
        switch (releaseChannel) {
            case DEV:
                return "Dev";
            case ALPHA:
                return "Alpha";
            case BETA:
                return "Beta";
            case RELEASE:
                return "Release";
            default:
                return "Unknown";
        }
    }
}
