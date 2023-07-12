import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Ponte implements Runnable {
    private Socket clientSocket;
    private String serverDirectory;

    public Ponte(Socket clientSocket, String serverDirectory) {
        this.clientSocket = clientSocket;
        this.serverDirectory = serverDirectory;
    }

    @Override
    public void run() {
        try {
            // Recebe o comando do cliente
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String command = reader.readLine();

            // Verifica o comando recebido
            if (command.equals("UPLOAD")) {
                // Recebe o arquivo enviado pelo cliente
                InputStream inputStream = clientSocket.getInputStream();

                // Define o diretório de destino para salvar o arquivo
                File directory = new File(serverDirectory);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Lê o nome do arquivo enviado pelo cliente
                String fileName = reader.readLine();

                // Cria o arquivo de destino
                File file = new File(directory, fileName);

                // Salva o arquivo enviado pelo cliente
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }

                fileOutputStream.close();
                inputStream.close();

                System.out.println("Arquivo recebido e salvo com sucesso: " + file.getAbsolutePath());
            } else if (command.equals("LIST")) {
                // Envia a resposta ao cliente para indicar que o comando foi recebido
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                writer.println("OK");

                // Envia a lista de arquivos para o cliente
                List<String> fileList = listarArquivos();

                if (!fileList.isEmpty()) {
                    for (String fileName : fileList) {
                        writer.println(fileName);
                    }
                } else {
                    writer.println("EMPTY");
                }
            }else if (command.equals("DOWNLOAD")) {
                // Recebe o nome do arquivo solicitado pelo cliente
                String fileName = reader.readLine();

                // Verifica se o arquivo existe no diretório do servidor
                File file = new File(serverDirectory + File.separator + fileName);
                if (file.exists() && file.isFile()) {
                    // Envia a resposta ao cliente indicando que o arquivo foi encontrado
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    writer.println("OK");

                    // Envia o conteúdo do arquivo para o cliente
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        clientSocket.getOutputStream().write(buffer, 0, bytesRead);
                    }

                    fileInputStream.close();
                } else {
                    // Envia a resposta ao cliente indicando que o arquivo não foi encontrado
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    writer.println("NOT_FOUND");
                }
            }

            // Fecha o socket do cliente
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> listarArquivos() {
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
