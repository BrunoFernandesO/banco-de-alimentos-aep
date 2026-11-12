package br.unicesumar.bancoalimentos.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Relatório de desperdício (RF07): alimentos vencidos que não foram distribuídos por
 * completo, com os totais que medem a perda (meta 12.3 do ODS 12).
 *
 * <p>Considera-se desperdiçado o saldo que restava em estoque quando o item venceu. Itens
 * que nunca tiveram nenhuma distribuição são contados à parte, por
 * {@link #contarNuncaDistribuidos()}.</p>
 *
 * @param dataReferencia data em que o relatório foi gerado
 * @param itens          itens vencidos com saldo, do vencimento mais antigo ao mais recente
 */
public record RelatorioDesperdicio(LocalDate dataReferencia, List<ItemDesperdicio> itens) {

    public RelatorioDesperdicio {
        itens = List.copyOf(itens);
    }

    /** Soma dos saldos vencidos (as unidades são as de cada item, indicadas no nome). */
    public double quantidadeTotalPerdida() {
        return itens.stream().mapToDouble(ItemDesperdicio::quantidadePerdida).sum();
    }

    /** Soma do que esses mesmos itens chegaram a distribuir antes de vencer. */
    public double quantidadeTotalDistribuida() {
        return itens.stream().mapToDouble(ItemDesperdicio::quantidadeDistribuida).sum();
    }

    /** Itens vencidos que nunca tiveram nenhuma distribuição. */
    public long contarNuncaDistribuidos() {
        return itens.stream().filter(ItemDesperdicio::nuncaDistribuido).count();
    }

    /**
     * Percentual distribuído do total recebido desses itens (distribuído ÷ (distribuído +
     * perdido) × 100); zero quando não há itens.
     */
    public double percentualAproveitado() {
        double total = quantidadeTotalDistribuida() + quantidadeTotalPerdida();
        return total == 0 ? 0 : quantidadeTotalDistribuida() / total * 100.0;
    }
}
