import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AnsService {

    public static final String URL_BASE =
            "https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/";

    private final HttpClient client;

    public AnsService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

    }

    public String buscarHtml(String url) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                System.out.println("Não foi possível obter o HTML, ocorreu um erro: " + response.statusCode());
            }
        } catch (IOException e) {
            System.err.println("Erro de conexão: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("A requisição foi interrompida: " + e.getMessage());
        }

        return null;
    }

    public List<String> listarLinks(String html) {

        Document document = Jsoup.parse(html);
        Elements links = document.select("a[href]");

        List<String> lista = new ArrayList<>();

        for (Element link : links) {
            lista.add(link.attr("href"));
        }

        return lista;
    }

    public List<Integer> listarAnos(List<String> links) {

        List<Integer> lista = new ArrayList<>();

        for (String link : links) {
            if (link.endsWith("/") && link.length() == 5) {
                lista.add(Integer.parseInt(link.split("/")[0]));
            }
        }

        return lista;
    }

    public List<ArquivoTrimestre> listarAquivosPorAno(int ano) {

        List<ArquivoTrimestre> arquivos = new ArrayList<>();

        String url_ano = URL_BASE + ano + "/";

        String html = buscarHtml(url_ano);

        int trimestre = 0;

        if (html == null) {
            return arquivos;
        }

        Document document = Jsoup.parse(html);
        Elements linksZip = document.select("a[href$=.zip]");

        ArquivoTrimestre arquivoTrimestre;

        for (Element link : linksZip) {

            String nomeArquivo = link.attr("href");

            if (nomeArquivo.length() >= 2
                    && Character.isDigit(nomeArquivo.charAt(0))
                    && Character.toUpperCase(nomeArquivo.charAt(1)) == 'T') {

                trimestre = Character.getNumericValue(nomeArquivo.charAt(0));

                if (trimestre >= 1 && trimestre <= 4) {
                    String urlArquivo = url_ano + nomeArquivo;

                    arquivoTrimestre = new ArquivoTrimestre(ano, trimestre, nomeArquivo, urlArquivo);
                    arquivos.add(arquivoTrimestre);
                }
            }
        }

        return arquivos;
    }

    public List<ArquivoTrimestre> listarUltimosTresTrimestres(List<Integer> anos) {

        List<ArquivoTrimestre> arquivosEncontrados = new ArrayList<>();

        for (int i = anos.size() - 1; i >= 0; i--) {

            int ano = anos.get(i);

            List<ArquivoTrimestre> arquivosDoAno = listarAquivosPorAno(ano);

            arquivosEncontrados.addAll(arquivosDoAno);
        }

        // Inverte a ordem dos anos: Mais recente -> Mais antigo
        arquivosEncontrados.sort(Comparator.comparingInt(ArquivoTrimestre::getAno)
                .thenComparingInt(ArquivoTrimestre::getTrimestre)
                .reversed());

        List<ArquivoTrimestre> resultado = new ArrayList<>();

        String ultimoTrimestre = "";
        int quantidadeDeTrimestres = 0;

        for (ArquivoTrimestre arquivo : arquivosEncontrados) {

            String trimestreAtual = arquivo.getAno() + " - " + arquivo.getTrimestre();

            if (!trimestreAtual.equals(ultimoTrimestre)) {

                quantidadeDeTrimestres++;

                if (quantidadeDeTrimestres > 3) {
                    break;
                }

                ultimoTrimestre = trimestreAtual;
            }

            resultado.add(arquivo);
        }

        return resultado;
    }

    public void baixarTrimestres(List<ArquivoTrimestre> arquivos) throws IOException {

        for(ArquivoTrimestre arquivo : arquivos) {

            HttpRequest downloadRequest = criarRequisicao(arquivo.getUrl(),45);


                Files.createDirectories(Path.of("downloads"));
                Path destino = Path.of("downloads", arquivo.getNome());

            try {
                HttpResponse<Path> response = client.send(downloadRequest, HttpResponse.BodyHandlers.ofFile(destino));

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    System.out.println("Arquivo salvo em: " + response.body());
                } else {
                    System.out.println("Ocorreu um erro: " + response.statusCode());
                }
            } catch (IOException e) {
                System.err.println("Erro de conexão: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("A requisição foi interrompida: " + e.getMessage());
            }
        }
    }
    private HttpRequest criarRequisicao(String url, int time){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(time))
                .GET()
                .build();

        return request;
    }
}