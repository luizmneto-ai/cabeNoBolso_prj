package cabenobolso.controller;

import cabenobolso.model.Despesa;
import cabenobolso.model.Participante;
import cabenobolso.model.Planejamento;
import cabenobolso.model.SimuladorAlternativas;
import cabenobolso.persistence.RepositorioArquivo;
import cabenobolso.persistence.RepositorioPlanejamento;
import cabenobolso.view.TelaPrincipal;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * CONTROLADOR do MVC (revisões 14 e 15).
 *
 * Caminho de um clique: a visão aciona a ação registrada aqui; o controlador
 * lê os campos, converte o texto em int, pede a operação ao modelo (ou ao
 * repositório) e manda a visão mostrar o resultado ou a mensagem de erro.
 *
 * Tratamento de erros (revisão 12):
 *  - NumberFormatException: texto que não é número inteiro;
 *  - IllegalArgumentException: regra do modelo recusada (a mensagem vem do modelo);
 *  - IOException: falha no arquivo (revisão 15).
 * O método converter troca a NumberFormatException por uma IllegalArgumentException
 * com mensagem clara; assim um único catch trata texto inválido e regra recusada.
 */
public class ControlePlanejamento {
    private Planejamento planejamento;   // não é final: "Novo" e "Abrir" trocam o planejamento
    private final TelaPrincipal tela;
    private Path arquivoAtual;            // null enquanto não foi salvo nem aberto

    public ControlePlanejamento(Planejamento planejamento, TelaPrincipal tela) {
        this.planejamento = planejamento;
        this.tela = tela;
    }

    public void iniciar() {
        tela.aoNovo(() -> novo());
        tela.aoAbrir(() -> abrir());
        tela.aoSalvar(() -> salvar(false));
        tela.aoSalvarComo(() -> salvar(true));
        tela.aoSimularAlternativas(this::simularAlternativas);

        tela.aoCadastrarParticipante(() -> cadastrarParticipante());
        tela.aoAlterarParticipante(() -> alterarParticipante());
        tela.aoExcluirParticipante(() -> excluirParticipante());
        tela.aoSelecionarParticipante(() -> selecionarParticipante());

        tela.aoCadastrarDespesa(() -> cadastrarDespesa());
        tela.aoSalvarEdicaoDespesa(() -> salvarEdicaoDespesa());
        tela.aoExcluirDespesa(() -> excluirDespesa());
        tela.aoSelecionarDespesa(() -> selecionarDespesa());

        tela.ligarLimpezaDosCampos();
        atualizarTela();
        tela.mostrarArquivoAtual(arquivoAtual);
        tela.exibir();
    }

    // ===================== Participantes =====================

    private void cadastrarParticipante() {
        try {
            int codigo = converter(tela.lerCodigo(), "Código");
            int orcamento = converter(tela.lerOrcamento(), "Orçamento");
            planejamento.cadastrarParticipante(codigo, tela.lerNome(), orcamento);
        } catch (IllegalArgumentException erro) {   // inclui NumberFormatException tratado em converter
            tela.mostrarErro(erro.getMessage());
            return;
        }
        atualizarTela();
        tela.limparCamposParticipante();
        tela.mostrarMensagem("Participante cadastrado.");
    }

    private void alterarParticipante() {
        try {
            int codigo = converter(tela.lerCodigo(), "Código");
            int orcamento = converter(tela.lerOrcamento(), "Orçamento");
            planejamento.alterarParticipante(codigo, tela.lerNome(), orcamento);
        } catch (IllegalArgumentException erro) {
            tela.mostrarErro(erro.getMessage());
            return;
        }
        atualizarTela();
        tela.limparCamposParticipante();
        tela.mostrarMensagem("Participante alterado. Totais recalculados.");
    }

    private void excluirParticipante() {
        try {
            int codigo = converter(tela.lerCodigo(), "Código");
            planejamento.removerParticipante(codigo);
        } catch (IllegalArgumentException erro) {
            tela.mostrarErro(erro.getMessage());
            return;
        }
        atualizarTela();
        tela.limparCamposParticipante();
        tela.mostrarMensagem("Participante excluído.");
    }

    private void selecionarParticipante() {
        int codigo = tela.lerCodigoParticipanteSelecionado();
        if (codigo > 0) {
            tela.preencherParticipante(planejamento.buscarParticipante(codigo));
        }
    }

    // ===================== Despesas =====================

    private void cadastrarDespesa() {
        try {
            int valor = converter(tela.lerValor(), "Valor");
            List<Integer> codigos = tela.lerCodigosSelecionados();
            if (tela.tipoFixaSelecionado()) {
                planejamento.cadastrarDespesaFixa(tela.lerDescricao(), valor, codigos);
            } else {
                planejamento.cadastrarDespesaPorPessoa(tela.lerDescricao(), valor, codigos);
            }
        } catch (IllegalArgumentException erro) {
            tela.mostrarErro(erro.getMessage());
            return;
        }
        atualizarTela();
        tela.limparCamposDespesa();
        tela.mostrarMensagem("Despesa cadastrada.");
    }

    private void salvarEdicaoDespesa() {
        try {
            int indice = tela.lerIndiceDespesa();
            int valor = converter(tela.lerValor(), "Valor");
            List<Integer> codigos = tela.lerCodigosSelecionados();
            if (tela.tipoFixaSelecionado()) {
                planejamento.editarDespesaFixa(indice, tela.lerDescricao(), valor, codigos);
            } else {
                planejamento.editarDespesaPorPessoa(indice, tela.lerDescricao(), valor, codigos);
            }
        } catch (IllegalArgumentException erro) {
            tela.mostrarErro(erro.getMessage());
            return;
        }
        atualizarTela();
        tela.limparCamposDespesa();
        tela.mostrarMensagem("Despesa alterada. Totais recalculados.");
    }

    private void excluirDespesa() {
        int indice = tela.lerIndiceDespesa();
        try {
            planejamento.removerDespesa(indice);
        } catch (IllegalArgumentException erro) {
            tela.mostrarErro(erro.getMessage());
            return;
        }
        atualizarTela();
        tela.limparCamposDespesa();
        tela.mostrarMensagem("Despesa excluída. Totais recalculados.");
    }

    private void selecionarDespesa() {
        int indice = tela.lerIndiceDespesa();
        if (indice >= 0) {
            tela.preencherDespesa(planejamento.getDespesas().get(indice));
        }
    }

    // ===================== Simulador de alternativas (objetivo extra) =====================

    /**
     * Pede uma prioridade por despesa (0 = essencial, positivo = opcional), roda o
     * simulador e mostra o resultado. O planejamento original não é alterado.
     */
    private void simularAlternativas() {
        List<Despesa> despesas = planejamento.getDespesas();
        if (despesas.isEmpty()) {
            tela.mostrarErro("Cadastre pelo menos uma despesa para simular.");
            return;
        }

        String entrada = tela.pedirPrioridadesSimulacao(despesas);
        if (entrada == null) {
            return;   // a pessoa cancelou
        }

        SimuladorAlternativas.Resultado resultado;
        try {
            List<Integer> prioridades = new ArrayList<>();
            for (String valor : entrada.split(",", -1)) {
                prioridades.add(Integer.parseInt(valor.trim()));
            }
            resultado = new SimuladorAlternativas().simular(planejamento, prioridades);
        } catch (NumberFormatException erro) {
            tela.mostrarErro("Digite números inteiros separados por vírgulas, como 0, 0, 3, 1.");
            return;
        } catch (IllegalArgumentException erro) {
            tela.mostrarErro(erro.getMessage());
            return;
        }
        tela.mostrarResultadoSimulacao(planejamento, resultado);
    }

    // ===================== Arquivo =====================

    private void novo() {
        if (!tela.confirmar("Iniciar um planejamento vazio? Os dados não salvos serão perdidos.")) {
            return;
        }
        planejamento = new Planejamento();
        arquivoAtual = null;
        tela.limparCamposParticipante();
        tela.limparCamposDespesa();
        atualizarTela();
        tela.mostrarArquivoAtual(null);
        tela.mostrarMensagem("Novo planejamento iniciado.");
    }

    private void salvar(boolean escolherOutroArquivo) {
        Path destino = arquivoAtual;
        if (destino == null || escolherOutroArquivo) {
            destino = tela.escolherArquivoParaSalvar();
            if (destino == null) {
                return;   // a pessoa cancelou
            }
        }
        RepositorioPlanejamento repositorio = new RepositorioArquivo(destino);   // tipo do contrato
        try {
            repositorio.salvar(planejamento);
        } catch (IOException erro) {
            tela.mostrarErro("Não foi possível salvar o planejamento em " + destino.getFileName() + ".");
            return;
        }
        arquivoAtual = destino;
        tela.mostrarArquivoAtual(arquivoAtual);

        // O enunciado permite salvar mesmo acima do orçamento, para revisão.
        int acima = contarAcimaDoOrcamento();
        if (acima > 0) {
            tela.mostrarMensagem("Planejamento salvo para revisão: " + acima
                    + " participante(s) acima do orçamento.");
        } else {
            tela.mostrarMensagem("Planejamento salvo.");
        }
    }

    private void abrir() {
        Path origem = tela.escolherArquivoParaAbrir();
        if (origem == null) {
            return;
        }
        RepositorioPlanejamento repositorio = new RepositorioArquivo(origem);
        Planejamento carregado;
        try {
            carregado = repositorio.carregar();
        } catch (NoSuchFileException erro) {
            tela.mostrarErro("Arquivo ou pasta do planejamento não encontrado. O planejamento atual foi mantido.");
            return;
        } catch (IOException erro) {
            tela.mostrarErro("Não foi possível ler o arquivo do planejamento. O planejamento atual foi mantido.");
            return;
        }
        // Só troca depois que a leitura terminou com sucesso.
        planejamento = carregado;
        arquivoAtual = origem;
        tela.limparCamposParticipante();
        tela.limparCamposDespesa();
        atualizarTela();
        tela.mostrarArquivoAtual(arquivoAtual);
        tela.mostrarMensagem("Planejamento carregado: " + planejamento.getTitulo() + ".");
    }

    // ===================== apoio =====================

    /** Converte o texto em int; troca a mensagem técnica por uma clara (revisão 14). */
    private int converter(String texto, String campo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("Digite um número inteiro em centavos no campo " + campo + ".");
        }
    }

    private int contarAcimaDoOrcamento() {
        int acima = 0;
        for (Participante p : planejamento.getParticipantes()) {
            if (!planejamento.cabeNoOrcamento(p.getCodigo())) {
                acima++;
            }
        }
        return acima;
    }

    private void atualizarTela() {
        tela.mostrarPlanejamento(planejamento);
    }
}
