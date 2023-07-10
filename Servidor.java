import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    private static final int PORT = 1234; // Porta que o servidor irá ouvir
    private String serverDirectory;
    private List<String> arquivos;

    public Servidor(String serverDirectory) {
        this.serverDirectory = serverDirectory;
        this.arquivos = new ArrayList<>();
    }

    public void start() {
        ServerSocket serverSocket = null;

        try {
            // Cria o socket do servidor
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor iniciado. Aguardando conexões...");

            // Aguarda e aceita conexões dos clientes
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                // Inicia uma nova thread para lidar com a conexão do cliente
                Thread clientThread = new Thread(new Ponte(clientSocket, serverDirectory, this));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Fecha o socket do servidor quando não for mais necessário
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void adicionarArquivo(String nomeArquivo) {
        arquivos.add(nomeArquivo);
    }

    public synchronized List<String> listarArquivos() {
        File directory = new File(serverDirectory);
        File[] files = directory.listFiles();
        List<String> fileList = new ArrayList<>();
    
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".txt")) {
                    fileList.add(file.getName());
                }
            }
        }
    
        return fileList;
    }
    
    

    
}
