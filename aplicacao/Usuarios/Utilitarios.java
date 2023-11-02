package Usuarios;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Scanner;

import Usuarios.Funcoes.Funcao_Download;
import Usuarios.Funcoes.Funcao_List;
import Usuarios.Funcoes.Funcao_Upload;

public class Utilitarios {
    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;
    private static DatagramSocket multicastSocket = null;
    private static boolean requireReconnect = false; // Variável para sinalizar a necessidade de reconexão

    public Utilitarios(String server, int serverPort) throws IOException {
        Socket socket = new Socket(server, serverPort);
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataInputStream = new DataInputStream(socket.getInputStream());
        multicastSocket = new DatagramSocket(); 
    }

    public static void run() {
        try {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                exibirMenu();
                int opcao = Integer.parseInt(scanner.nextLine());
                funcoes(opcao, scanner); // Chama o método funcoes usando a instância
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void exibirMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. Upload de arquivo");
        System.out.println("2. Download de arquivo");
        System.out.println("3. Listar arquivos do servidor");
        System.out.println("0. Sair");
        System.out.print("Entre com sua escolha: ");
    }

    public static void funcoes(int opcao, Scanner scanner)
        throws IOException, InterruptedException {
        switch (opcao) {
            //upload
            case 1:
            try{
                System.out.print("Digite o caminho do arquivo que deseja fazer o upload ('exit' para sair): ");
                String filePath = scanner.nextLine();
                Funcao_Upload.uploadFile(multicastSocket, filePath, dataInputStream, dataOutputStream);
            }catch (Exception e) {
                    System.out.println("Não foi possível fazer o upload. Problema no Servidor, Comencando reconexao.");
                    requireReconnect = true; // Sinaliza a necessidade de reconexão
                }
                break;
            //download
            case 2:
            try{
                System.out.print("Digite o nome do arquivo que deseja fazer o download: ");
                String fileName = scanner.nextLine();
                Funcao_Download.downloadFile(multicastSocket, fileName, dataInputStream, dataOutputStream);
                break;
            }catch (Exception e) {
                    System.out.println("Não foi possível o download. Problema no Servidor, Comencando reconexao.");
                    requireReconnect = true; // Sinaliza a necessidade de reconexão
            }
            case 3:
                //listar
                try {
                    Funcao_List.listFilesOnServer(dataInputStream, dataOutputStream);
                } catch (Exception e) {
                    System.out.println("Não foi possível listar. Problema no Servidor, Comencando reconexao.");
                    requireReconnect = true; // Sinaliza a necessidade de reconexão
                }
                break;
            case 0:
                dataOutputStream.writeUTF("EXIT"); // Envia o comando de saída para o servidor
                System.out.println("Saindo do programa.");
                System.exit(0); // Encerra o programa
                break;
            default:
                System.out.println("Opção inválida, tente novamente.");
        }
    }

    public static boolean isReconnectRequired() {
        return requireReconnect;
    }

    public static void resetReconnectFlag() {
        requireReconnect = false;
    }
}
