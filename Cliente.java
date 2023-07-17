import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Scanner;

public class Cliente {
    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;
//"C:/Users/edson/OneDrive/Documentos/vs-code/facul/sd/a.txt"
    public static void main(String[] args) {
        try (Socket socket = new Socket("192.168.0.6", 5000)) {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            Scanner scanner = new Scanner(System.in);

            boolean running = true;
            while (running) {
                System.out.println("\nMenu:");
                System.out.println("1. Upload de arquivo");
                System.out.println("2. Download de arquivo");
                System.out.println("3. Listar arquivos no servidor");
                System.out.println("0. Sair");

                System.out.print("Digite a opção desejada: ");
                int option = Integer.parseInt(scanner.nextLine());

                switch (option) {
                    case 1:
                        System.out.print("Digite o caminho do arquivo para upload (ou 'exit' para sair): ");
                        String filePath = scanner.nextLine();
                        if (filePath.equals("exit")) {
                            dataOutputStream.writeUTF("EXIT");
                            running = false;
                        } else {
                            uploadFile(socket, filePath);
                        }
                        break;
                    case 2:
                        System.out.print("Digite o nome do arquivo para download: ");
                        String fileName = scanner.nextLine();
                        downloadFile(socket, fileName);
                        break;
                    case 3:
                        listFilesOnServer();
                        break;
                    case 0:
                        dataOutputStream.writeUTF("EXIT");
                        running = false;
                        break;
                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                }
            }

            dataInputStream.close();
            dataOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void uploadFile(Socket socket, String filePath) throws IOException {
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

    private static void downloadFile(Socket socket, String fileName) throws IOException {
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

    private static void listFilesOnServer() throws IOException {
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
