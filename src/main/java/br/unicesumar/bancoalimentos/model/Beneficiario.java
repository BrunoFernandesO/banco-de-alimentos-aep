package br.unicesumar.bancoalimentos.model;

/**
 * Pessoa, família ou instituição atendida pelo banco de alimentos (RF04). Um beneficiário
 * recebe várias distribuições ao longo do tempo (agregação 1:N com {@link Distribuicao}).
 */
public class Beneficiario {

    private int id;
    private String nome;
    private String contato;

    /** Novo beneficiário, ainda não gravado no banco (id 0). */
    public Beneficiario(String nome, String contato) {
        this(0, nome, contato);
    }

    /** Beneficiário já existente, reconstruído a partir do banco. */
    public Beneficiario(int id, String nome, String contato) {
        if (id < 0) {
            throw new RegraNegocioException("Identificador de beneficiario invalido: " + id + ".");
        }
        this.id = id;
        setNome(nome);
        setContato(contato);
    }

    public int getId() {
        return id;
    }

    /**
     * Define o id gerado pelo banco após a inserção. Só pode ser chamado uma vez.
     *
     * @throws RegraNegocioException se o beneficiário já possuir id ou o valor não for positivo
     */
    public void setId(int id) {
        if (this.id != 0 || id <= 0) {
            throw new RegraNegocioException("Identificador de beneficiario invalido ou ja definido.");
        }
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Validacao.textoObrigatorio(nome, "nome do beneficiario");
    }

    /** Contato (telefone ou e-mail); pode ser {@code null}. */
    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = Validacao.textoOpcional(contato, "contato");
    }
}
