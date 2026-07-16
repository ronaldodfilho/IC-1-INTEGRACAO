import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        AnsService ansService = new AnsService();

        String html = ansService.buscarHtml(AnsService.URL_BASE);

        if (html == null) {
            System.err.println("Não foi possível iniciar o processo.");
            return;
        }

        List<String> links = ansService.listarLinks(html);
        List<Integer> anos = ansService.listarAnos(links);

        List<ArquivoTrimestre> arquivosRecentes =
                ansService.listarUltimosTresTrimestres(anos);

        System.out.println("Iniciando Download...");
        ansService.baixarTrimestres(arquivosRecentes);
        System.out.println("Download concluído!");

        for (ArquivoTrimestre arquivo : arquivosRecentes) {

            Path caminhoZip = Path.of("downloads", arquivo.getNome());

            String nomePastaDestino = arquivo.getNome().replace(".zip", "");
            Path destino = Path.of("downloads", "extraídos", nomePastaDestino);

            try {
                ZipService.extrairArquivo(caminhoZip, destino);
            }
            catch (IOException e) {
                System.err.println("Erro ao processar o arquivo " + arquivo.getNome());
            }


        }
    }
}