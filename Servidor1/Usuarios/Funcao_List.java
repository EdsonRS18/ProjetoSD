package Usuarios;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Funcao_List {
    public static void listFilesOnServer(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF("LIST");

        String response = dataInputStream.readUTF();
        if (response.equals("FILE_LIST")) {
            System.out.println("Lista de arquivos no servidor:");

            int numFiles = dataInputStream.readInt();
            for (int i = 0; i < numFiles; i++) {
                String fileName = dataInputStream.readUTF();
                System.out.println(fileName);
            }
        } else {
            System.out.println("Erro ao obter a lista de arquivos do servidor.");
        }
    }
}
