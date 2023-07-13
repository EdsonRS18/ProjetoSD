import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class FuncaoListFiles {
    public static void listFiles(Socket socket, String serverIp, int serverPort) throws IOException {
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
}
