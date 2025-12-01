package cz.vitekform.rPGCore.pluginUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.logging.Logger;

/**
 * Utility class for uploading files to catbox.moe file hosting service.
 */
public class CatboxUploader {

    private static final String CATBOX_API_URL = "https://catbox.moe/user/api.php";

    private final Logger logger;

    public CatboxUploader(Logger logger) {
        this.logger = logger;
    }

    /**
     * Uploads a file to catbox.moe and returns the download URL.
     *
     * @param file The file to upload
     * @return The download URL, or null if upload failed
     */
    public String upload(File file) {
        if (!file.exists()) {
            logger.warning("Cannot upload file: file does not exist: " + file.getAbsolutePath());
            return null;
        }

        // Generate unique boundary per upload
        String boundary = "----RPGCoreBoundary" + System.currentTimeMillis() + Math.random();

        try {
            URL url = URI.create(CATBOX_API_URL).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("User-Agent", "RPGCore-Plugin");

            try (OutputStream outputStream = connection.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {

                // Add reqtype field
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"reqtype\"\r\n\r\n");
                writer.append("fileupload").append("\r\n");

                // Add the file field
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"fileToUpload\"; filename=\"")
                        .append(file.getName())
                        .append("\"\r\n");
                writer.append("Content-Type: application/zip\r\n\r\n");
                writer.flush();

                // Write file content
                Files.copy(file.toPath(), outputStream);
                outputStream.flush();

                // End boundary
                writer.append("\r\n--").append(boundary).append("--\r\n");
                writer.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String response = reader.readLine();
                    if (response != null && response.startsWith("https://")) {
                        logger.info("Successfully uploaded resource pack to catbox.moe: " + response);
                        return response;
                    } else {
                        logger.warning("Unexpected response from catbox.moe: " + response);
                        return null;
                    }
                }
            } else {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    logger.warning("Failed to upload to catbox.moe. Response code: " + responseCode + ", Error: " + errorResponse);
                }
                return null;
            }
        } catch (Exception e) {
            logger.severe("Error uploading to catbox.moe: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verifies that a URL is accessible and returns the expected content hash.
     *
     * @param downloadUrl The URL to verify
     * @return true if the URL is accessible, false otherwise
     */
    public boolean verifyUrl(String downloadUrl) {
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            return false;
        }

        try {
            URL url = URI.create(downloadUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("User-Agent", "RPGCore-Plugin");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            boolean isValid = responseCode == HttpURLConnection.HTTP_OK;
            
            if (!isValid) {
                logger.warning("Resource pack URL verification failed. Response code: " + responseCode);
            }
            
            return isValid;
        } catch (Exception e) {
            logger.warning("Failed to verify resource pack URL: " + e.getMessage());
            return false;
        }
    }
}
