package cabenobolso.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Base abstrata das despesas (revisões 08, 09, 12 e 15).
 *
 * O que é comum fica aqui: descrição e participantes envolvidos.
 * O que muda é calculado pelas subclasses (sobrescrita):
 *  - calcularTotal(quantidade): mesmo método da trilha;
 *  - calcularCota(codigo): quanto UM participante paga. É o acréscimo do projeto
 *    para a participação seletiva e a distribuição dos centavos.
 *
 * Os participantes são guardados pelos CÓDIGOS (como o Set<Integer> da revisão 11).
 * Usamos TreeSet para manter a ordem crescente de código, exigida na regra dos centavos.
 */
public abstract class Despesa implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String descricao;
    private final TreeSet<Integer> codigosParticipantes;

    public Despesa(String descricao, Collection<Integer> codigosParticipantes) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Informe a descrição da despesa.");
        }
        if (codigosParticipantes == null || codigosParticipantes.isEmpty()) {
            throw new IllegalArgumentException("Selecione pelo menos um participante.");
        }
        TreeSet<Integer> codigos = new TreeSet<>(codigosParticipantes);
        if (codigos.size() != codigosParticipantes.size()) {
            throw new IllegalArgumentException("Um participante foi selecionado mais de uma vez.");
        }
        this.descricao = descricao.trim();
        this.codigosParticipantes = codigos;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Somente leitura, em ordem crescente de código. */
    public Set<Integer> getCodigosParticipantes() {
        return Collections.unmodifiableSet(codigosParticipantes);
    }

    public int getQuantidadeParticipantes() {
        return codigosParticipantes.size();
    }

    public boolean envolve(int codigo) {
        return codigosParticipantes.contains(codigo);
    }

    /** Custo total desta despesa para os participantes selecionados. */
    public int calcularTotal() {
        return calcularTotal(getQuantidadeParticipantes());
    }

    /** "Fixa" ou "Por pessoa", para a tela e o resumo. */
    public abstract String getTipo();

    /** Valor digitado no cadastro: total da fixa ou valor unitário da por pessoa. */
    public abstract int getValorInformado();

    /** Total da despesa para uma quantidade de participantes (revisões 08 e 09). */
    public abstract int calcularTotal(int quantidadeParticipantes);

    /** Quanto o participante deste código paga nesta despesa; zero se não participa. */
    public abstract int calcularCota(int codigoParticipante);
}
