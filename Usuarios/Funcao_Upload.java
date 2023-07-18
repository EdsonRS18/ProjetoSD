package Usuarios;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;


public class Funcao_Upload {
    public static void uploadFile(Socket socket, String filePath, DataInputStream dataInputStream,DataOutputStream dataOutputStream) throws IOException {
        
        int bytes;
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);

        String targetDirectory = "C:/Users/edson/OneDrive/Documentos/vs-code/facul/sd/projetoD/repositorioS1/";
        String targetFilePath = targetDirectory + file.getName(); // Caminho completo do arquivo de destino

        dataOutputStream.writeUTF("UPLOAD");
        dataOutputStream.writeUTF(targetFilePath);
        dataOutputStream.writeLong(file.length());

        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            dataOutputStream.write(buffer, 0, bytes);
        }

        fileInputStream.close();
        System.out.println("Arquivo enviado com sucesso.");
    }
}
