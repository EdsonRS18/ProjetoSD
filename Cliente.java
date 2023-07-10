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

            System.out.print("Digite o caminho do arquivo para upload: ");
            String filePath = scanner.nextLine();

            // Lê o arquivo e envia os dados ao servidor
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                socket.getOutputStream().write(buffer, 0, bytesRead);
            }

            // Finaliza o envio do arquivo
            fileInputStream.close();

            // Fecha o socket do cliente
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   
}
