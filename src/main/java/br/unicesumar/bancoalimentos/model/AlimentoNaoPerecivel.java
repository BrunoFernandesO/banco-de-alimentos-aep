package br.unicesumar.bancoalimentos.model;

import java.time.LocalDate;

/**
 * Alimento sem data de validade relevante para a distribuição (grãos, enlatados,
 * massas, óleo...). No banco, a coluna {@code data_validade} fica nula.
 */
public class AlimentoNaoPerecivel extends Alimento {

    /** Novo alimento não perecível, ainda não gravado no banco. */
    public AlimentoNaoPerecivel(String nome, double quantidade, LocalDate dataRecebimento, Doador doador) {
        this(0, nome, quantidade, dataRecebimento, doador);
    }

    /** Alimento não perecível já existente, reconstruído a partir do banco. */
    public AlimentoNaoPerecivel(int id, String nome, double quantidade, LocalDate dataRecebimento, Doador doador) {
        super(id, nome, quantidade, dataRecebimento, doador);
    }

    @Override
    public TipoAlimento getTipo() {
        return TipoAlimento.NAO_PERECIVEL;
    }
}
