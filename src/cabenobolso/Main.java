package cabenobolso;

import cabenobolso.controller.ControlePlanejamento;
import cabenobolso.model.Planejamento;
import cabenobolso.view.TelaPrincipal;

import javax.swing.SwingUtilities;

/**
 * Ponto de entrada (revisão 14): cria modelo, visão e controlador e abre a janela.
 * invokeLater executa a criação da tela na thread de eventos do Swing.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Planejamento planejamento = new Planejamento();
            TelaPrincipal tela = new TelaPrincipal();
            ControlePlanejamento controle = new ControlePlanejamento(planejamento, tela);
            controle.iniciar();
        });
    }
}
