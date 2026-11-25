package cabenobolso.model;

import java.io.Serializable;

/**
 * Participante do passeio: código único, nome e orçamento em centavos.
 *
 * Base: revisões 05 (classe e construtor), 06 (encapsulamento, alterarOrcamento,
 * cabeNoOrcamento, calcularExcesso) e 12 (validação com IllegalArgumentException).
 * Acréscimo do projeto: o código, que identifica o participante no Map do Planejamento.
 */
public class Participante implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int codigo;      // não muda depois do cadastro
    private String nome;
    private int orcamento;         // centavos: 7000 = R$ 70,00

    public Participante(int codigo, String nome, int orcamento) {
        if (codigo <= 0) {
            throw new IllegalArgumentException("O código deve ser um inteiro positivo.");
        }
        validarNome(nome);
        validarOrcamento(orcamento);
        this.codigo = codigo;
        this.nome = nome.trim();
        this.orcamento = orcamento;
    }

    // Validações reaproveitadas pelo construtor e pelos métodos de alteração.
    private static void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe o nome do participante.");
        }
    }

    private static void validarOrcamento(int orcamento) {
        if (orcamento < 0) {
            throw new IllegalArgumentException("O orçamento deve ser maior ou igual a zero.");
        }
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public int getOrcamento() {
        return orcamento;
    }

    /** Valida antes de atribuir: se o nome for inválido, o anterior é mantido. */
    public void alterarNome(String novoNome) {
        validarNome(novoNome);
        this.nome = novoNome.trim();
    }

    /** Valida antes de atribuir: orçamento negativo é recusado e o anterior é mantido. */
    public void alterarOrcamento(int novoOrcamento) {
        validarOrcamento(novoOrcamento);
        this.orcamento = novoOrcamento;
    }

    /** Custo igual ao orçamento ainda está dentro do limite (revisão 06). */
    public boolean cabeNoOrcamento(int custo) {
        return custo <= orcamento;
    }

    /** Quanto o custo passa do orçamento; zero quando está dentro (revisão 06). */
    public int calcularExcesso(int custo) {
        if (cabeNoOrcamento(custo)) {
            return 0;
        }
        return custo - orcamento;
    }

    @Override
    public String toString() {
        return codigo + " - " + nome;
    }
}
