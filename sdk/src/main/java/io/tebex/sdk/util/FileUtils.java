package io.tebex.sdk.util;

import java.io.File;

public class FileUtils {
    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    boolean success = deleteDirectory(child);
                    if (!success) {
                        return false;
                    }
                }
            }
        }

        // Either file or an empty directory
        return dir.delete();
    }
}
