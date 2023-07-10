import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Ponte implements Runnable {
    private Socket clientSocket;

    public Ponte(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            // Recebe o arquivo enviado pelo cliente
            InputStream inputStream = clientSocket.getInputStream();

            // Cria o arquivo no servidor para salvar o conteúdo recebido
            String fileName = "arquivo_recebido.txt";
            File file = new File(fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            fileOutputStream.close();

            // Fecha o socket do cliente
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
