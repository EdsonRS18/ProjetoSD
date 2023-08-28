package Usuarios;

import java.io.*;
import java.net.*;
import java.util.*;

public class Cliente {
    private static final String MULTICAST_ADDRESS = "224.0.0.1";
    private static final int MULTICAST_PORT = 8888;

    public static void main(String[] args) {
        try {
            System.out.println("Cliente iniciado.");

            List<String> discoveredServers = discoverServers();

            if (discoveredServers.isEmpty()) {
                System.out.println("Nenhum servidor encontrado. Encerrando o cliente.");
                return;
            }

            System.out.println("Servidores descobertos:");
            for (int i = 0; i < discoveredServers.size(); i++) {
                System.out.println((i + 1) + ". " + discoveredServers.get(i));
            }

            Random random = new Random();
            int selectedServerIndex = random.nextInt(discoveredServers.size());
            String serverIP = discoveredServers.get(selectedServerIndex);

            connectToServer(serverIP);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> discoverServers() {
        List<String> discoveredServers = new ArrayList<>();

        try {
            System.out.println("Descobrindo servidores usando multicast...");

            InetAddress multicastGroup = InetAddress.getByName(MULTICAST_ADDRESS);
            MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
            multicastSocket.joinGroup(multicastGroup);

            byte[] buffer = new byte[256];

            long startTime = System.currentTimeMillis();
            long discoveryTime = 1000;
            while (System.currentTimeMillis() - startTime < discoveryTime) {
                DatagramPacket announcementPacket = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(announcementPacket);

                String announcementData = new String(buffer, 0, announcementPacket.getLength());
                String[] servers = announcementData.split(",");
                for (String server : servers) {
                    if (!discoveredServers.contains(server)) {
                        discoveredServers.add(server);
                    }
                }
            }

            multicastSocket.leaveGroup(multicastGroup);
            multicastSocket.close();

            if (discoveredServers.isEmpty()) {
                System.out.println("Nenhum servidor encontrado.");
            } else {
                System.out.println("Servidores descobertos:");
                for (int i = 0; i < discoveredServers.size(); i++) {
                    System.out.println((i + 1) + ". " + discoveredServers.get(i));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao descobrir servidores.");
        }

        return discoveredServers;
    }

    private static void connectToServer(String serverIP) {
        System.out.println("Conectando ao servidor " + serverIP + "...");

        while (true) {
            try (Socket socket = new Socket(serverIP, 8000)) {
                System.out.println("Conexão bem-sucedida ao servidor.");

                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                boolean running = true;
                Scanner scanner = new Scanner(System.in);

                while (running) {
                    System.out.println("\nMenu:");
                    System.out.println("1. Upload do arquivo");
                    System.out.println("2. Download do arquivo");
                    System.out.println("3. Listar arquivos no servidor");
                    System.out.println("0. Sair");

                    System.out.print("Digite sua escolha: ");
                    int option;
                    try {
                        option = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Entrada inválida.");
                        continue;
                    }

                    switch (option) {
                        case 1:
                            System.out.print("Digite o caminho do arquivo para upload ('exit' para sair): ");
                            String filePath = scanner.nextLine();
                            if (filePath.equalsIgnoreCase("exit")) {
                                dataOutputStream.writeUTF("EXIT");
                                running = false;
                            } else {
                                Funcao_Upload.uploadFile(socket, filePath, dataInputStream, dataOutputStream);
                            }
                            break;
                        case 2:
                            System.out.print("Digite o nome do arquivo para download: ");
                            String fileName = scanner.nextLine();
                            FuncaoDownload.downloadFile(socket, fileName, dataInputStream, dataOutputStream);
                            break;
                        case 3:
                            FuncaoList.listFilesOnServer(dataInputStream, dataOutputStream);
                            break;
                            case 0:
                            dataOutputStream.writeUTF("EXIT");
                            running = false;
                            break;
                        default:
                            System.out.println("Opção inválida, tente novamente.");
                    }
                }

                System.out.println("Encerrando a conexão.");
                break; // Sai do loop principal ao encerrar a conexão

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Erro na conexão com o servidor. Tentando reconectar...");
                try {
                    Thread.sleep(3000); // Tentar reconectar após 3 segundos
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
