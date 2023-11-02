package Usuarios.Funcoes;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.security.MessageDigest;

public class Funcao_Download {
    public static void downloadFile(DatagramSocket multicastSocket, String fileName, DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF("DOWNLOAD");
        dataOutputStream.writeUTF(fileName);

        String response = dataInputStream.readUTF();
        if (response.equals("FILE_FOUND")) {
            long fileSize = dataInputStream.readLong();

            FileOutputStream fileOutputStream = new FileOutputStream(fileName);

            int bytes;
            byte[] buffer = new byte[4 * 1024];
            while (fileSize > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                fileOutputStream.write(buffer, 0, bytes);
                fileSize -= bytes;
            }

            fileOutputStream.close();
            System.out.println("Arquivo baixado com sucesso.");

            // Solicitar ao servidor que envie seu hash
            dataOutputStream.writeUTF("SEND_HASH");
            String serverHash = dataInputStream.readUTF();

            // Calcular e exibir o hash do arquivo baixado
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                FileInputStream fileInputStream = new FileInputStream(fileName);

                byte[] data = new byte[1024];
                while ((bytes = fileInputStream.read(data)) != -1) {
                    digest.update(data, 0, bytes);
                }

                byte[] hashBytes = digest.digest();
                String hash = bytesToHex(hashBytes);
                System.out.println("Hash do arquivo: " + hash);

                fileInputStream.close();

                // Comparar o hash do arquivo com o hash do servidor
                if (hash.equals(serverHash)) {
                    System.out.println("Hash verificado com sucesso. O arquivo é íntegro.");
                } else {
                    System.out.println("Erro: O hash não corresponde. O arquivo pode estar corrompido.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (response.equals("FILE_NOT_FOUND")) {
            System.out.println("Arquivo não encontrado no servidor.");
        } else {
            System.out.println("Erro durante o download do arquivo.");
        }
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }
}
