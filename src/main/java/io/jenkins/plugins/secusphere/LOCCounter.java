package io.jenkins.plugins.secusphere;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.jenkinsci.remoting.RoleChecker;
import jenkins.MasterToSlaveFileCallable;

public class LOCCounter {

    public Map<String, Integer> countLinesOfCode(FilePath workspace) {
        Map<String, Integer> locData = new HashMap<>();
        try {
            workspace.act(new MasterToSlaveFileCallable<Void>() {
                @Override
                public Void invoke(File file, VirtualChannel channel) throws IOException, InterruptedException {
                    countLinesInDirectory(file, locData);
                    return null;
                }

                @Override
                public void checkRoles(RoleChecker roleChecker) throws SecurityException {
                    // You can leave this method empty if you don't need to check roles.
                }
            });
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return locData;
    }

    private void countLinesInDirectory(File directory, Map<String, Integer> locData) throws IOException {
        if (directory.isHidden() || directory.getName().startsWith(".")) {
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    countLinesInDirectory(file, locData);
                } else {
                    String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
                    int lines = countLinesInFile(file);
                    locData.put(extension, locData.getOrDefault(extension, 0) + lines);
                }
            }
        }
    }

    private int countLinesInFile(File file) throws IOException {
        int lines = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines++;
            }
        }
        return lines;
    }

    public Map<String, Integer> countFilesPerLanguage(FilePath workspace) {
        Map<String, Integer> fileData = new HashMap<>();
        try {
            workspace.act(new MasterToSlaveFileCallable<Void>() {
                @Override
                public Void invoke(File file, VirtualChannel channel) throws IOException, InterruptedException {
                    countFilesInDirectory(file, fileData);
                    return null;
                }

                @Override
                public void checkRoles(RoleChecker roleChecker) throws SecurityException {
                    // You can leave this method empty if you don't need to check roles.
                }
            });
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return fileData;
    }

    private void countFilesInDirectory(File directory, Map<String, Integer> fileData) {
        if (directory.isHidden() || directory.getName().startsWith(".")) {
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    countFilesInDirectory(file, fileData);
                } else {
                    String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
                    fileData.put(extension, fileData.getOrDefault(extension, 0) + 1);
                }
            }
        }
    }

}
