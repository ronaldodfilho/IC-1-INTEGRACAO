import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipService {

    public ZipService() {
        //something
    }

    public static void extrairArquivo(Path arquivoZip, Path pastaDestino) throws IOException {

        Files.createDirectories(pastaDestino);

        try (ZipInputStream zipInput = new ZipInputStream(new FileInputStream(arquivoZip.toFile()))) {
            ZipEntry entrada;

            while ((entrada = zipInput.getNextEntry()) != null){

                Path caminhoArquivoFinal = pastaDestino.resolve(entrada.getName());

                if (entrada.isDirectory()) {
                    Files.createDirectories(caminhoArquivoFinal);
                }
                else {
                    Files.createDirectories(caminhoArquivoFinal.getParent());
                    Files.copy(zipInput, caminhoArquivoFinal, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }

                zipInput.closeEntry();
            }
        }
    }
}