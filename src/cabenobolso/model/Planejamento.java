package cabenobolso.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * MODELO do MVC (revisões 07, 11, 14 e 15): guarda participantes e despesas,
 * valida as operações e faz os cálculos. Não lê teclado nem mostra janelas.
 *
 * Coleções (justificativa para o relatório):
 *  - Map<Integer, Participante> (TreeMap): consulta pelo código, impede código
 *    repetido e lista os participantes em ordem crescente de código.
 *  - List<Despesa> (ArrayList): mantém a ordem de cadastro; a tela edita e
 *    exclui pela posição na lista. Aceita DespesaFixa e DespesaPorPessoa.
 *  - TreeSet<Integer> em cada Despesa: códigos sem repetição e em ordem crescente.
 *
 * Toda operação valida ANTES de alterar. Se lançar IllegalArgumentException,
 * os dados anteriores continuam iguais.
 */
public class Planejamento implements Serializable {
    private static final long serialVersionUID = 1L;

    private String titulo;
    private final Map<Integer, Participante> participantes = new TreeMap<>();
    private final List<Despesa> despesas = new ArrayList<>();

    public Planejamento() {
        this("Novo planejamento");
    }

    public Planejamento(String titulo) {
        this.titulo = validarTitulo(titulo);
    }

    public String getTitulo() {
        return titulo;
    }

    public void alterarTitulo(String novoTitulo) {
        this.titulo = validarTitulo(novoTitulo);
    }

    private static String validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Informe o título do planejamento.");
        }
        return titulo.trim();
    }

    // ===================== Participantes =====================

    public void cadastrarParticipante(int codigo, String nome, int orcamento) {
        // Revisão 11: conferir se o ID já existe antes de chamar put.
        if (participantes.containsKey(codigo)) {
            throw new IllegalArgumentException("Já existe participante com o código " + codigo + ".");
        }
        Participante participante = new Participante(codigo, nome, orcamento);
        participantes.put(codigo, participante);
    }

    /** Altera nome e orçamento. Valida os dois antes de mudar qualquer um. */
    public void alterarParticipante(int codigo, String novoNome, int novoOrcamento) {
        Participante participante = buscarParticipante(codigo);
        new Participante(codigo, novoNome, novoOrcamento);   // só para validar
        participante.alterarNome(novoNome);
        participante.alterarOrcamento(novoOrcamento);
    }

    /** Recusa a exclusão enquanto houver despesa vinculada ao participante. */
    public void removerParticipante(int codigo) {
        Participante participante = buscarParticipante(codigo);
        for (Despesa despesa : despesas) {
            if (despesa.envolve(codigo)) {
                throw new IllegalArgumentException(participante.getNome()
                        + " participa de \"" + despesa.getDescricao() + "\" e não pode ser excluído.");
            }
        }
        participantes.remove(codigo);
    }

    public Participante buscarParticipante(int codigo) {
        Participante participante = participantes.get(codigo);
        if (participante == null) {
            throw new IllegalArgumentException("Participante de código " + codigo + " não encontrado.");
        }
        return participante;
    }

    /** Cópia para leitura, em ordem crescente de código (revisão 14: List.copyOf). */
    public List<Participante> getParticipantes() {
        return List.copyOf(participantes.values());
    }

    // ===================== Despesas =====================

    public void cadastrarDespesaFixa(String descricao, int valorTotal, Collection<Integer> codigos) {
        adicionar(new DespesaFixa(descricao, valorTotal, codigos));
    }

    public void cadastrarDespesaPorPessoa(String descricao, int valorUnitario, Collection<Integer> codigos) {
        adicionar(new DespesaPorPessoa(descricao, valorUnitario, codigos));
    }

    /** Editar = criar a nova versão (validada) e trocar pela antiga na mesma posição. */
    public void editarDespesaFixa(int indice, String descricao, int valorTotal, Collection<Integer> codigos) {
        substituir(indice, new DespesaFixa(descricao, valorTotal, codigos));
    }

    public void editarDespesaPorPessoa(int indice, String descricao, int valorUnitario, Collection<Integer> codigos) {
        substituir(indice, new DespesaPorPessoa(descricao, valorUnitario, codigos));
    }

    public void removerDespesa(int indice) {
        validarIndice(indice);
        despesas.remove(indice);
    }

    public List<Despesa> getDespesas() {
        return List.copyOf(despesas);
    }

    // O add só acontece depois que o construtor e os vínculos foram validados.
    private void adicionar(Despesa despesa) {
        validarVinculos(despesa);
        despesas.add(despesa);
    }

    private void substituir(int indice, Despesa nova) {
        validarIndice(indice);
        validarVinculos(nova);
        despesas.set(indice, nova);
    }

    private void validarVinculos(Despesa despesa) {
        for (int codigo : despesa.getCodigosParticipantes()) {
            if (!participantes.containsKey(codigo)) {
                throw new IllegalArgumentException("O código " + codigo + " não está cadastrado.");
            }
        }
    }

    private void validarIndice(int indice) {
        if (indice < 0 || indice >= despesas.size()) {
            throw new IllegalArgumentException("Selecione uma despesa da lista.");
        }
    }

    // ===================== Resumo =====================
    // Os totais são calculados na hora, então refletem toda edição ou exclusão.

    /** Custo total do planejamento (long para somar vários int, como na revisão 14). */
    public long calcularTotal() {
        long soma = 0;
        for (Despesa despesa : despesas) {
            soma = soma + despesa.calcularTotal();   // polimorfismo
        }
        return soma;
    }

    /** Total individual: soma das cotas do participante em cada despesa. */
    public int calcularTotalParticipante(int codigo) {
        buscarParticipante(codigo);
        int soma = 0;
        for (Despesa despesa : despesas) {
            soma = soma + despesa.calcularCota(codigo);   // polimorfismo
        }
        return soma;
    }

    public boolean cabeNoOrcamento(int codigo) {
        return buscarParticipante(codigo).cabeNoOrcamento(calcularTotalParticipante(codigo));
    }

    public int calcularExcesso(int codigo) {
        return buscarParticipante(codigo).calcularExcesso(calcularTotalParticipante(codigo));
    }
}
