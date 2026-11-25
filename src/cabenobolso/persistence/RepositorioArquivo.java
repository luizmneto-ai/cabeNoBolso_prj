package cabenobolso.persistence;

import cabenobolso.model.Planejamento;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Implementação do contrato com serialização de objetos (revisão 15).
 *
 * O Planejamento é gravado inteiro: participantes, despesas e os códigos que
 * ligam cada despesa aos participantes. Ao carregar, as relações voltam iguais.
 */
public class RepositorioArquivo implements RepositorioPlanejamento {
    private final Path arquivo;

    public RepositorioArquivo(Path arquivo) {
        this.arquivo = arquivo.toAbsolutePath();
    }

    public Path getArquivo() {
        return arquivo;
    }

    @Override
    public void salvar(Planejamento planejamento) throws IOException {
        if (planejamento == null) {
            throw new IllegalArgumentException("Informe o planejamento.");
        }

        // Grava primeiro em um arquivo temporário na mesma pasta; depois troca
        // pelo definitivo em uma única operação, sem estragar a versão anterior.
        Path temporario = Files.createTempFile(arquivo.getParent(), "planejamento-", ".tmp");
        try {
            try (OutputStream fluxo = Files.newOutputStream(temporario);
                    ObjectOutputStream saida = new ObjectOutputStream(fluxo)) {
                saida.writeObject(planejamento);
            }
            Files.move(temporario, arquivo, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temporario);
        }
    }

    @Override
    public Planejamento carregar() throws IOException {
        try (InputStream fluxo = Files.newInputStream(arquivo);
                ObjectInputStream entrada = new ObjectInputStream(fluxo)) {
            Object objeto = entrada.readObject();
            if (objeto instanceof Planejamento plano) {
                return plano;
            }
            throw new IOException("O arquivo não contém um planejamento.");
        } catch (ClassNotFoundException erro) {
            throw new IOException("Uma classe do planejamento não está disponível.", erro);
        }
    }
}
