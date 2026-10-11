package br.unicesumar.bancoalimentos.view;

/**
 * Sinaliza que a entrada padrão terminou (Ctrl+Z/Ctrl+D ou fim de um arquivo redirecionado),
 * para que o sistema seja encerrado normalmente em vez de repetir a leitura sem fim.
 */
public class EntradaEncerradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EntradaEncerradaException() {
        super("Entrada encerrada.");
    }
}
