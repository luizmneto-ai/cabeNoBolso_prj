package cabenobolso.model;

import java.util.Collection;

/**
 * Despesa cobrada por pessoa (revisões 05, 08 e 09): cada selecionado paga o
 * valor unitário; quem não participa paga zero.
 */
public class DespesaPorPessoa extends Despesa {
    private static final long serialVersionUID = 1L;

    private final int valorUnitario;

    public DespesaPorPessoa(String descricao, int valorUnitario, Collection<Integer> codigosParticipantes) {
        super(descricao, codigosParticipantes);
        if (valorUnitario <= 0) {
            throw new IllegalArgumentException("O valor da despesa deve ser positivo.");
        }
        this.valorUnitario = valorUnitario;
    }

    @Override
    public String getTipo() {
        return "Por pessoa";
    }

    @Override
    public int getValorInformado() {
        return valorUnitario;
    }

    /** O total cresce com a quantidade de pessoas. */
    @Override
    public int calcularTotal(int quantidadeParticipantes) {
        return valorUnitario * quantidadeParticipantes;
    }

    @Override
    public int calcularCota(int codigoParticipante) {
        if (envolve(codigoParticipante)) {
            return valorUnitario;
        }
        return 0;
    }
}
