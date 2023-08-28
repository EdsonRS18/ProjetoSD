package Servidores;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

public class ServerDiscovery extends Thread {
    private static final String MULTICAST_ADDRESS = "224.0.0.1";
    private static final int MULTICAST_PORT = 8888;
    private List<String> otherServerAddresses;
    private String localServerIP;

    public ServerDiscovery() {
        otherServerAddresses = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            System.out.println("Thread do ServerDiscovery iniciada.");
            // Obter o endereço IP do servidor local
            localServerIP = InetAddress.getLocalHost().getHostAddress();

            InetAddress multicastGroup = InetAddress.getByName(MULTICAST_ADDRESS);
            MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
            multicastSocket.joinGroup(multicastGroup);

            System.out.println("ServerDiscovery: Iniciando anúncio de presença do servidor.");

            announceServerPresence();

            System.out.println("ServerDiscovery: Anúncio de presença do servidor enviado com sucesso.");

            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                String receivedData = new String(packet.getData()).trim();
                String serverIP = receivedData;

                // Verificar se o endereço IP recebido é diferente do servidor local
                if (!packet.getAddress().getHostAddress().equals(localServerIP)) {
                    // Adicionar o endereço IP à lista de outros servidores descobertos
                    if (!otherServerAddresses.contains(serverIP)) {
                        otherServerAddresses.add(serverIP);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getOtherServerAddresses() {
        return otherServerAddresses;
    }

    public String getLocalServerIP() {
        return localServerIP;
    }

    // Método estático para anunciar a presença do servidor através do multicast
    public static void announceServerPresence() {
        try {
            String serverIP = InetAddress.getLocalHost().getHostAddress();
            String announcementData = serverIP;

            InetAddress multicastGroup = InetAddress.getByName(MULTICAST_ADDRESS);
            MulticastSocket multicastSocket = new MulticastSocket();

            byte[] buffer = announcementData.getBytes();
            DatagramPacket announcementPacket = new DatagramPacket(buffer, buffer.length, multicastGroup, MULTICAST_PORT);
            multicastSocket.send(announcementPacket);

            multicastSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
