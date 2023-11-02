package Servidores.funcoes;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListHandler {
    private DataOutputStream dataOutputStream;

    public ListHandler(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public void sendFileList() throws IOException {
        //envia ao cliente a lista de arquivos disponíveis no diretório do servidor.
        //destamos passando o user.dir que seria o diretorio do projeto
            String workingDir = System.getProperty("user.dir");
            File directory = new File(workingDir);
            File[] files = directory.listFiles();
        
            List<String> fileList = new ArrayList<>();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        fileList.add(file.getName());
                    }
                }
            }
        
            dataOutputStream.writeUTF("FILE_LIST");
            dataOutputStream.writeInt(fileList.size());
        
            for (String fileName : fileList) {
                dataOutputStream.writeUTF(fileName);
            }
        }
    
}
