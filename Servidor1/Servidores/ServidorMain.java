package Servidores;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorMain {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress());

                // Create a new Bridge instance for each connected client
                Ponte clientHandler = new Ponte(socket);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
