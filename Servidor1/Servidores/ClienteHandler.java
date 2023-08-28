package Servidores;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ClienteHandler extends Thread {
    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final List<String> otherServerAddresses;

    public ClienteHandler(Socket socket, List<String> otherServerAddresses) throws IOException {
        this.socket = socket;
        this.otherServerAddresses = otherServerAddresses;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            System.out.println("Thread do ClienteHandler iniciada.");

            boolean running = true;
            while (running) {
                try {
                    String request = dataInputStream.readUTF();

                    switch (request) {
                        case "UPLOAD":
                            handleUpload();
                            break;
                        case "LIST":
                            handleListFiles();
                            break;
                        case "DOWNLOAD":
                            handleDownload();
                            break;
                        case "EXIT":
                            running = false;
                            break;
                        case "CHECK_EXISTENCE":
                            handleCheckExistence();
                            break;
                        default:
                            System.out.println("Unknown request: " + request);
                            break;
                    }

                } catch (EOFException e) {
                    System.err.println("Client disconnected unexpectedly.");
                    running = false;
                }
            }

            socket.close();
            System.out.println("Thread do ClienteHandler encerrada.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleUpload() {
        UploadHandler uploadHandler = new UploadHandler(dataInputStream, dataOutputStream, otherServerAddresses);
        uploadHandler.handleUpload();
    }

    private void handleListFiles() {
        ListHandler listHandler = new ListHandler(dataOutputStream);
        listHandler.handleListFiles();
    }

    private void handleDownload() {
        DownloadHandler downloadHandler = new DownloadHandler(dataInputStream, dataOutputStream);
        downloadHandler.handleDownload();
    }

    private void handleCheckExistence() {
        try {
            String fileName = dataInputStream.readUTF();
            ExistenceChecker existenceChecker = new ExistenceChecker(dataOutputStream);
            existenceChecker.checkFileExistence(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
