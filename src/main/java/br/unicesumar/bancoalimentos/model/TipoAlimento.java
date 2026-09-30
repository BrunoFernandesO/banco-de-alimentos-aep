package br.unicesumar.bancoalimentos.model;

/**
 * Discriminador da hierarquia {@link Alimento}, gravado na coluna {@code alimentos.tipo}.
 *
 * <p>Cada subclasse concreta informa o seu tipo por {@link Alimento#getTipo()}; o DAO usa
 * esse valor apenas para gravar a coluna e, na leitura, para decidir qual subclasse
 * instanciar. Nenhuma regra de negócio é decidida a partir dele.</p>
 */
public enum TipoAlimento {

    PERECIVEL("Perecivel"),
    NAO_PERECIVEL("Nao perecivel");

    /** Rótulo exibido no console (sem acentos). */
    private final String descricao;

    TipoAlimento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
