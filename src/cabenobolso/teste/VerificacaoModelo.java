package cabenobolso.teste;

import cabenobolso.model.Despesa;
import cabenobolso.model.Participante;
import cabenobolso.model.Planejamento;
import cabenobolso.persistence.RepositorioArquivo;
import cabenobolso.persistence.RepositorioPlanejamento;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

/**
 * Verificação do MODELO e da PERSISTÊNCIA pelo console, antes da tela
 * ("validem a UML e os cálculos antes de conectá-los às telas", seção 3).
 * Como na revisão 14, o modelo é usado sem a janela.
 *
 * Executa o caso de referência (seção 4) e os testes adicionais 2 a 7,
 * imprimindo [OK] ou [FALHOU] em cada verificação.
 *
 * Uso:
 *   java -cp out cabenobolso.teste.VerificacaoModelo
 *   java -cp out cabenobolso.teste.VerificacaoModelo --gerar-dados   (grava dados/caso-referencia.dat)
 */
public class VerificacaoModelo {

    private static int ok = 0;
    private static int falhas = 0;

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equals("--gerar-dados")) {
            Path arquivo = Path.of("dados", "caso-referencia.dat");
            Files.createDirectories(arquivo.getParent());
            new RepositorioArquivo(arquivo).salvar(casoReferencia());
            System.out.println("Gerado: " + arquivo);
            return;
        }

        teste1CasoReferencia();
        teste2Centavos();
        teste3EntradasInvalidas();
        teste4AlteracaoOrcamento();
        teste5ExclusaoDespesa();
        teste6SalvarEReiniciar();
        teste7ProblemaNoArquivo();
        testeExtraExclusaoParticipanteVinculado();

        System.out.println();
        System.out.println("Resultado: " + ok + " verificações OK, " + falhas + " falharam.");
        if (falhas > 0) {
            System.exit(1);
        }
    }

    /** Caso de referência da seção 4 (valores em centavos). */
    static Planejamento casoReferencia() {
        Planejamento plano = new Planejamento("Passeio da turma");
        plano.cadastrarParticipante(1, "Ana", 7000);
        plano.cadastrarParticipante(2, "Bruno", 6000);
        plano.cadastrarParticipante(3, "Carla", 7000);

        plano.cadastrarDespesaFixa("Transporte", 9000, List.of(1, 2, 3));
        plano.cadastrarDespesaPorPessoa("Almoço", 2000, List.of(1, 2, 3));
        plano.cadastrarDespesaFixa("Atividade recreativa", 3000, List.of(1, 3));
        plano.cadastrarDespesaPorPessoa("Sobremesa", 1000, List.of(1, 2, 3));
        return plano;
    }

    static void teste1CasoReferencia() {
        titulo("1. Caso de referência");
        Planejamento plano = casoReferencia();
        imprimirResumo(plano);

        verificar("Ana total R$ 75,00", plano.calcularTotalParticipante(1), 7500);
        verificar("Bruno total R$ 60,00", plano.calcularTotalParticipante(2), 6000);
        verificar("Carla total R$ 75,00", plano.calcularTotalParticipante(3), 7500);
        verificar("Bruno paga 0 na atividade", plano.getDespesas().get(2).calcularCota(2), 0);
        verificar("Ana excede R$ 5,00", plano.calcularExcesso(1), 500);
        verificar("Carla excede R$ 5,00", plano.calcularExcesso(3), 500);
        verificarBool("Bruno dentro (igual ao limite)", plano.cabeNoOrcamento(2));
        verificar("Custo total R$ 210,00", plano.calcularTotal(), 21000);
    }

    static void teste2Centavos() {
        titulo("2. Centavos no rateio (R$ 100,00 fixa para 1, 2 e 3)");
        Planejamento plano = new Planejamento("Teste centavos");
        plano.cadastrarParticipante(1, "Ana", 10000);
        plano.cadastrarParticipante(2, "Bruno", 10000);
        plano.cadastrarParticipante(3, "Carla", 10000);
        // Seleção fora de ordem de propósito: o resultado deve seguir o código.
        plano.cadastrarDespesaFixa("Teste", 10000, List.of(3, 1, 2));
        Despesa despesa = plano.getDespesas().get(0);

        verificar("Código 1 paga R$ 33,34", despesa.calcularCota(1), 3334);
        verificar("Código 2 paga R$ 33,33", despesa.calcularCota(2), 3333);
        verificar("Código 3 paga R$ 33,33", despesa.calcularCota(3), 3333);
        verificar("Soma R$ 100,00",
                despesa.calcularCota(1) + despesa.calcularCota(2) + despesa.calcularCota(3), 10000);
    }

    static void teste3EntradasInvalidas() {
        titulo("3. Entrada ou vínculo inválido");
        Planejamento plano = casoReferencia();
        int despesasAntes = plano.getDespesas().size();
        long totalAntes = plano.calcularTotal();

        esperarErro("Despesa sem participantes", () -> plano.cadastrarDespesaFixa("X", 1000, List.of()));
        esperarErro("Valor negativo", () -> plano.cadastrarDespesaFixa("X", -1000, List.of(1)));
        esperarErro("Valor zero", () -> plano.cadastrarDespesaPorPessoa("X", 0, List.of(1)));
        esperarErro("Descrição vazia", () -> plano.cadastrarDespesaFixa("  ", 1000, List.of(1)));
        esperarErro("Participante repetido", () -> plano.cadastrarDespesaFixa("X", 1000, List.of(1, 1)));
        esperarErro("Código não cadastrado", () -> plano.cadastrarDespesaFixa("X", 1000, List.of(9)));
        esperarErro("Código duplicado", () -> plano.cadastrarParticipante(1, "Outra Ana", 1000));
        esperarErro("Nome vazio", () -> plano.cadastrarParticipante(9, "", 1000));
        esperarErro("Orçamento negativo", () -> plano.alterarParticipante(1, "Ana", -1));
        esperarErro("Edição inválida", () -> plano.editarDespesaFixa(0, "Transporte", 0, List.of(1, 2, 3)));
        // Texto digitado que não é número: tratado pelo controlador (revisão 14).
        esperarErroConversao("Valor digitado \"noventa\"", "noventa");

        verificar("Nº de despesas inalterado", plano.getDespesas().size(), despesasAntes);
        verificar("Custo total inalterado", plano.calcularTotal(), totalAntes);
        Participante ana = plano.buscarParticipante(1);
        verificarBool("Nome e orçamento de Ana inalterados",
                ana.getNome().equals("Ana") && ana.getOrcamento() == 7000);
    }

    static void teste4AlteracaoOrcamento() {
        titulo("4. Alteração de orçamento (Ana -> R$ 80,00)");
        Planejamento plano = casoReferencia();
        plano.alterarParticipante(1, "Ana", 8000);
        verificar("Ana continua com R$ 75,00", plano.calcularTotalParticipante(1), 7500);
        verificarBool("Ana agora dentro do orçamento", plano.cabeNoOrcamento(1));
    }

    static void teste5ExclusaoDespesa() {
        titulo("5. Exclusão de despesa (Atividade recreativa)");
        Planejamento plano = casoReferencia();
        plano.removerDespesa(2);
        for (Participante p : plano.getParticipantes()) {
            verificar(p.getNome() + " paga R$ 60,00", plano.calcularTotalParticipante(p.getCodigo()), 6000);
        }
        verificar("Total R$ 180,00", plano.calcularTotal(), 18000);
    }

    static void teste6SalvarEReiniciar() throws IOException {
        titulo("6. Salvar e reiniciar");
        Path pasta = Files.createTempDirectory("cabenobolso");
        Path arquivo = pasta.resolve("teste6.dat");
        RepositorioPlanejamento repositorio = new RepositorioArquivo(arquivo);   // tipo do contrato

        repositorio.salvar(casoReferencia());
        repositorio.salvar(casoReferencia());   // salvar de novo substitui, não acumula
        Planejamento lido = repositorio.carregar();

        verificar("3 participantes recuperados", lido.getParticipantes().size(), 3);
        verificar("4 despesas recuperadas (sem duplicar)", lido.getDespesas().size(), 4);
        verificar("Custo total R$ 210,00", lido.calcularTotal(), 21000);
        verificar("Ana total R$ 75,00", lido.calcularTotalParticipante(1), 7500);
        verificarBool("Vínculos preservados (atividade: Ana e Carla)",
                lido.getDespesas().get(2).getCodigosParticipantes().equals(java.util.Set.of(1, 3)));

        Files.deleteIfExists(arquivo);
        Files.deleteIfExists(pasta);
    }

    static void teste7ProblemaNoArquivo() throws IOException {
        titulo("7. Problema no arquivo");
        Planejamento emUso = casoReferencia();
        Path pasta = Files.createTempDirectory("cabenobolso");

        Path inexistente = pasta.resolve("nao-existe.dat");
        Path invalido = pasta.resolve("texto-renomeado.dat");
        Files.writeString(invalido, "isto é um .txt renomeado para .dat");

        for (Path caminho : List.of(inexistente, invalido)) {
            try {
                emUso = new RepositorioArquivo(caminho).carregar();   // só troca se carregar
                falhou("Deveria recusar " + caminho.getFileName());
            } catch (NoSuchFileException erro) {
                passou("Arquivo ausente -> \"Arquivo ou pasta do planejamento não encontrado.\"");
            } catch (IOException erro) {
                passou("Arquivo inválido -> \"Não foi possível acessar o arquivo do planejamento.\"");
            }
        }
        verificar("Planejamento em uso preservado", emUso.calcularTotal(), 21000);
        verificar("Planejamento vazio pode ser iniciado", new Planejamento().getDespesas().size(), 0);

        Files.deleteIfExists(invalido);
        Files.deleteIfExists(pasta);
    }

    static void testeExtraExclusaoParticipanteVinculado() {
        titulo("Extra. Exclusão de participante vinculado");
        Planejamento plano = casoReferencia();
        esperarErro("Excluir Bruno com despesas", () -> plano.removerParticipante(2));
        verificar("Bruno continua cadastrado", plano.getParticipantes().size(), 3);
    }

    // ---------------- apoio ----------------

    static void imprimirResumo(Planejamento plano) {
        System.out.printf("%-8s %10s %10s  %s%n", "Pessoa", "Total", "Orçamento", "Situação");
        for (Participante p : plano.getParticipantes()) {
            int total = plano.calcularTotalParticipante(p.getCodigo());
            String situacao = p.cabeNoOrcamento(total)
                    ? "Dentro do orçamento"
                    : "Excede em " + reais(p.calcularExcesso(total));
            System.out.printf("%-8s %10s %10s  %s%n", p.getNome(), reais(total), reais(p.getOrcamento()), situacao);
        }
        System.out.println("Custo total: " + reais(plano.calcularTotal()));
    }

    static String reais(long centavos) {
        return String.format("R$ %d,%02d", centavos / 100, centavos % 100);
    }

    static void esperarErro(String nome, Runnable acao) {
        try {
            acao.run();
            falhou(nome + " -> deveria ter sido recusado");
        } catch (IllegalArgumentException erro) {
            passou(nome + " -> recusado: " + erro.getMessage());
        }
    }

    static void esperarErroConversao(String nome, String texto) {
        try {
            Integer.parseInt(texto.trim());
            falhou(nome + " -> deveria falhar na conversão");
        } catch (NumberFormatException erro) {
            passou(nome + " -> recusado: Digite um número inteiro em centavos.");
        }
    }

    static void verificar(String nome, long obtido, long esperado) {
        if (obtido == esperado) {
            passou(nome);
        } else {
            falhou(nome + " (esperado " + esperado + ", obtido " + obtido + ")");
        }
    }

    static void verificarBool(String nome, boolean condicao) {
        if (condicao) {
            passou(nome);
        } else {
            falhou(nome);
        }
    }

    static void passou(String mensagem) {
        ok++;
        System.out.println("  [OK]      " + mensagem);
    }

    static void falhou(String mensagem) {
        falhas++;
        System.out.println("  [FALHOU]  " + mensagem);
    }

    static void titulo(String texto) {
        System.out.println();
        System.out.println("== " + texto);
    }
}
