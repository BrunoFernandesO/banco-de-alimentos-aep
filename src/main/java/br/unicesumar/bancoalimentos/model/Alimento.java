package br.unicesumar.bancoalimentos.model;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Item recebido em doação e mantido em estoque: entidade central do sistema (RF02),
 * sobre a qual se aplica o CRUD completo.
 *
 * <p>Classe abstrata: todo alimento é, concretamente, um {@link AlimentoPerecivel} ou um
 * {@link AlimentoNaoPerecivel}. Aqui ficam o estado e as regras comuns aos dois tipos
 * (nome, saldo em estoque, data de recebimento, doador de origem e a baixa de estoque).</p>
 *
 * <p>Regras garantidas pelo encapsulamento: nome obrigatório; quantidade nunca negativa,
 * arredondada a duas casas (coluna {@code DECIMAL(10,2)}); data de recebimento
 * obrigatória e não futura; doador obrigatório e imutável.</p>
 */
public abstract class Alimento {

    /** Maior valor aceito pela coluna {@code alimentos.quantidade DECIMAL(10,2)}. */
    public static final double QUANTIDADE_MAXIMA = 99_999_999.99;

    private int id;
    private String nome;
    private double quantidade;
    private LocalDate dataRecebimento;
    private final Doador doador;

    /**
     * @param id              0 para um alimento novo; o id do banco para um já gravado
     * @param nome            descrição do item (ex.: "Arroz tipo 1 (pacote 5kg)")
     * @param quantidade      saldo em estoque, maior ou igual a zero
     * @param dataRecebimento data de entrada no estoque, não futura
     * @param doador          doador de origem
     */
    protected Alimento(int id, String nome, double quantidade, LocalDate dataRecebimento, Doador doador) {
        if (id < 0) {
            throw new RegraNegocioException("Identificador de alimento invalido: " + id + ".");
        }
        if (doador == null) {
            throw new RegraNegocioException("Informe o doador do alimento.");
        }
        this.id = id;
        this.doador = doador;
        setNome(nome);
        setQuantidade(quantidade);
        setDataRecebimento(dataRecebimento);
    }

    /** Tipo concreto, usado pelo DAO para gravar a coluna discriminadora {@code tipo}. */
    public abstract TipoAlimento getTipo();

    /**
     * Data de validade, quando o tipo de alimento possui uma. A implementação comum
     * retorna vazio; {@link AlimentoPerecivel} a sobrescreve.
     */
    public Optional<LocalDate> consultarValidade() {
        return Optional.empty();
    }

    /**
     * Dá baixa de uma quantidade no saldo em estoque (usada ao registrar uma distribuição).
     *
     * @param qtd quantidade a retirar, maior que zero
     * @throws RegraNegocioException se a quantidade não for positiva ou superar o saldo
     */
    public void darBaixa(double qtd) {
        if (!(qtd > 0)) {
            throw new RegraNegocioException("A quantidade a retirar deve ser maior que zero.");
        }
        double retirada = arredondar(qtd);
        if (retirada > quantidade) {
            throw new RegraNegocioException("Quantidade solicitada (" + Formatos.quantidade(retirada)
                    + ") maior que o saldo em estoque de '" + nome + "' (" + Formatos.quantidade(quantidade) + ").");
        }
        quantidade = arredondar(quantidade - retirada);
    }

    public int getId() {
        return id;
    }

    /**
     * Define o id gerado pelo banco após a inserção. Só pode ser chamado uma vez.
     *
     * @throws RegraNegocioException se o alimento já possuir id ou o valor não for positivo
     */
    public void setId(int id) {
        if (this.id != 0 || id <= 0) {
            throw new RegraNegocioException("Identificador de alimento invalido ou ja definido.");
        }
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public final void setNome(String nome) {
        this.nome = Validacao.textoObrigatorio(nome, "nome do alimento");
    }

    /** Saldo atual em estoque. */
    public double getQuantidade() {
        return quantidade;
    }

    /**
     * Ajusta o saldo em estoque (cadastro ou correção de inventário).
     *
     * @throws RegraNegocioException se o valor for negativo, inválido ou acima do limite da coluna
     */
    public final void setQuantidade(double quantidade) {
        if (Double.isNaN(quantidade) || quantidade < 0) {
            throw new RegraNegocioException("A quantidade nao pode ser negativa.");
        }
        if (quantidade > QUANTIDADE_MAXIMA) {
            throw new RegraNegocioException("A quantidade excede o limite de " + Formatos.quantidade(QUANTIDADE_MAXIMA) + ".");
        }
        this.quantidade = arredondar(quantidade);
    }

    public LocalDate getDataRecebimento() {
        return dataRecebimento;
    }

    /**
     * Altera a data de recebimento.
     *
     * @throws RegraNegocioException se a data for nula ou futura
     */
    public void setDataRecebimento(LocalDate dataRecebimento) {
        if (dataRecebimento == null) {
            throw new RegraNegocioException("Informe a data de recebimento.");
        }
        if (dataRecebimento.isAfter(LocalDate.now())) {
            throw new RegraNegocioException("A data de recebimento nao pode ser futura.");
        }
        this.dataRecebimento = dataRecebimento;
    }

    public Doador getDoador() {
        return doador;
    }

    /** Arredonda para duas casas decimais, como a coluna {@code DECIMAL(10,2)}. */
    private static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
