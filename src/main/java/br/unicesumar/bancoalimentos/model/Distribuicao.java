package br.unicesumar.bancoalimentos.model;

import java.time.LocalDate;

/**
 * Entrega de uma quantidade de um alimento a um beneficiário (RF05).
 *
 * <p>É um registro histórico: depois de criado, não muda (todos os campos, exceto o id
 * gerado pelo banco, são imutáveis). A baixa correspondente no estoque é feita por
 * {@link Alimento#darBaixa(double)}, na mesma transação em que a distribuição é gravada.</p>
 */
public class Distribuicao {

    private int id;
    private final LocalDate data;
    private final double quantidade;
    private final Alimento alimento;
    private final Beneficiario beneficiario;

    /** Nova distribuição, ainda não gravada no banco. */
    public Distribuicao(LocalDate data, double quantidade, Alimento alimento, Beneficiario beneficiario) {
        this(0, data, quantidade, alimento, beneficiario);
    }

    /**
     * Distribuição já existente, reconstruída a partir do banco.
     *
     * @throws RegraNegocioException se algum dado for inválido
     */
    public Distribuicao(int id, LocalDate data, double quantidade, Alimento alimento, Beneficiario beneficiario) {
        if (id < 0) {
            throw new RegraNegocioException("Identificador de distribuicao invalido: " + id + ".");
        }
        if (data == null) {
            throw new RegraNegocioException("Informe a data da distribuicao.");
        }
        if (data.isAfter(LocalDate.now())) {
            throw new RegraNegocioException("A data da distribuicao nao pode ser futura.");
        }
        if (!(quantidade > 0) || Double.isInfinite(quantidade)) {
            throw new RegraNegocioException("A quantidade distribuida deve ser maior que zero.");
        }
        if (alimento == null) {
            throw new RegraNegocioException("Informe o alimento distribuido.");
        }
        if (beneficiario == null) {
            throw new RegraNegocioException("Informe o beneficiario.");
        }
        this.id = id;
        this.data = data;
        this.quantidade = Math.round(quantidade * 100.0) / 100.0;
        this.alimento = alimento;
        this.beneficiario = beneficiario;
    }

    public int getId() {
        return id;
    }

    /**
     * Define o id gerado pelo banco após a inserção. Só pode ser chamado uma vez.
     *
     * @throws RegraNegocioException se a distribuição já possuir id ou o valor não for positivo
     */
    public void setId(int id) {
        if (this.id != 0 || id <= 0) {
            throw new RegraNegocioException("Identificador de distribuicao invalido ou ja definido.");
        }
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public Alimento getAlimento() {
        return alimento;
    }

    public Beneficiario getBeneficiario() {
        return beneficiario;
    }
}
