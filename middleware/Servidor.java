package middleware;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;


public class Servidor {
    private static final int SERVER_PORT = 8000; // Porta do servidor
    private static final int MULTICAST_INTERVAL = 5000; // Intervalo entre anúncios (em milissegundos)

    public void run() throws SocketException {
        int multicastPort = 8888; // Porta multicast
        String multicastGroup = "239.10.10.11"; // Endereço IP do grupo multicast

        HashSet<String> serverAddresses = new HashSet<>(); // Armazena os endereços IP dos servidores

        new Thread(() -> receiveMulticastAnnouncements(serverAddresses, multicastPort, multicastGroup)).start();

        while (true) {
            // Descobrir o endereço IP local do servidor
            String serverIP = getLocalIPAddress();

            // Enviar informações do servidor pelo multicast
            try {
                MulticastSocket multicastSocket = new MulticastSocket();
                InetAddress group = InetAddress.getByName(multicastGroup);
                String serverInfo = serverIP + ":" + SERVER_PORT; // Incluir o IP local do servidor
                byte[] serverInfoBuffer = serverInfo.getBytes();
                DatagramPacket serverInfoPacket = new DatagramPacket(serverInfoBuffer, serverInfoBuffer.length, group, multicastPort);
                multicastSocket.send(serverInfoPacket);
                serverAddresses.add(serverInfo);

                multicastSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Iniciar o servidor para aceitar conexões
            try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
                System.out.println("Servidor ouvindo na porta " + SERVER_PORT);

                // Defina um timeout para o accept para que ele possa verificar a presença de clientes em intervalos
                serverSocket.setSoTimeout(MULTICAST_INTERVAL);

                Socket socket;
                try {
                    socket = serverSocket.accept();
                    System.out.println("Cliente conectado");

                    // Crie uma nova instância do Ponte(ClientHandler) para cada cliente conectado
                    Ponte clientHandler = new Ponte(socket, serverAddresses);
                    clientHandler.start();
                } catch (java.net.SocketTimeoutException e) {
                    System.out.println("Nenhum cliente se conectou no intervalo, fazendo novo anúncio.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static String getLocalIPAddress() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                    return addr.getHostAddress();
                }
            }
        }
        return null;
    }

    private static void receiveMulticastAnnouncements(HashSet<String> serverAddresses, int port, String group) {
        try {
            MulticastSocket multicastSocket = new MulticastSocket(port);
            InetAddress multicastGroup = InetAddress.getByName(group);
            multicastSocket.joinGroup(multicastGroup);

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                multicastSocket.receive(packet);
                String receivedInfo = new String(packet.getData(), 0, packet.getLength());
                
               // System.out.println("Received announcement: " + receivedInfo); //informando o ip de onde ta sendo o anuncio

                // Armazenar o endereço IP do servidor anunciante
                serverAddresses.add(receivedInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
}
