package br.unicesumar.bancoalimentos.dao;

/**
 * Falha de acesso ao banco de dados, com mensagem legível para o operador.
 *
 * <p>Os DAOs convertem {@link java.sql.SQLException} (exceção verificada e técnica)
 * nesta exceção não verificada, para que as camadas de serviço e de console não
 * dependam da API JDBC. A causa original é preservada em {@link #getCause()}.</p>
 */
public class PersistenciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
