import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    private static final int PORT = 1234; // Porta que o servidor irá ouvir

    public void start() {
        ServerSocket serverSocket = null;

        try {
            // Cria o socket do servidor
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor iniciado. Aguardando conexões...");

            // Aguarda e aceita conexões dos clientes
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                // Inicie uma nova thread para lidar com a conexão do cliente
                Thread clientThread = new Thread(new Ponte(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Fecha o socket do servidor quando não for mais necessário
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
