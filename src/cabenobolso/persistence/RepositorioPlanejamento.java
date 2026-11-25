package cabenobolso.persistence;

import cabenobolso.model.Planejamento;

import java.io.IOException;

/**
 * Contrato de persistência (revisões 10 e 15).
 * O controlador usa este tipo, sem depender da classe que grava em arquivo.
 */
public interface RepositorioPlanejamento {
    void salvar(Planejamento planejamento) throws IOException;

    Planejamento carregar() throws IOException;
}
