public class ConsolidaDTO {
    String cnpj;
    String razaoSocial;
    String trimestre;
    String ano;
    double valorDespesas;

    public ConsolidaDTO(String cnpj, String razaoSocial, String trimestre, String ano, double valorDespesas) {
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.trimestre = trimestre;
        this.ano = ano;
        this.valorDespesas = valorDespesas;
    }

    public void adicionarValor(double valor) {
        this.valorDespesas += valor;
    }
}
