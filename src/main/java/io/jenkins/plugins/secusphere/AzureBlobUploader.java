package io.jenkins.plugins.secusphere;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
import hudson.model.TaskListener;

import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


import java.io.IOException;

public class AzureBlobUploader {

    private final TaskListener listener;
    private final String accountName;
    private final String accountKey;
    private final String containerName;

    public AzureBlobUploader(TaskListener listener, String accountName, String accountKey, String containerName) {
        this.listener = listener;
        this.accountName = accountName;
        this.accountKey = accountKey;
        this.containerName = containerName;
    }

    public void uploadFile(String filePath, String fileName, String contentType, String category, String appName) throws IOException {
        listener.getLogger().println("Uploading file: " + filePath);

        // Generate the timestamp and attach it to the filename
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = now.format(formatter);
        String fileNameWithTimestamp = appName + "/" + category + "/" + fileName + "_" + timestamp;

        BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
                .endpoint(String.format("https://%s.blob.core.windows.net", accountName))
                .containerName(containerName)
                .sasToken(accountKey)
                .buildClient();

        // Use the filename with timestamp when getting the BlobClient
        BlobClient blobClient = blobContainerClient.getBlobClient(fileNameWithTimestamp);
        blobClient.uploadFromFile(filePath);
        listener.getLogger().println("File uploaded successfully.");
    }

}
