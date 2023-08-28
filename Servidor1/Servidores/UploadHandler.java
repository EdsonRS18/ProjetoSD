package Servidores;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.List;

public class UploadHandler {
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private List<String> otherServerAddresses;

    public UploadHandler(DataInputStream dataInputStream, DataOutputStream dataOutputStream, List<String> otherServerAddresses) {
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.otherServerAddresses = otherServerAddresses;
    }

    public void handleUpload() {
        try {
            String fileName = dataInputStream.readUTF();
            long fileSize = dataInputStream.readLong();

            saveFile(fileName, fileSize);

            replicateFile(fileName);

            String fileHash = calculateFileHash(fileName);
            dataOutputStream.writeUTF("File uploaded successfully.");
            dataOutputStream.writeUTF("File hash: " + fileHash);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFile(String fileName, long fileSize) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            int bytes;
            byte[] buffer = new byte[4 * 1024];
            long bytesRemaining = fileSize;
            while (bytesRemaining > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) != -1) {
                fileOutputStream.write(buffer, 0, bytes);
                bytesRemaining -= bytes;
            }

            System.out.println("File received: " + fileName);

            String fileHash = calculateFileHash(fileName);
            System.out.println("File hash: " + fileHash);
        }
    }

    private void replicateFile(String fileName) {
        for (String serverAddress : otherServerAddresses) {
            try (Socket serverSocket = new Socket(serverAddress, 5000);
                 DataOutputStream serverOutputStream = new DataOutputStream(serverSocket.getOutputStream())) {

                File file = new File(fileName);
                if (file.exists() && file.isFile()) {
                    serverOutputStream.writeUTF("CHECK_EXISTENCE");
                    serverOutputStream.writeUTF(fileName);

                    DataInputStream serverInputStream = new DataInputStream(serverSocket.getInputStream());
                    String response = serverInputStream.readUTF();
                    if (response.equals("FILE_NOT_FOUND")) {
                        serverOutputStream.writeUTF("UPLOAD");
                        serverOutputStream.writeUTF(fileName);
                        serverOutputStream.writeLong(file.length());

                        try (FileInputStream fileInputStream = new FileInputStream(file)) {
                            byte[] buffer = new byte[4 * 1024];
                            int bytesRead;
                            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                serverOutputStream.write(buffer, 0, bytesRead);
                            }
                        }

                        System.out.println("File replicated: " + fileName);
                    } else {
                        System.out.println("File already exists on remote server: " + fileName);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String calculateFileHash(String fileName) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
                byte[] data = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(data)) != -1) {
                    digest.update(data, 0, bytesRead);
                }

                byte[] hashBytes = digest.digest();
                StringBuilder result = new StringBuilder();
                for (byte b : hashBytes) {
                    result.append(String.format("%02x", b));
                }
                return result.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calculating hash.";
        }
    }
}
