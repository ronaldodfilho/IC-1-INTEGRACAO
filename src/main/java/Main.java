import java.io.IOException;
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

        ansService.baixarTrimestres(arquivosRecentes);
    }
}