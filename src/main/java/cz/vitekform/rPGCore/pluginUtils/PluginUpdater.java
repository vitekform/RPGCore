package cz.vitekform.rPGCore.pluginUtils;

import cz.vitekform.rPGCore.objects.ReleaseChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class PluginUpdater {

    private static final String pluginVersion = "0.0.1";
    private static final int build = 1;
    private static final ReleaseChannel releaseChannel = ReleaseChannel.DEV;
    private static final String dataURL = "https://raw.githubusercontent.com/vitekform/RPGCore/master/src/main/java/cz/vitekform/rPGCore/pluginUtils/PluginUpdater.java";

    public static boolean isLatest() {
        try {
            URL url = new URL(dataURL);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String totalContent = "";
            String line;
            while ((line = reader.readLine()) != null) {
                totalContent += line;
            }
            reader.close();
            String[] lines = totalContent.split("\n");

            // Find the pluginVersion line
            String pluginVersionLine = "";
            for (String l : lines) {
                if (l.contains("private static final String pluginVersion")) {
                    pluginVersionLine = l;
                    break;
                }
            }
            // Find the build line
            String buildLine = "";
            for (String l : lines) {
                if (l.contains("private static final int build")) {
                    buildLine = l;
                    break;
                }
            }

            int plVer = Integer.parseInt(pluginVersionLine.split("\"")[1].replace(".", ""));
            int bld = Integer.parseInt(buildLine.split(" ")[5].replace(";", ""));
            if (plVer > Integer.parseInt(pluginVersion.replace(".", ""))) {
                return false;
            } else if (plVer == Integer.parseInt(pluginVersion.replace(".", ""))) {
                return bld >= build;
            } else {
                return true;
            }
        }   catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void update() {
        String plReleaseURL = "https://";
    }
}
