import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {

    public static void main(String[] args) throws IOException {
        AnsService ansService = new AnsService();

        // Baixa últimos três trimestres
        String html = ansService.buscarHtml(AnsService.URL_BASE);
        if (html == null) {
            System.err.println("Não foi possível iniciar o processo.");
            return;
        }

        List<String> links = ansService.listarLinks(html);
        List<Integer> anos = ansService.listarAnos(links);
        List<ArquivoTrimestre> arquivosRecentes = ansService.listarUltimosTresTrimestres(anos);

        System.out.println("Iniciando Download...");
        ansService.baixarTrimestres(arquivosRecentes);
        System.out.println("Download concluído!");

        // Extração .zip
        for (ArquivoTrimestre arquivo : arquivosRecentes) {
            Path caminhoZip = Path.of("downloads", arquivo.getNome());
            Path destino = Path.of("downloads", "extraídos", arquivo.getNome().replace(".zip", ""));
            try {
                ZipService.extrairArquivo(caminhoZip, destino);
            } catch (IOException e) {
                System.err.println("Erro ao extrair: " + arquivo.getNome());
            }
        }

        // Mapeamento de operadoras
        ProcessadorService.carregarCadastroOperadoras(Path.of("recursos", "Relatorio_cadop.csv"));

        // Exportação dos dados
        System.out.println("Iniciando processamento dos dados...");
        Path pastaExtraidos = Path.of("downloads", "extraídos");

        for (ArquivoTrimestre arquivoTr : arquivosRecentes) {
            String nomePasta = arquivoTr.getNome().replace(".zip", "");
            Path arquivoCsv = pastaExtraidos.resolve(nomePasta).resolve(nomePasta + ".csv");

            if (Files.exists(arquivoCsv)) {
                ProcessadorService.processarAquivo(arquivoCsv);
            }
        }

        Path caminhoCsv = Path.of("downloads", "consolidado.csv");
        Path caminhoZipFinal = Path.of("consolidado_despesas.zip");

        ProcessadorService.salvarCsvConsolidado(caminhoCsv);
        compactarParaZip(caminhoCsv, caminhoZipFinal);
    }

    private static void compactarParaZip(Path origem, Path destino) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(destino))) {
            ZipEntry entry = new ZipEntry(origem.getFileName().toString());
            zos.putNextEntry(entry);
            Files.copy(origem, zos);
            zos.closeEntry();
            System.out.println("Arquivo ZIP gerado em: " + destino.toAbsolutePath());
        }
    }
}