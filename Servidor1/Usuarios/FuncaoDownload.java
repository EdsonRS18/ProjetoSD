package Usuarios;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FuncaoDownload {
    public static void downloadFile(Socket socket, String fileName, DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF("DOWNLOAD");
        dataOutputStream.writeUTF(fileName);

        String response = dataInputStream.readUTF();
        if (response.equals("FILE_FOUND")) {
            long fileSize = dataInputStream.readLong();

            try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                int bytes;
                byte[] buffer = new byte[4 * 1024];
                while (fileSize > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                    fileOutputStream.write(buffer, 0, bytes);
                    fileSize -= bytes;
                }

                System.out.println("Arquivo baixado com sucesso.");
                displayFileHash(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (response.equals("FILE_NOT_FOUND")) {
            System.out.println("Arquivo não encontrado no servidor.");
        } else {
            System.out.println("Erro durante o download do arquivo.");
        }
    }

    private static void displayFileHash(String fileName) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
                byte[] data = new byte[1024];
                int bytes;
                while ((bytes = fileInputStream.read(data)) != -1) {
                    digest.update(data, 0, bytes);
                }

                byte[] hashBytes = digest.digest();
                String hash = bytesToHex(hashBytes);
                System.out.println("Hash do arquivo: " + hash);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }
}
