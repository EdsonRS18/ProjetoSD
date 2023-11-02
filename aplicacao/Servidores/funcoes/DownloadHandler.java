package Servidores.funcoes;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

public class DownloadHandler {
    private DataOutputStream dataOutputStream;

    public DownloadHandler(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public void sendFile(String fileName) throws IOException {
        File file = new File(fileName);

        if (file.exists() && file.isFile()) {
            dataOutputStream.writeUTF("FILE_FOUND");
            dataOutputStream.writeLong(file.length());

            FileInputStream fileInputStream = new FileInputStream(file);

            int bytes;
            byte[] buffer = new byte[4 * 1024];
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytes);
            }

            fileInputStream.close();
            System.out.println("Arquivo enviado: " + fileName);

            // Calcular o hash do arquivo e enviá-lo ao cliente
            String fileHash = calculateFileHash(file);
            dataOutputStream.writeUTF(fileHash);
        } else {
            dataOutputStream.writeUTF("FILE_NOT_FOUND");
        }
    }

    private String calculateFileHash(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fileInputStream = new FileInputStream(file);

            int bytes;
            byte[] buffer = new byte[4 * 1024];
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytes);
            }

            byte[] hashBytes = digest.digest();
            StringBuilder hashBuilder = new StringBuilder();
            for (byte b : hashBytes) {
                hashBuilder.append(String.format("%02x", b));
            }

            fileInputStream.close();
            return hashBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
