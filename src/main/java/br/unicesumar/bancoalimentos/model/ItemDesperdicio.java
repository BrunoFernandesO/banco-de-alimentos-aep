package br.unicesumar.bancoalimentos.model;

/**
 * Linha do relatório de desperdício (RF07): um alimento vencido que ainda tinha saldo em
 * estoque, junto com o quanto dele chegou a ser distribuído antes de vencer.
 *
 * @param alimento              alimento vencido; seu saldo atual é a quantidade perdida
 * @param quantidadeDistribuida soma das distribuições já registradas para o alimento
 */
public record ItemDesperdicio(Alimento alimento, double quantidadeDistribuida) {

    public ItemDesperdicio {
        if (alimento == null) {
            throw new RegraNegocioException("Informe o alimento do item de desperdicio.");
        }
        if (quantidadeDistribuida < 0) {
            throw new RegraNegocioException("A quantidade distribuida nao pode ser negativa.");
        }
    }

    /** Saldo que venceu sem ser entregue. */
    public double quantidadePerdida() {
        return alimento.getQuantidade();
    }

    /** {@code true} se o item nunca teve nenhuma distribuição registrada. */
    public boolean nuncaDistribuido() {
        return quantidadeDistribuida == 0;
    }
}
