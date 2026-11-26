package cabenobolso.view;

import cabenobolso.model.Despesa;
import cabenobolso.model.Participante;
import cabenobolso.model.Planejamento;
import cabenobolso.model.SimuladorAlternativas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 * VISÃO do MVC (revisões 13 e 14).
 *
 * Monta os componentes Swing, devolve o texto digitado (lerX), liga botões a
 * ações recebidas do controlador (aoX(Runnable)) e apresenta dados e mensagens
 * (mostrarX). Não converte números, não valida regras e não calcula nada:
 * isso é papel do controlador e do modelo.
 *
 * Valores são digitados em centavos inteiros (9000 = R$ 90,00), como na trilha.
 * Nas tabelas, a visão apenas formata os centavos como reais.
 */
public class TelaPrincipal {
    private final JFrame janela = new JFrame("Cabe no bolso — Planejador de despesas compartilhadas");

    // ----- barra superior: arquivo -----
    private final JButton novo = new JButton("Novo");
    private final JButton abrir = new JButton("Abrir...");
    private final JButton salvar = new JButton("Salvar");
    private final JButton salvarComo = new JButton("Salvar como...");
    private final JLabel arquivoAtual = new JLabel("Arquivo: (não salvo)");

    // ----- aba Participantes -----
    private final JTextField codigo = new JTextField(8);
    private final JTextField nome = new JTextField(18);
    private final JTextField orcamento = new JTextField(10);
    private final JButton cadastrarParticipante = new JButton("Cadastrar");
    private final JButton alterarParticipante = new JButton("Alterar nome/orçamento");
    private final JButton excluirParticipante = new JButton("Excluir");
    private final JButton limparParticipante = new JButton("Limpar campos");
    private final DefaultTableModel modeloParticipantes =
            tabelaSomenteLeitura("Código", "Nome", "Orçamento");
    private final JTable tabelaParticipantes = new JTable(modeloParticipantes);

    // ----- aba Despesas -----
    private final JTextField descricao = new JTextField(18);
    private final JTextField valor = new JTextField(10);
    private final JComboBox<String> tipo = new JComboBox<>(new String[] {"Fixa", "Por pessoa"});
    private final DefaultListModel<Participante> modeloSelecao = new DefaultListModel<>();
    private final JList<Participante> listaSelecao = new JList<>(modeloSelecao);
    private final JButton cadastrarDespesa = new JButton("Cadastrar");
    private final JButton salvarEdicaoDespesa = new JButton("Salvar edição");
    private final JButton excluirDespesa = new JButton("Excluir");
    private final JButton limparDespesa = new JButton("Limpar campos");
    private final DefaultTableModel modeloDespesas =
            tabelaSomenteLeitura("Descrição", "Tipo", "Valor informado", "Participantes", "Custo");
    private final JTable tabelaDespesas = new JTable(modeloDespesas);

    // ----- aba Resumo -----
    private final DefaultTableModel modeloResumo = tabelaSomenteLeitura("Pessoa");
    private final JTable tabelaResumo = new JTable(modeloResumo);
    private final JLabel custoTotal = new JLabel("Custo total: R$ 0,00");
    private final JButton simularAlternativas = new JButton("Simular alternativas");

    // ----- rodapé -----
    private final JLabel mensagem = new JLabel("Cadastre os participantes e depois as despesas.");

    private final JFileChooser seletorArquivo = new JFileChooser();

    public TelaPrincipal() {
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Participantes", montarAbaParticipantes());
        abas.addTab("Despesas", montarAbaDespesas());
        abas.addTab("Resumo", montarAbaResumo());

        JPanel barraArquivo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        barraArquivo.add(novo);
        barraArquivo.add(abrir);
        barraArquivo.add(salvar);
        barraArquivo.add(salvarComo);
        barraArquivo.add(arquivoAtual);

        mensagem.setBorder(BorderFactory.createEmptyBorder(6, 4, 2, 4));

        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        painel.add(barraArquivo, BorderLayout.NORTH);
        painel.add(abas, BorderLayout.CENTER);
        painel.add(mensagem, BorderLayout.SOUTH);

        File pastaDados = new File("dados");
        if (pastaDados.isDirectory()) {
            seletorArquivo.setCurrentDirectory(pastaDados);
        }
        seletorArquivo.setFileFilter(new FileNameExtensionFilter("Planejamento (*.dat)", "dat"));

        janela.setContentPane(painel);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JPanel montarAbaParticipantes() {
        JPanel campos = new JPanel(new GridLayout(0, 2, 8, 8));
        adicionarCampo(campos, "Código", codigo);
        adicionarCampo(campos, "Nome", nome);
        adicionarCampo(campos, "Orçamento (centavos)", orcamento);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botoes.add(cadastrarParticipante);
        botoes.add(alterarParticipante);
        botoes.add(excluirParticipante);
        botoes.add(limparParticipante);

        JPanel topo = new JPanel(new BorderLayout(8, 8));
        topo.add(campos, BorderLayout.NORTH);
        topo.add(botoes, BorderLayout.SOUTH);

        tabelaParticipantes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel aba = new JPanel(new BorderLayout(8, 8));
        aba.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        aba.add(topo, BorderLayout.NORTH);
        aba.add(new JScrollPane(tabelaParticipantes), BorderLayout.CENTER);
        return aba;
    }

    private JPanel montarAbaDespesas() {
        JPanel campos = new JPanel(new GridLayout(0, 2, 8, 8));
        adicionarCampo(campos, "Descrição", descricao);
        adicionarCampo(campos, "Valor (centavos)", valor);
        adicionarCampo(campos, "Tipo", tipo);

        listaSelecao.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaSelecao.setVisibleRowCount(4);
        JPanel selecao = new JPanel(new BorderLayout(4, 4));
        selecao.add(new JLabel("Participantes (Ctrl+clique para vários)"), BorderLayout.NORTH);
        selecao.add(new JScrollPane(listaSelecao), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new GridLayout(1, 2, 12, 0));
        formulario.add(campos);
        formulario.add(selecao);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botoes.add(cadastrarDespesa);
        botoes.add(salvarEdicaoDespesa);
        botoes.add(excluirDespesa);
        botoes.add(limparDespesa);

        JPanel topo = new JPanel(new BorderLayout(8, 8));
        topo.add(formulario, BorderLayout.NORTH);
        topo.add(botoes, BorderLayout.SOUTH);

        tabelaDespesas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel aba = new JPanel(new BorderLayout(8, 8));
        aba.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        aba.add(topo, BorderLayout.NORTH);
        aba.add(new JScrollPane(tabelaDespesas), BorderLayout.CENTER);
        return aba;
    }

    private JPanel montarAbaResumo() {
        JPanel aba = new JPanel(new BorderLayout(8, 8));
        aba.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        aba.add(new JScrollPane(tabelaResumo), BorderLayout.CENTER);
        JPanel rodapeResumo = new JPanel(new BorderLayout(8, 0));
        rodapeResumo.add(custoTotal, BorderLayout.WEST);
        rodapeResumo.add(simularAlternativas, BorderLayout.EAST);
        aba.add(rodapeResumo, BorderLayout.SOUTH);
        return aba;
    }

    private static void adicionarCampo(JPanel painel, String texto, java.awt.Component campo) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setLabelFor(campo);
        painel.add(rotulo);
        painel.add(campo);
    }

    private static DefaultTableModel tabelaSomenteLeitura(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
    }

    // ===================== leitura dos campos =====================

    public String lerCodigo() {
        return codigo.getText();
    }

    public String lerNome() {
        return nome.getText();
    }

    public String lerOrcamento() {
        return orcamento.getText();
    }

    public String lerDescricao() {
        return descricao.getText();
    }

    public String lerValor() {
        return valor.getText();
    }

    public boolean tipoFixaSelecionado() {
        return tipo.getSelectedIndex() == 0;
    }

    public List<Integer> lerCodigosSelecionados() {
        List<Integer> codigos = new ArrayList<>();
        for (Participante participante : listaSelecao.getSelectedValuesList()) {
            codigos.add(participante.getCodigo());
        }
        return codigos;
    }

    /** Posição da despesa selecionada na tabela, ou -1. */
    public int lerIndiceDespesa() {
        return tabelaDespesas.getSelectedRow();
    }

    /** Código do participante selecionado na tabela, ou -1. */
    public int lerCodigoParticipanteSelecionado() {
        int linha = tabelaParticipantes.getSelectedRow();
        if (linha < 0) {
            return -1;
        }
        return (Integer) modeloParticipantes.getValueAt(linha, 0);
    }

    // ===================== ligação das ações (controlador) =====================

    public void aoNovo(Runnable acao) {
        novo.addActionListener(evento -> acao.run());
    }

    public void aoAbrir(Runnable acao) {
        abrir.addActionListener(evento -> acao.run());
    }

    public void aoSalvar(Runnable acao) {
        salvar.addActionListener(evento -> acao.run());
    }

    public void aoSalvarComo(Runnable acao) {
        salvarComo.addActionListener(evento -> acao.run());
    }

    public void aoSimularAlternativas(Runnable acao) {
        simularAlternativas.addActionListener(evento -> acao.run());
    }

    public void aoCadastrarParticipante(Runnable acao) {
        cadastrarParticipante.addActionListener(evento -> acao.run());
    }

    public void aoAlterarParticipante(Runnable acao) {
        alterarParticipante.addActionListener(evento -> acao.run());
    }

    public void aoExcluirParticipante(Runnable acao) {
        excluirParticipante.addActionListener(evento -> acao.run());
    }

    public void aoSelecionarParticipante(Runnable acao) {
        tabelaParticipantes.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) {
                acao.run();
            }
        });
    }

    public void aoCadastrarDespesa(Runnable acao) {
        cadastrarDespesa.addActionListener(evento -> acao.run());
    }

    public void aoSalvarEdicaoDespesa(Runnable acao) {
        salvarEdicaoDespesa.addActionListener(evento -> acao.run());
    }

    public void aoExcluirDespesa(Runnable acao) {
        excluirDespesa.addActionListener(evento -> acao.run());
    }

    public void aoSelecionarDespesa(Runnable acao) {
        tabelaDespesas.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) {
                acao.run();
            }
        });
    }

    // ===================== apresentação =====================

    /** Atualiza as três abas a partir do modelo. */
    public void mostrarPlanejamento(Planejamento planejamento) {
        List<Participante> participantes = planejamento.getParticipantes();
        List<Despesa> despesas = planejamento.getDespesas();

        // Participantes
        modeloParticipantes.setRowCount(0);
        modeloSelecao.clear();
        for (Participante p : participantes) {
            modeloParticipantes.addRow(new Object[] {p.getCodigo(), p.getNome(), reais(p.getOrcamento())});
            modeloSelecao.addElement(p);
        }

        // Despesas
        modeloDespesas.setRowCount(0);
        for (Despesa d : despesas) {
            StringJoiner nomes = new StringJoiner(", ");
            for (int c : d.getCodigosParticipantes()) {
                nomes.add(planejamento.buscarParticipante(c).getNome());
            }
            modeloDespesas.addRow(new Object[] {
                d.getDescricao(), d.getTipo(), reais(d.getValorInformado()), nomes.toString(), reais(d.calcularTotal())
            });
        }

        // Resumo: uma coluna por despesa, como a tabela da seção 4 do enunciado
        List<String> colunas = new ArrayList<>();
        colunas.add("Pessoa");
        for (Despesa d : despesas) {
            colunas.add(d.getDescricao());
        }
        colunas.add("Total");
        colunas.add("Orçamento");
        colunas.add("Situação");
        modeloResumo.setColumnIdentifiers(colunas.toArray());
        modeloResumo.setRowCount(0);
        for (Participante p : participantes) {
            List<Object> linha = new ArrayList<>();
            linha.add(p.getNome());
            for (Despesa d : despesas) {
                linha.add(reais(d.calcularCota(p.getCodigo())));
            }
            int total = planejamento.calcularTotalParticipante(p.getCodigo());
            linha.add(reais(total));
            linha.add(reais(p.getOrcamento()));
            linha.add(p.cabeNoOrcamento(total)
                    ? "Dentro do orçamento"
                    : "Excede em " + reais(p.calcularExcesso(total)));
            modeloResumo.addRow(linha.toArray());
        }
        // A coluna Situação recebe mais espaço para a mensagem não ficar cortada.
        int ultima = tabelaResumo.getColumnCount() - 1;
        tabelaResumo.getColumnModel().getColumn(ultima).setPreferredWidth(220);
        custoTotal.setText("Custo total: " + reais(planejamento.calcularTotal()));
    }

    public void preencherParticipante(Participante p) {
        codigo.setText(String.valueOf(p.getCodigo()));
        nome.setText(p.getNome());
        orcamento.setText(String.valueOf(p.getOrcamento()));
    }

    public void preencherDespesa(Despesa d) {
        descricao.setText(d.getDescricao());
        valor.setText(String.valueOf(d.getValorInformado()));
        tipo.setSelectedIndex(d.getTipo().equals("Fixa") ? 0 : 1);
        listaSelecao.clearSelection();
        for (int i = 0; i < modeloSelecao.size(); i++) {
            if (d.envolve(modeloSelecao.get(i).getCodigo())) {
                listaSelecao.addSelectionInterval(i, i);
            }
        }
    }

    public void limparCamposParticipante() {
        codigo.setText("");
        nome.setText("");
        orcamento.setText("");
        tabelaParticipantes.clearSelection();
    }

    public void limparCamposDespesa() {
        descricao.setText("");
        valor.setText("");
        tipo.setSelectedIndex(0);
        listaSelecao.clearSelection();
        tabelaDespesas.clearSelection();
    }

    public void ligarLimpezaDosCampos() {
        limparParticipante.addActionListener(evento -> limparCamposParticipante());
        limparDespesa.addActionListener(evento -> limparCamposDespesa());
    }

    public void mostrarMensagem(String texto) {
        mensagem.setForeground(new Color(0, 110, 0));
        mensagem.setText(texto);
    }

    public void mostrarErro(String texto) {
        mensagem.setForeground(new Color(180, 0, 0));
        mensagem.setText(texto);
    }

    public void mostrarArquivoAtual(Path arquivo) {
        arquivoAtual.setText("Arquivo: " + (arquivo == null ? "(não salvo)" : arquivo.getFileName()));
    }

    public boolean confirmar(String pergunta) {
        int resposta = JOptionPane.showConfirmDialog(janela, pergunta, "Confirmar", JOptionPane.YES_NO_OPTION);
        return resposta == JOptionPane.YES_OPTION;
    }

    // ----- simulador de alternativas (objetivo extra) -----

    /** Pede as prioridades (texto separado por vírgulas), ou null se a pessoa cancelar. */
    public String pedirPrioridadesSimulacao(List<Despesa> despesas) {
        StringBuilder texto = new StringBuilder(
                "Digite uma prioridade para cada despesa, separando por vírgulas.\n"
                + "Use 0 para essencial e um número positivo para opcional.\n"
                + "Exemplo para quatro despesas: 0, 0, 3, 1\n\n");
        for (int i = 0; i < despesas.size(); i++) {
            texto.append(i + 1).append(". ").append(despesas.get(i).getDescricao()).append("\n");
        }
        return JOptionPane.showInputDialog(
                janela, texto.toString(), "Simular alternativas", JOptionPane.QUESTION_MESSAGE);
    }

    /** Mostra despesas mantidas e retiradas e os totais antes e depois. Só apresentação. */
    public void mostrarResultadoSimulacao(Planejamento planejamento, SimuladorAlternativas.Resultado resultado) {
        StringBuilder texto = new StringBuilder();

        if (!resultado.viavel()) {
            texto.append("Sem solução. As despesas essenciais já ultrapassam o orçamento.\n\n");
            for (Participante pessoa : planejamento.getParticipantes()) {
                Long excesso = resultado.excessoPorParticipante().get(pessoa.getCodigo());
                if (excesso != null) {
                    texto.append(pessoa.getNome()).append(" excede em ").append(reais(excesso)).append("\n");
                }
            }
        } else {
            texto.append("Despesas mantidas:\n");
            for (Despesa despesa : resultado.mantidas()) {
                texto.append("- ").append(despesa.getDescricao()).append("\n");
            }

            texto.append("\nDespesas retiradas:\n");
            if (resultado.retiradas().isEmpty()) {
                texto.append("Nenhuma.\n");
            }
            for (Despesa despesa : resultado.retiradas()) {
                texto.append("- ").append(despesa.getDescricao()).append("\n");
            }

            texto.append("\nAntes → Depois por participante:\n");
            long totalDepois = 0;
            for (Participante pessoa : planejamento.getParticipantes()) {
                int codigo = pessoa.getCodigo();
                long depois = resultado.totalPorParticipante().get(codigo);
                totalDepois += depois;
                texto.append(pessoa.getNome()).append(": ")
                        .append(reais(planejamento.calcularTotalParticipante(codigo)))
                        .append(" → ").append(reais(depois)).append("\n");
            }

            texto.append("\nTotal do grupo: ").append(reais(planejamento.calcularTotal()))
                    .append(" → ").append(reais(totalDepois));
        }

        JTextArea area = new JTextArea(texto.toString(), 18, 48);
        area.setEditable(false);
        area.setCaretPosition(0);
        JOptionPane.showMessageDialog(
                janela, new JScrollPane(area), "Resultado da simulação", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Devolve o arquivo escolhido para abrir, ou null se a pessoa cancelar. */
    public Path escolherArquivoParaAbrir() {
        if (seletorArquivo.showOpenDialog(janela) == JFileChooser.APPROVE_OPTION) {
            return seletorArquivo.getSelectedFile().toPath();
        }
        return null;
    }

    /** Devolve o arquivo escolhido para salvar (com .dat), ou null se cancelar. */
    public Path escolherArquivoParaSalvar() {
        if (seletorArquivo.showSaveDialog(janela) == JFileChooser.APPROVE_OPTION) {
            File arquivo = seletorArquivo.getSelectedFile();
            if (!arquivo.getName().toLowerCase().endsWith(".dat")) {
                arquivo = new File(arquivo.getParentFile(), arquivo.getName() + ".dat");
            }
            return arquivo.toPath();
        }
        return null;
    }

    public void exibir() {
        janela.pack();
        janela.setSize(Math.max(janela.getWidth(), 1000), Math.max(janela.getHeight(), 580));
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);
    }

    /** Formata centavos como reais: 7500 -> "R$ 75,00". Só apresentação. */
    private static String reais(long centavos) {
        String sinal = centavos < 0 ? "-" : "";
        long valorAbsoluto = Math.abs(centavos);
        return String.format("%sR$ %d,%02d", sinal, valorAbsoluto / 100, valorAbsoluto % 100);
    }
}
