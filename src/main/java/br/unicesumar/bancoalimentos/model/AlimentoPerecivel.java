package br.unicesumar.bancoalimentos.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Alimento com prazo de validade (leite, pão, frutas, verduras...).
 *
 * <p>Regras próprias do tipo: a data de validade é obrigatória e não pode ser anterior à
 * data de recebimento; a prioridade de distribuição cresce à medida que a validade se
 * aproxima; após a validade, o item está vencido e não pode ser distribuído.</p>
 */
public class AlimentoPerecivel extends Alimento {

    /** Limite inferior (não atingido) da prioridade de um perecível. */
    static final double PRIORIDADE_PISO = 50.0;
    /** Prioridade de um perecível que vence hoje (máxima do sistema). */
    static final double PRIORIDADE_MAXIMA = 100.0;

    private LocalDate dataValidade;

    /** Novo alimento perecível, ainda não gravado no banco. */
    public AlimentoPerecivel(String nome, double quantidade, LocalDate dataRecebimento,
                             LocalDate dataValidade, Doador doador) {
        this(0, nome, quantidade, dataRecebimento, dataValidade, doador);
    }

    /** Alimento perecível já existente, reconstruído a partir do banco. */
    public AlimentoPerecivel(int id, String nome, double quantidade, LocalDate dataRecebimento,
                             LocalDate dataValidade, Doador doador) {
        super(id, nome, quantidade, dataRecebimento, doador);
        setDataValidade(dataValidade);
    }

    @Override
    public TipoAlimento getTipo() {
        return TipoAlimento.PERECIVEL;
    }

    /**
     * Prioridade cresce conforme a validade se aproxima:
     *
     * <pre>
     *   prioridade = 50 + 50 / (diasParaVencer + 1)
     * </pre>
     *
     * <p>Vence hoje (0 dia): 100; amanhã: 75; em 2 dias: 66,7; em 9 dias: 55; em 49 dias: 51.
     * A curva é estritamente decrescente e nunca chega a 50, que é o limite superior
     * reservado aos não perecíveis. Um item vencido recebe a prioridade máxima, mas é
     * excluído da lista de distribuição por {@link #estaVencido()}.</p>
     */
    @Override
    public double calcularPrioridade() {
        long dias = diasParaVencer();
        if (dias <= 0) {
            return PRIORIDADE_MAXIMA;
        }
        return PRIORIDADE_PISO + (PRIORIDADE_MAXIMA - PRIORIDADE_PISO) / (dias + 1);
    }

    /** Vencido a partir do dia seguinte à data de validade (no próprio dia ainda pode ser distribuído). */
    @Override
    public boolean estaVencido() {
        return dataValidade.isBefore(LocalDate.now());
    }

    /** Dias de hoje até a validade: 0 se vence hoje, negativo se já venceu. */
    public long diasParaVencer() {
        return ChronoUnit.DAYS.between(LocalDate.now(), dataValidade);
    }

    @Override
    public Optional<LocalDate> consultarValidade() {
        return Optional.of(dataValidade);
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    /**
     * Altera a data de validade.
     *
     * @throws RegraNegocioException se a data for nula ou anterior à data de recebimento
     */
    public void setDataValidade(LocalDate dataValidade) {
        exigirDatasCoerentes(getDataRecebimento(), dataValidade);
        this.dataValidade = dataValidade;
    }

    /**
     * Altera a data de recebimento, mantendo-a coerente com a validade já informada.
     * Durante a construção a validade ainda é nula, e só a regra comum se aplica.
     */
    @Override
    public void setDataRecebimento(LocalDate dataRecebimento) {
        if (dataValidade != null) {
            exigirDatasCoerentes(dataRecebimento, dataValidade);
        }
        super.setDataRecebimento(dataRecebimento);
    }

    /**
     * Altera recebimento e validade de uma só vez, validando o par (evita rejeitar uma
     * alteração válida por causa da ordem em que as duas datas são trocadas).
     */
    public void alterarDatas(LocalDate dataRecebimento, LocalDate dataValidade) {
        exigirDatasCoerentes(dataRecebimento, dataValidade);
        super.setDataRecebimento(dataRecebimento);
        this.dataValidade = dataValidade;
    }

    private static void exigirDatasCoerentes(LocalDate dataRecebimento, LocalDate dataValidade) {
        if (dataValidade == null) {
            throw new RegraNegocioException("Alimento perecivel exige data de validade.");
        }
        if (dataRecebimento != null && dataValidade.isBefore(dataRecebimento)) {
            throw new RegraNegocioException("A data de validade (" + Formatos.data(dataValidade)
                    + ") nao pode ser anterior a data de recebimento (" + Formatos.data(dataRecebimento) + ").");
        }
    }
}
