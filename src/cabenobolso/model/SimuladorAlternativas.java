package cabenobolso.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MODELO — simulador de alternativas (objetivo extra, seção 7 do enunciado).
 *
 * Cada despesa recebe uma prioridade: 0 = essencial (sempre mantida) e um inteiro
 * positivo = opcional. Com até 10 opcionais, testa todas as 2^n combinações
 * (no máximo 1.024) e escolhe a de maior soma de prioridades que caiba no
 * orçamento de todos. Em empate, fica a primeira combinação válida encontrada.
 * O planejamento recebido apenas é lido, nunca alterado.
 */
public class SimuladorAlternativas {

    /**
     * Resultado da simulação. Se viavel for false, as essenciais já estouram algum
     * orçamento: mantidas e retiradas ficam vazias e excessoPorParticipante traz o excesso.
     */
    public record Resultado(
            boolean viavel,
            List<Despesa> mantidas,
            List<Despesa> retiradas,
            Map<Integer, Long> totalPorParticipante,
            Map<Integer, Long> excessoPorParticipante) {
    }

    public Resultado simular(Planejamento planejamento, List<Integer> prioridades) {
        List<Despesa> despesas = planejamento.getDespesas();
        List<Participante> participantes = planejamento.getParticipantes();

        if (prioridades.size() != despesas.size()) {
            throw new IllegalArgumentException("Informe uma prioridade para cada despesa.");
        }

        List<Despesa> essenciais = new ArrayList<>();
        List<Integer> indicesOpcionais = new ArrayList<>();

        for (int i = 0; i < despesas.size(); i++) {
            int prioridade = prioridades.get(i);

            if (prioridade < 0) {
                throw new IllegalArgumentException("A prioridade deve ser zero ou positiva.");
            }

            if (prioridade == 0) {
                essenciais.add(despesas.get(i));
            } else {
                indicesOpcionais.add(i);
            }
        }

        if (indicesOpcionais.size() > 10) {
            throw new IllegalArgumentException("Selecione até 10 despesas opcionais.");
        }

        Map<Integer, Long> totaisEssenciais = calcularTotais(participantes, essenciais);
        Map<Integer, Long> excessos = calcularExcessos(participantes, totaisEssenciais);

        if (!excessos.isEmpty()) {
            return new Resultado(false, List.of(), List.of(), totaisEssenciais, excessos);
        }

        List<Despesa> melhoresMantidas = essenciais;
        Map<Integer, Long> melhoresTotais = totaisEssenciais;
        long melhorPontuacao = 0;
        int quantidadeCombinacoes = 1 << indicesOpcionais.size();

        // Cada bit indica se uma despesa opcional entra na combinação.
        for (int combinacao = 1; combinacao < quantidadeCombinacoes; combinacao++) {
            List<Despesa> mantidas = new ArrayList<>(essenciais);
            long pontuacao = 0;

            for (int posicao = 0; posicao < indicesOpcionais.size(); posicao++) {
                if ((combinacao & (1 << posicao)) != 0) {
                    int indice = indicesOpcionais.get(posicao);
                    mantidas.add(despesas.get(indice));
                    pontuacao += prioridades.get(indice);
                }
            }

            if (pontuacao > melhorPontuacao) {
                Map<Integer, Long> totais = calcularTotais(participantes, mantidas);

                if (calcularExcessos(participantes, totais).isEmpty()) {
                    melhorPontuacao = pontuacao;
                    melhoresMantidas = mantidas;
                    melhoresTotais = totais;
                }
            }
        }

        List<Despesa> retiradas = new ArrayList<>(despesas);
        retiradas.removeAll(melhoresMantidas);

        return new Resultado(
                true, List.copyOf(melhoresMantidas), List.copyOf(retiradas),
                melhoresTotais, Map.of());
    }

    private Map<Integer, Long> calcularTotais(
            List<Participante> participantes, List<Despesa> despesas) {
        Map<Integer, Long> totais = new LinkedHashMap<>();

        for (Participante participante : participantes) {
            int codigo = participante.getCodigo();
            long total = 0;

            for (Despesa despesa : despesas) {
                if (despesa.envolve(codigo)) {
                    total += despesa.calcularCota(codigo);
                }
            }

            totais.put(codigo, total);
        }

        return totais;
    }

    private Map<Integer, Long> calcularExcessos(
            List<Participante> participantes, Map<Integer, Long> totais) {
        Map<Integer, Long> excessos = new LinkedHashMap<>();

        for (Participante participante : participantes) {
            int codigo = participante.getCodigo();
            long excesso = totais.get(codigo) - participante.getOrcamento();

            if (excesso > 0) {
                excessos.put(codigo, excesso);
            }
        }

        return excessos;
    }
}
