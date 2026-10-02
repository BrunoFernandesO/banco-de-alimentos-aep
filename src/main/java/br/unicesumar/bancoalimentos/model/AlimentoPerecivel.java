package br.unicesumar.bancoalimentos.model;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Alimento com prazo de validade (leite, pão, frutas, verduras...).
 *
 * <p>Regra própria do tipo: a data de validade é obrigatória e não pode ser anterior à
 * data de recebimento.</p>
 */
public class AlimentoPerecivel extends Alimento {

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
