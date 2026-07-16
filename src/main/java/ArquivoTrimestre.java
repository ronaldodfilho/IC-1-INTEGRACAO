public class ArquivoTrimestre {

    private int ano;
    private int trimestre;
    private String nome;
    private String url;

    public ArquivoTrimestre(int ano, int trimestre, String nome, String url) {
        this.ano = ano;
        this.trimestre = trimestre;
        this.nome = nome;
        this.url = url;
    }

    public int getAno() {
        return ano;
    }

    public int getTrimestre() {
        return trimestre;
    }

    public String getNome() {
        return nome;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "ArquivoTrimestre{" +
                "\nano=" + ano +
                ",\ntrimestre=" + trimestre +
                ",\n='" + nome + '\'' +
                ",\nurl='" + url + '\'' +
                '}';
    }
}
