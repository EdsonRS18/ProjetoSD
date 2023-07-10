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
        try {
            // Conecta ao servidor
            Socket socket = new Socket(serverIp, serverPort);
            System.out.println("Conectado ao servidor: " + serverIp);

            // Cria os fluxos de entrada e saída para comunicação com o servidor
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // Interage com o servidor
            Scanner scanner = new Scanner(System.in);
            boolean running = true;

            while (running) {
                System.out.println("\nMenu:");
                System.out.println("1. Upload de arquivo");
                System.out.println("2. Listar arquivos no servidor");
                System.out.println("0. Sair");

                System.out.print("Digite a opção desejada: ");
                int option = scanner.nextInt();
                scanner.nextLine(); // Limpar o buffer de entrada

                switch (option) {
                    case 1:
                        uploadFile(socket, scanner);
                        break;
                    case 2:
                        listFiles(socket);
                        break;
                    case 0:
                        running = false;
                        break;
                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                }
            }

            // Fecha o socket do cliente
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadFile(Socket socket, Scanner scanner) throws IOException {
        System.out.print("Digite o caminho do arquivo para upload: ");
        String filePath = scanner.nextLine();
    
        // Verifica se o arquivo existe
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Arquivo não encontrado.");
            return;
        }
    
        // Envia o comando de upload para o servidor
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        writer.println("UPLOAD");
    
        // Envia o nome do arquivo para o servidor
        writer.println(file.getName());
    
        // Envia o conteúdo do arquivo para o servidor
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            socket.getOutputStream().write(buffer, 0, bytesRead);
        }
        fileInputStream.close();
    
        System.out.println("Arquivo enviado com sucesso.");
    }
    

    private void listFiles(Socket socket) throws IOException {
    // Envia o comando de listagem de arquivos para o servidor
    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
    writer.println("LIST");

    // Recebe a lista de arquivos do servidor
    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    String line;

    // Verifica se o servidor retornou uma resposta válida
    if ((line = reader.readLine()) != null && line.equals("OK")) {
        // Recebe e exibe a lista de arquivos
        System.out.println("Arquivos no servidor:");
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    } else {
        System.out.println("Falha ao obter a lista de arquivos do servidor.");
    }
    }
}
