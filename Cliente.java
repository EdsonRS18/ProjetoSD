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
                            uploadFile(socket, scanner);
                            break;
                        case 2:
                            listFiles(socket);
                            break;
                        case 3:
                            downloadFile(socket,reader, scanner);
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

    private void uploadFile(Socket socket, Scanner scanner) throws IOException {
        System.out.print("Digite o caminho do arquivo para upload: ");
        String filePath = scanner.nextLine();
    
        // Verifica se o arquivo existe
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            System.out.println("Arquivo inválido ou não encontrado.");
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
        try {
            // Cria uma nova conexão com o servidor
            Socket newSocket = new Socket(serverIp, serverPort);
            System.out.println("Conectado ao servidor: " + serverIp);
    
            // Envia o comando de listagem de arquivos para o servidor
            PrintWriter writer = new PrintWriter(newSocket.getOutputStream(), true);
            writer.println("LIST");
    
            // Recebe a resposta do servidor
            BufferedReader reader = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));
            String line;
    
            // Limpa o buffer de entrada
            while (reader.ready()) {
                reader.readLine();
            }
    
            // Verifica a resposta do servidor
            if ((line = reader.readLine()) != null) {
                if (line.equals("OK")) {
                    // Recebe e exibe a lista de arquivos
                    System.out.println("Arquivos no servidor:");
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } else if (line.equals("EMPTY")) {
                    System.out.println("Não há arquivos no servidor.");
                } else {
                    System.out.println("Resposta inválida do servidor.");
                }
            } else {
                System.out.println("Falha ao obter a resposta do servidor.");
            }
    
            // Fecha o BufferedReader e o socket do cliente
            reader.close();
            newSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(Socket socket, BufferedReader reader, Scanner scanner) throws IOException {
        System.out.print("Digite o nome do arquivo para download: ");
        String fileName = scanner.nextLine();
    
        // Envia o comando de download para o servidor
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        writer.println("DOWNLOAD");
    
        // Envia o nome do arquivo para o servidor
        writer.println(fileName);
    
        // Recebe a resposta do servidor
        String response = reader.readLine();
    
        if (response != null && response.equals("OK")) { // Verifica se a resposta não é nula antes de chamar o método equals
            // Cria o arquivo de destino para salvar o arquivo recebido
            File file = new File(fileName);
    
            // Cria o fluxo de saída para salvar o arquivo
            FileOutputStream fileOutputStream = new FileOutputStream(file);
    
            // Recebe o conteúdo do arquivo do servidor e salva no arquivo de destino
            byte[] buffer = new byte[4096];
            int bytesRead;
    
            while ((bytesRead = socket.getInputStream().read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
    
            fileOutputStream.close();
    
            System.out.println("Arquivo recebido e salvo com sucesso: " + file.getAbsolutePath());
        } else {
            System.out.println("Arquivo não encontrado no servidor.");
        }
    }
}