import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;
//"C:/Users/edson/OneDrive/Documentos/vs-code/facul/sd/a.txt"
    public static void main(String[] args) {
        try (Socket socket = new Socket("192.168.0.13", 5000)) {
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
                        System.out.print("Digite o caminho do arquivo para upload: ");
                        String filePath = scanner.nextLine();
                        uploadFile(socket, filePath);
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
            System.out.println("File downloaded successfully.");
        } else if (response.equals("FILE_NOT_FOUND")) {
            System.out.println("File not found on the server.");
        } else {
            System.out.println("Error during file download.");
        }
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