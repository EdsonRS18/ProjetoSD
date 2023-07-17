import java.io.*;
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

                // Cria uma nova instância de ClientHandler para cada cliente conectado
                Ponte clientHandler = new Ponte(socket);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
  
}
