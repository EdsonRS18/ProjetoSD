package Servidores;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DownloadHandler {
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;

    public DownloadHandler(DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
    }

    public void handleDownload() {
        try {
            String fileName = dataInputStream.readUTF();
            sendFile(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFile(String fileName) throws IOException {
        File file = new File(fileName);

        if (file.exists() && file.isFile()) {
            dataOutputStream.writeUTF("FILE_FOUND");
            dataOutputStream.writeLong(file.length());

            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[4 * 1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytesRead);
            }
            fileInputStream.close();
            System.out.println("File sent: " + fileName);
        } else {
            dataOutputStream.writeUTF("FILE_NOT_FOUND");
        }
    }
}
