package middleware;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Scanner;

import Usuarios.Utilitarios;

public class Cliente {

    private static final int MULTICAST_PORT = 8888;
    private static final String MULTICAST_GROUP = "239.10.10.11";
    private static final int MULTICAST_RETRY_INTERVAL = 5000;

    public void run() {
        boolean connected = false;
        HashSet<String> availableServers = new HashSet<>();
        Utilitarios utilitarios = null; // Mova a criação da instância aqui
    
        while (!connected) {
            try {
                MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
                InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
                multicastSocket.joinGroup(group);
    
                byte[] serverInfoBuffer = new byte[256];
                DatagramPacket serverInfoPacket = new DatagramPacket(serverInfoBuffer, serverInfoBuffer.length);
    
                multicastSocket.receive(serverInfoPacket);
                String serverInfo = new String(serverInfoPacket.getData(), 0, serverInfoPacket.getLength());
              //  System.out.println("Received server info: " + serverInfo);
    
                // Split serverInfo into IP and port
                String[] infoParts = serverInfo.split(":");
                if (infoParts.length == 2) {
                    String serverIP = infoParts[0];
                    int serverPort = Integer.parseInt(infoParts[1]);
    
                    // Try connecting to the server
                    try {
                        utilitarios = new Utilitarios(serverIP, serverPort);
                        connected = true; // Defina como true quando a conexão for estabelecida
    
                        Scanner scanner = new Scanner(System.in);
    
                        while (connected) {
                            Utilitarios.exibirMenu();
                            int opcao = Integer.parseInt(scanner.nextLine());
                            Utilitarios.funcoes(opcao, scanner);
        
                            if (Utilitarios.isReconnectRequired()) {
                                connected = false; // Sair do loop e reconectar
                                Utilitarios.resetReconnectFlag(); // Resetar a flag de reconexão
                            }
                        }
    
                    } catch (IOException | InterruptedException e) {
                        System.out.println("Connection to the server failed. Searching for available servers...");
                        // Handle the connection error
                        try {
                            Thread.sleep(MULTICAST_RETRY_INTERVAL);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        } // Espera antes de tentar reconectar
                    }
                } else {
                    System.out.println("Invalid serverInfo format");
                }
    
                multicastSocket.close();
            } catch (IOException e) {
                // Failed to connect to the server, search for available servers
                System.out.println("Failed to connect to the server. Searching for available servers...");
                availableServers.clear();
                availableServers = searchForAvailableServers();
    
                if (availableServers.isEmpty()) {
                    // No servers available, wait before retrying
                    try {
                        Thread.sleep(MULTICAST_RETRY_INTERVAL);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    
    

    private HashSet<String> searchForAvailableServers() {
        HashSet<String> availableServers = new HashSet<>();

        try {
            MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            multicastSocket.joinGroup(group);

            byte[] serverInfoBuffer = new byte[256];
            DatagramPacket serverInfoPacket = new DatagramPacket(serverInfoBuffer, serverInfoBuffer.length);

            // Receive server announcements for a limited time
            long searchEndTime = System.currentTimeMillis() + MULTICAST_RETRY_INTERVAL;
            while (System.currentTimeMillis() < searchEndTime) {
                multicastSocket.receive(serverInfoPacket);
                String serverInfo = new String(serverInfoPacket.getData(), 0, serverInfoPacket.getLength());
                availableServers.add(serverInfo);
            }

            multicastSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return availableServers;
    }

    public static void main(String[] args) {
        Cliente cliente = new Cliente();
        cliente.run();
    }
}
