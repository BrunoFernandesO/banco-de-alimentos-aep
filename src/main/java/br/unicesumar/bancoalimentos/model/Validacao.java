package br.unicesumar.bancoalimentos.model;

/**
 * Validações de campos de texto comuns às entidades do domínio, alinhadas aos limites
 * das colunas {@code VARCHAR(120)} de {@code database/schema.sql}.
 */
final class Validacao {

    /** Tamanho das colunas de texto (nome, contato) no banco. */
    static final int TAMANHO_MAXIMO_TEXTO = 120;

    private Validacao() {
    }

    /** Retorna o texto sem espaços nas pontas; rejeita valor vazio ou longo demais. */
    static String textoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("O campo " + campo + " e obrigatorio.");
        }
        return limitarTamanho(valor.trim(), campo);
    }

    /** Como {@link #textoObrigatorio}, mas aceita valor vazio, que é armazenado como {@code null}. */
    static String textoOpcional(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return limitarTamanho(valor.trim(), campo);
    }

    private static String limitarTamanho(String valor, String campo) {
        if (valor.length() > TAMANHO_MAXIMO_TEXTO) {
            throw new RegraNegocioException("O campo " + campo + " aceita no maximo "
                    + TAMANHO_MAXIMO_TEXTO + " caracteres.");
        }
        return valor;
    }
}
