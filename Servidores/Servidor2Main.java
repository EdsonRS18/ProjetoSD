package Servidores;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Servidor2Main {
    private static final int PORT = 8000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server 2 is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress());

                // Cria uma nova instância de ClientHandler para cada cliente conectado
                Ponte clientHandler = new Ponte(socket);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
  
}

