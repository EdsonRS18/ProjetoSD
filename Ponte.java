import java.io.*;
import java.net.Socket;
import java.util.List;

public class Ponte implements Runnable {
    private Socket clientSocket;
    private String serverDirectory;
    private Servidor servidor;

    public Ponte(Socket clientSocket, String serverDirectory, Servidor servidor) {
        this.clientSocket = clientSocket;
        this.serverDirectory = serverDirectory;
        this.servidor = servidor;
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

                // Adiciona o nome do arquivo à lista de arquivos do servidor
                servidor.adicionarArquivo(fileName);
            } else if (command.equals("LIST")) {
                // Envia a resposta ao cliente para indicar que o comando foi recebido
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                writer.println("OK");

                // Envia a lista de arquivos para o cliente
                List<String> fileList = servidor.listarArquivos();

                if (!fileList.isEmpty()) {
                    for (String fileName : fileList) {
                        writer.println(fileName);
                    }
                } else {
                    writer.println("EMPTY");
                }
            }

            // Fecha o socket do cliente
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
