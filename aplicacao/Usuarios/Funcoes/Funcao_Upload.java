package Usuarios.Funcoes;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramSocket;


public class Funcao_Upload {
    public static void uploadFile(DatagramSocket multicastSocket, String filePath, DataInputStream dataInputStream,DataOutputStream dataOutputStream) throws IOException {
        
        int bytes;
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);

        dataOutputStream.writeUTF("UPLOAD");
        dataOutputStream.writeUTF(file.getName());
        dataOutputStream.writeLong(file.length());

        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            dataOutputStream.write(buffer, 0, bytes);
        }

        fileInputStream.close();
        System.out.println("Arquivo enviado com sucesso.");
    }
}
