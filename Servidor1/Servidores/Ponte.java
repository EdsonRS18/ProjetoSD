package Servidores;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

class Ponte extends Thread {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String server2Address = "192.168.0.102";
    private int server2Port = 8000;
   

    public Ponte(Socket socket) {
        this.socket = socket;
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            boolean running = true;
            while (running) {
                try {
                    String request = dataInputStream.readUTF();
    
                    if (request.equals("UPLOAD")) {
                        String fileName = dataInputStream.readUTF();
                        long fileSize = dataInputStream.readLong();
    
                        saveFile(fileName, fileSize);
                        replicateFile(fileName);
    
                    } else if (request.equals("LIST")) {
                        sendFileList();
                    } else if (request.equals("DOWNLOAD")) {
                        String fileName = dataInputStream.readUTF();
                        sendFile(fileName);
                    } else if (request.equals("EXIT")) {
                        running = false;
                    }
                } catch (EOFException e) {
                    // Client disconnected unexpectedly
                    System.err.println("Client disconnected unexpectedly.");
                    running = false;
                }
            }
    
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFile(String fileName, long fileSize) throws IOException {
        File file = new File(fileName);
        if (file.exists()) {
            System.out.println("File " + fileName + " already exists on Server1. Skipping upload and replication.");
            return;
        }
    
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
    
        int bytes;
        byte[] buffer = new byte[4 * 1024];
        long bytesRemaining = fileSize;
        while (bytesRemaining > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            bytesRemaining -= bytes;
        }
    
        fileOutputStream.close();
        System.out.println("File received: " + fileName);
    
        // Calculate and display the hash of the file
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fileInputStream = new FileInputStream(fileName);
    
            byte[] data = new byte[1024];
            while ((bytes = fileInputStream.read(data)) != -1) {
                digest.update(data, 0, bytes);
            }
    
            byte[] hashBytes = digest.digest();
            String hash = bytesToHex(hashBytes);
            System.out.println("File hash: " + hash);
    
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

    private void sendFileList() throws IOException {
        String workingDir = System.getProperty("user.dir");
        File directory = new File(workingDir);
        File[] files = directory.listFiles();
    
        List<String> fileList = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileList.add(file.getName());
                }
            }
        }
    
        dataOutputStream.writeUTF("FILE_LIST");
        dataOutputStream.writeInt(fileList.size());
    
        for (String fileName : fileList) {
            dataOutputStream.writeUTF(fileName);
        }
    }
   
    private void sendFile(String fileName) throws IOException {
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
            System.out.println("File sent: " + fileName);
        } else {
            dataOutputStream.writeUTF("FILE_NOT_FOUND");
        }
    }

    private void replicateFile(String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                System.out.println("File " + fileName + " does not exist on Server1. Skipping replication to Server2.");
                return;
            }
    
            Socket server2Socket = new Socket(server2Address, server2Port);
            DataOutputStream server2OutputStream = new DataOutputStream(server2Socket.getOutputStream());
            DataInputStream server2InputStream = new DataInputStream(server2Socket.getInputStream());
    
            server2OutputStream.writeUTF("CHECK_EXISTENCE");
            server2OutputStream.writeUTF(fileName);
    
            String response = server2InputStream.readUTF();
    
            if (response.equals("FILE_NOT_FOUND")) {
                server2OutputStream.writeUTF("UPLOAD");
                server2OutputStream.writeUTF(fileName);
                server2OutputStream.writeLong(file.length());
    
                FileInputStream fileInputStream = new FileInputStream(file);
                int bytes;
                byte[] buffer = new byte[4 * 1024];
                while ((bytes = fileInputStream.read(buffer)) != -1) {
                    server2OutputStream.write(buffer, 0, bytes);
                }
                fileInputStream.close();
                System.out.println("File replicated to Server2: " + fileName);
            } else {
                System.out.println("File " + fileName + " already exists on Server2. Skipping replication.");
            }
    
            server2Socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


   
}
