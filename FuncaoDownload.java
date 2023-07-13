import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class FuncaoDownload {
     static void downloadFile(Socket socket, BufferedReader reader, Scanner scanner) throws IOException {
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
