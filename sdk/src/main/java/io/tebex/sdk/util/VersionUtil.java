package io.tebex.sdk.util;

public class VersionUtil {
    public static boolean isNewerVersion(String currentVersion, String newVersion) {
        String[] currentVersionArray = currentVersion.split("\\.");
        String[] newVersionArray = newVersion.split("\\.");

        int length = Math.max(currentVersionArray.length, newVersionArray.length);

        for (int i = 0; i < length; i++) {
            int current = i < currentVersionArray.length ? Integer.parseInt(currentVersionArray[i]) : 0;
            int next = i < newVersionArray.length ? Integer.parseInt(newVersionArray[i]) : 0;

            if (next > current) {
                return true;
            } else if (next < current) {
                return false;
            }
        }

        return false;
    }
}
