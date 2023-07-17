import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

class Ponte extends Thread {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

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
                String request = dataInputStream.readUTF();

                if (request.equals("UPLOAD")) {
                    String fileName = dataInputStream.readUTF();
                    long fileSize = dataInputStream.readLong();

                    saveFile(fileName, fileSize);
                } else if (request.equals("LIST")) {
                    sendFileList();
                } else if (request.equals("DOWNLOAD")) {
                    String fileName = dataInputStream.readUTF();
                    sendFile(fileName);
                } else if (request.equals("EXIT")) {
                    running = false;
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFile(String fileName, long fileSize) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
    
        int bytes;
        byte[] buffer = new byte[4 * 1024];
        long bytesRemaining = fileSize;
        while (bytesRemaining > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            bytesRemaining -= bytes;
        }
    
        fileOutputStream.close();
        System.out.println("Arquivo recebido: " + fileName);
    

        // Calcular e exibir o hash do arquivo
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

    private void sendFileList() throws IOException {
        File directory = new File("C:/Users/edson/OneDrive/Documentos/vs-code/facul/sd/projetoS"); // Substitua pelo caminho real do diretório no servidor

        List<String> fileList = new ArrayList<>();
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        fileList.add(file.getName());
                    }
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
            System.out.println("Arquivo enviado: " + fileName);
        } else {
            dataOutputStream.writeUTF("FILE_NOT_FOUND");
        }
    }
}