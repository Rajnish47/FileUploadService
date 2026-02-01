package com.rajnish.FileUploadService.util;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class CalculateChecksum {

    public static String calculateFileChecksum(Path filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (InputStream is = Files.newInputStream(filePath);
             DigestInputStream dis = new DigestInputStream(is, digest)) {

            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {
                // reading updates digest automatically
            }
        }

        byte[] hash = digest.digest();
        return bytesToHex(hash);
    }

    public static String calculateChunkChecksum(byte[] chunk) throws Exception
    {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(chunk);
        byte[] hash = digest.digest();
        return bytesToHex(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}