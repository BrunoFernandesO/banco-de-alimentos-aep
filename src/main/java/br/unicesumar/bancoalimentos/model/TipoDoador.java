package br.unicesumar.bancoalimentos.model;

/**
 * Categorias de doador previstas no RF01.
 *
 * <p>Gravado na coluna {@code doadores.tipo} pelo nome da constante
 * ({@link #name()}) e lido de volta com {@link #valueOf(String)}.</p>
 */
public enum TipoDoador {

    PESSOA_FISICA("Pessoa fisica"),
    MERCADO("Mercado"),
    INDUSTRIA("Industria");

    /** Rótulo exibido no console (sem acentos, por compatibilidade com o terminal do Windows). */
    private final String descricao;

    TipoDoador(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
