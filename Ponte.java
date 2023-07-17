import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Ponte extends Thread {
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
        while ((bytes = socket.getInputStream().read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
        }

        fileOutputStream.close();
        System.out.println("Arquivo recebido: " + fileName);
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
