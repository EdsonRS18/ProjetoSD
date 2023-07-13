// Classe Cliente
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    private String serverIp;
    private int serverPort;

    public Cliente(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public void start() {
        try (
            // Conecta ao servidor
            Socket socket = new Socket(serverIp, serverPort);
            // Cria os fluxos de entrada e saída para comunicação com o servidor
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Conectado ao servidor: " + serverIp);
            
            // Interage com o servidor
            boolean running = true;

            while (running) {
                System.out.println("\nMenu:");
                System.out.println("1. Upload de arquivo");
                System.out.println("2. Listar arquivos no servidor");
                System.out.println("3. Download de arquivo");
                System.out.println("0. Sair");

                System.out.print("Digite a opção desejada: ");
                if (scanner.hasNextInt()) {
                    int option = scanner.nextInt();
                    scanner.nextLine(); // Consume the newline character

                    switch (option) {
                        case 1:
                            // Criou um arquivo novo .java
                            // nome_do_arquivo.nome_funçao(parametros)
                            FuncaoUploadFile.uploadFile(socket, scanner);
                            break;
                        case 2:
                            FuncaoListFiles.listFiles(socket, serverIp, serverPort);
                            break;
                        case 3:
                            FuncaoDownload.downloadFile(socket, reader, scanner);
                            break;
                        case 0:
                            running = false;
                            break;
                        default:
                            System.out.println("Opção inválida. Tente novamente.");
                    }
                } else {
                    System.out.println("Opção inválida. Tente novamente.");
                    scanner.nextLine(); // Consume the invalid input
                }
            }

            // Fecha o socket do cliente
            socket.close();
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
}