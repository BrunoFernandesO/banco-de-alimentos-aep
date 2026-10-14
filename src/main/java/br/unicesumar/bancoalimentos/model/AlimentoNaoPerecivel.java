package br.unicesumar.bancoalimentos.model;

import java.time.LocalDate;

/**
 * Alimento sem data de validade relevante para a distribuição (grãos, enlatados,
 * massas, óleo...). No banco, a coluna {@code data_validade} fica nula.
 *
 * <p>Regra própria do tipo: prioridade baixa, que cresce lentamente com o tempo em estoque
 * (o que entrou primeiro sai primeiro, FIFO), limitada a um teto inferior à faixa dos
 * perecíveis.</p>
 */
public class AlimentoNaoPerecivel extends Alimento {

    /** Prioridade no dia da entrada. */
    static final double PRIORIDADE_BASE = 10.0;
    /** Acréscimo de prioridade por dia em estoque. */
    static final double INCREMENTO_POR_DIA = 0.1;
    /** Teto da prioridade (atingido com 300 dias em estoque); fica abaixo do piso dos perecíveis (50). */
    static final double PRIORIDADE_TETO = 40.0;

    /** Novo alimento não perecível, ainda não gravado no banco. */
    public AlimentoNaoPerecivel(String nome, double quantidade, LocalDate dataRecebimento, Doador doador) {
        this(0, nome, quantidade, dataRecebimento, doador);
    }

    /** Alimento não perecível já existente, reconstruído a partir do banco. */
    public AlimentoNaoPerecivel(int id, String nome, double quantidade, LocalDate dataRecebimento, Doador doador) {
        super(id, nome, quantidade, dataRecebimento, doador);
    }

    /**
     * Prioridade baixa e crescente com o tempo em estoque (FIFO):
     *
     * <pre>
     *   prioridade = min(10 + 0,1 x diasEmEstoque, 40)
     * </pre>
     *
     * <p>Entrada hoje: 10; após 30 dias: 13; após 300 dias ou mais: 40. Entre dois itens
     * não perecíveis, o mais antigo sai primeiro; qualquer perecível (prioridade acima de
     * 50) continua à frente.</p>
     */
    @Override
    public double calcularPrioridade() {
        return Math.min(PRIORIDADE_BASE + INCREMENTO_POR_DIA * diasEmEstoque(), PRIORIDADE_TETO);
    }

    @Override
    public TipoAlimento getTipo() {
        return TipoAlimento.NAO_PERECIVEL;
    }
}
