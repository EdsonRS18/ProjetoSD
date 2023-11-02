package Servidores.funcoes;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.io.FileInputStream;

public class UploadHandler {
    private DataInputStream dataInputStream;

    public UploadHandler(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
    }

    public void saveFile(String fileName, long fileSize) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);

        int bytes;
        byte[] buffer = new byte[4 * 1024];
        long bytesRemaining = fileSize;
        while (bytesRemaining > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            bytesRemaining -= bytes;
        }

        fileOutputStream.close();
        System.out.println("Nome do arquivo: " + fileName);

        // Cálculo do hash
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fileInputStream = new FileInputStream(fileName);

            byte[] data = new byte[1024];
            while ((bytes = fileInputStream.read(data)) != -1) {
                digest.update(data, 0, bytes);
            }

            byte[] hashBytes = digest.digest();
            String hash = bytesToHex(hashBytes);
            System.out.println("Hash do arquivo: " + hash);

            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    
}
