package cabenobolso.model;

import java.util.Collection;

/**
 * Despesa de valor fixo, dividido entre os selecionados (revisões 08 e 12).
 *
 * Regra dos centavos do enunciado: 10000 / 3 = 3333 e sobra 1 centavo.
 * A sobra é distribuída, 1 centavo por pessoa, na ordem crescente dos códigos:
 * código 1 -> 3334; códigos 2 e 3 -> 3333. A soma volta a ser 10000.
 */
public class DespesaFixa extends Despesa {
    private static final long serialVersionUID = 1L;

    private final int valorTotal;

    public DespesaFixa(String descricao, int valorTotal, Collection<Integer> codigosParticipantes) {
        super(descricao, codigosParticipantes);
        if (valorTotal <= 0) {
            throw new IllegalArgumentException("O valor da despesa deve ser positivo.");
        }
        this.valorTotal = valorTotal;
    }

    @Override
    public String getTipo() {
        return "Fixa";
    }

    @Override
    public int getValorInformado() {
        return valorTotal;
    }

    /** O total não depende da quantidade de pessoas. */
    @Override
    public int calcularTotal(int quantidadeParticipantes) {
        return valorTotal;
    }

    @Override
    public int calcularCota(int codigoParticipante) {
        if (!envolve(codigoParticipante)) {
            return 0;
        }
        int quantidade = getQuantidadeParticipantes();
        int cotaBase = valorTotal / quantidade;   // divisão inteira
        int sobra = valorTotal % quantidade;      // centavos que restaram

        // Posição do participante na ordem crescente dos códigos: 0, 1, 2...
        int posicao = 0;
        for (int codigo : getCodigosParticipantes()) {
            if (codigo == codigoParticipante) {
                break;
            }
            posicao++;
        }
        // As primeiras 'sobra' posições recebem 1 centavo a mais.
        if (posicao < sobra) {
            return cotaBase + 1;
        }
        return cotaBase;
    }
}
