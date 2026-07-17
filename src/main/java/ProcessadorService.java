import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


public class ProcessadorService {

    private static final Map<String, OperadoraCadastro> cadastroOperadoras = new HashMap<>();

    private static final Map<String, ConsolidaDTO> consolidadoMap = new HashMap<>();

    public static void carregarCadastroOperadoras(Path caminhoCadop) {

        System.out.println("Carregando cadastro operadoras...");

        try (BufferedReader reader = new BufferedReader(new FileReader(caminhoCadop.toFile()))) {

            String cabecalho = reader.readLine();
            if (cabecalho == null) return;

            cabecalho = cabecalho.replace("\"", "");
            String separador = cabecalho.contains(";") ? ";" : ",";
            String[] colunas = cabecalho.split(separador);

            int indiceRegAns = -1;
            int indiceCnpj = -1;
            int indiceRazaoSocial = -1;

            for (int i = 0; i < colunas.length; i++) {

                String coluna = colunas[i].trim();

                if (coluna.equalsIgnoreCase("REGISTRO_OPERADORA")) {
                    indiceRegAns = i;
                }
                else if (coluna.equalsIgnoreCase("CNPJ")) {
                    indiceCnpj = i;
                }
                else if (coluna.equalsIgnoreCase("Razao_Social")) {
                    indiceRazaoSocial = i;
                }
            }

            if (indiceRegAns == -1 || indiceCnpj == -1 || indiceRazaoSocial == -1) {
                System.out.println("Colunas obrigatórias não encontradas no CADOP local.");
                return;
            }

            String linha;

            while((linha = reader.readLine()) != null) {

                String linhaLimpa = linha.replace("\"", "");
                String[] dados = linhaLimpa.split(separador);

                int indiceMaximo = Math.max(indiceRegAns, Math.max(indiceCnpj, indiceRazaoSocial));

                if (dados.length > indiceMaximo) {
                    String regAns = dados[indiceRegAns].trim();
                    String cnpj = dados[indiceCnpj].trim();
                    String razaoSocial = dados[indiceRazaoSocial].trim();

                    cadastroOperadoras.put(regAns, new OperadoraCadastro(cnpj,razaoSocial));
                }
            }

            System.out.println("Cadastro CADOP carregado: " + cadastroOperadoras.size() + " operadoras mapeadas");

        }
        catch (IOException e) {
            System.err.println("Erro ao ler o arquivo CADOP local");
        }

    }

    public static void processarAquivo(Path caminhoArquivo) {

        System.out.println("Processando arquivo " + caminhoArquivo.getFileName() + "...");

        String linha;

        try(BufferedReader reader = new BufferedReader(new FileReader(caminhoArquivo.toFile()))){

            String cabecalho = reader.readLine();
            if (cabecalho == null) return;

            cabecalho = cabecalho.replace("\"", "");

            String separador = cabecalho.contains(";") ? ";" : ",";
            String[] colunasCabecalho = cabecalho.split(separador);

            int indiceConta = -1;
            int indiceDescricao = -1;
            int indiceValor = -1;
            int indiceRegAns = -1;
            int indiceData = -1;

            for (int i = 0; i < colunasCabecalho.length; i++) {

                String nomeColuna = colunasCabecalho[i].trim();

                if (nomeColuna.equalsIgnoreCase("CD_CONTA_CONTABIL")) {
                    indiceConta = i;
                }
                else if (nomeColuna.equalsIgnoreCase("DESCRICAO")) {
                    indiceDescricao = i;
                }
                else if (nomeColuna.equalsIgnoreCase("VL_SALDO_FINAL")) {
                    indiceValor = i;
                }
                else if (nomeColuna.equalsIgnoreCase("REG_ANS")) {
                    indiceRegAns = i;
                }
                else if (nomeColuna.equalsIgnoreCase("DATA")) {
                    indiceData = i;
                }
            }

            if (indiceValor == -1 && indiceConta == -1){
                System.out.println("Aquivo invalido! Colunas não encontradas!");
                return;
            }

            String ano = "";
            String trimestre = "";

            while((linha = reader.readLine()) != null){

                String linhaLimpa = linha.replace("\"", "");
                String[] colunas = linhaLimpa.split(separador);

                int indiceMaximo = Math.max(indiceConta, Math.max(indiceDescricao,
                        Math.max(indiceValor, Math.max(indiceRegAns, indiceData))));

                if (colunas.length > indiceMaximo) {

                    String contaContabil = colunas[indiceConta];

                    if (contaContabil.startsWith("411")){

                        String descricao = (indiceDescricao != -1) ? colunas[indiceDescricao] : "";
                        String valorTexto = colunas[indiceValor];

                        double valorNormalizado = converterParaDouble(valorTexto);

                        if (valorNormalizado <= 0.0){
                            continue;
                        }

                        String data = colunas[indiceData].trim();
                        ano = data.length() >= 4 ? data.substring(0, 4) : "Inconsistente";
                        trimestre = "Inconsistente";

                        if (data.length() >= 7){

                            String mes = data.substring(5, 7);
                            trimestre = obterTrimestre(mes);
                        }

                        String regAns = colunas[indiceRegAns].trim();
                        String cnpj = buscarCnpjPorAns(regAns);
                        String razaoSocial = buscarRazaoSocial(regAns);

                        String chaveUnica = cnpj + "_" + trimestre + "_" + ano;

                        if (consolidadoMap.containsKey(chaveUnica)){
                            consolidadoMap.get(chaveUnica).adicionarValor(valorNormalizado);
                        }
                        else{
                            consolidadoMap.put(chaveUnica, new ConsolidaDTO(cnpj, razaoSocial,trimestre,ano,valorNormalizado));
                        }
                    }

                }
        }
        } catch (IOException e) {
            System.err.println("Erro ao ler ou processar o arquivo " + caminhoArquivo.getFileName());
        }
    }

    private static String obterTrimestre(String mes){

        switch (mes){
            case "01": case "02": case "03": return "1º Trimestre";
            case "04": case "05": case "06": return "2º Trimestre";
            case "07": case "08": case "09": return "3º Trimestre";
            case "10": case "11": case "12": return "4º Trimestre";
            default: return "Inconsistente";
        }
    }

    private static double converterParaDouble(String texto){

        if (texto == null || texto.isEmpty()) return 0.0;
        String limpo = texto.trim().replace("\"", "");
        if (limpo.isEmpty() || limpo.equals("-")) return 0.0;

        limpo = limpo.replace(",",".");

        try {
            return Double.parseDouble(limpo);
        }
        catch (NumberFormatException e){
            return 0.0;
        }
    }

    private static String buscarCnpjPorAns(String regAns){

        OperadoraCadastro operadora = cadastroOperadoras.get(regAns);

        if ((operadora != null) && operadora.getCnpj() != null){
            return operadora.getCnpj();
        }
        return "Não encontrado - " + regAns;
    }

    private static String buscarRazaoSocial(String regAns){

        OperadoraCadastro operadora = cadastroOperadoras.get(regAns);
        if ((operadora != null) && operadora.getRazaoSocial() != null){
            return operadora.getRazaoSocial();
        }
        return "Não encontrado - " + regAns;
    }

    public static void salvarCsvConsolidado(Path caminhoSaida) {

        System.out.println("Gerando arquivo CSV em: " + caminhoSaida.getFileName() + "...");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminhoSaida.toFile()))) {

            writer.write("CNPJ;RazaoSocial;Trimestre;Ano;ValorDespesas");
            writer.newLine();

            for (ConsolidaDTO dto : consolidadoMap.values()) {

                String cnpj = dto.cnpj != null ? dto.cnpj : "";
                String razaoSocial = dto.razaoSocial != null ? dto.razaoSocial : "";

                String linha = String.format("%s,%s,%s,%s,%.2f",
                        cnpj,
                        razaoSocial,
                        dto.trimestre,
                        dto.ano,
                        dto.valorDespesas);

                writer.write(linha);
                writer.newLine();
            }

            System.out.println("Consolidado com sucesso!");
        }
        catch (IOException e){
            System.err.println("Erro ao salvar o arquivo CSV consolidado");
        }
    }
}
