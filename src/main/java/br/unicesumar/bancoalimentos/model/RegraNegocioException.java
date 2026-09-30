package br.unicesumar.bancoalimentos.model;

/**
 * Violação de uma regra de negócio (dado inválido ou operação não permitida), com
 * mensagem pronta para ser exibida ao operador no console.
 *
 * <p>Distingue-se de {@code PersistenciaException}, que indica falha técnica de acesso
 * ao banco: aqui o banco está acessível, mas a operação solicitada não é válida
 * (ex.: quantidade negativa, baixa maior que o saldo, alimento vencido).</p>
 */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
