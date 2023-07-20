package Usuarios;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class Cliente {
    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;
//"C:/Users/edson/OneDrive/Documentos/a.txt
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
                            Funcao_Upload.uploadFile(socket, filePath, dataInputStream, dataOutputStream);
                        }
                        break;
                    case 2:
                        System.out.print("Digite o nome do arquivo para download: ");
                        String fileName = scanner.nextLine();
                        Funcao_Download.downloadFile(socket, fileName, dataInputStream, dataOutputStream);
                        break;
                    case 3:
                        Funcao_List.listFilesOnServer(dataInputStream, dataOutputStream);
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
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
