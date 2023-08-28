package Servidores;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorMain {
    private static final int PORT = 8000;

    public static void main(String[] args) {
        // Anunciar a presença do servidor usando multicast
        announceServerPresence();

        // Aguardar um curto período para que a descoberta de servidores seja concluída
        waitForServerDiscovery();

        // Iniciar a execução do servidor
        startServer();
    }

    private static void announceServerPresence() {
        ServerDiscovery.announceServerPresence();
    }

    private static void waitForServerDiscovery() {
        try {
            Thread.sleep(5000); // Aguardar 5 segundos
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void startServer() {
        ServerDiscovery serverDiscovery = new ServerDiscovery();
        serverDiscovery.start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor aguardando conexão do cliente na porta " + PORT);

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("Cliente conectado");

                    // Crie uma nova instância do ClienteHandler para cada cliente conectado
                    ClienteHandler clientHandler = new ClienteHandler(socket, serverDiscovery.getOtherServerAddresses());
                    clientHandler.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Erro ao aceitar conexão com o cliente.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao iniciar o servidor na porta " + PORT);
        }
    }
}
