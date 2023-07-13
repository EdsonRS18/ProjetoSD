import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class FuncaoUploadFile {
    
    public static void uploadFile(Socket socket, Scanner scanner) throws IOException {
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
    }


