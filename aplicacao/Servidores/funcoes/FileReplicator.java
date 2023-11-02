package Servidores.funcoes;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;

public class FileReplicator {
   
    public static void replicateFileToServer(String fileName, String serverIP, int serverPort) {
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            byte[] fileData = fileInputStream.readAllBytes();
            fileInputStream.close();

            // Calculate file hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fileData);
            String hash = bytesToHex(hashBytes);

            Socket socket = new Socket(serverIP, serverPort);

            // Create data output stream to send file details
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF("REPLICATE");
            dataOutputStream.writeUTF(fileName);
            dataOutputStream.writeUTF(hash);
            dataOutputStream.writeLong(fileData.length);
            dataOutputStream.flush();

            // Create data output stream to send file content
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(fileData);
            outputStream.flush();

            socket.close();

            //System.out.println("File replicated to server: " + serverIP + ":" + serverPort);
        } catch (Exception e) {
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
