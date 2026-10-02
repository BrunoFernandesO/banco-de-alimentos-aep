package br.unicesumar.bancoalimentos.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pessoa física, mercado ou indústria que realiza doações (RF01).
 *
 * <p>Compõe a coleção dos alimentos que doou (composição 1:N do diagrama de classes):
 * cada {@link Alimento} nasce vinculado a exatamente um doador e só pode ser adicionado
 * à lista desse mesmo doador. A lista é preenchida pela camada de serviço a partir do
 * banco e exposta somente para leitura por {@link #listarAlimentos()}.</p>
 */
public class Doador {

    private int id;
    private String nome;
    private TipoDoador tipo;
    private String contato;
    private final List<Alimento> alimentos = new ArrayList<>();

    /** Novo doador, ainda não gravado no banco (id 0). */
    public Doador(String nome, TipoDoador tipo, String contato) {
        this(0, nome, tipo, contato);
    }

    /** Doador já existente, reconstruído a partir do banco. */
    public Doador(int id, String nome, TipoDoador tipo, String contato) {
        if (id < 0) {
            throw new RegraNegocioException("Identificador de doador invalido: " + id + ".");
        }
        this.id = id;
        setNome(nome);
        setTipo(tipo);
        setContato(contato);
    }

    /**
     * Inclui um alimento doado por este doador na coleção.
     *
     * @throws RegraNegocioException se o alimento estiver vinculado a outro doador
     */
    public void adicionarAlimento(Alimento alimento) {
        if (alimento == null || alimento.getDoador() != this) {
            throw new RegraNegocioException("O alimento informado nao pertence ao doador '" + nome + "'.");
        }
        if (!alimentos.contains(alimento)) {
            alimentos.add(alimento);
        }
    }

    /** Alimentos doados (somente leitura). */
    public List<Alimento> listarAlimentos() {
        return Collections.unmodifiableList(alimentos);
    }

    public int getId() {
        return id;
    }

    /**
     * Define o id gerado pelo banco após a inserção. Só pode ser chamado uma vez.
     *
     * @throws RegraNegocioException se o doador já possuir id ou o valor não for positivo
     */
    public void setId(int id) {
        if (this.id != 0 || id <= 0) {
            throw new RegraNegocioException("Identificador de doador invalido ou ja definido.");
        }
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Validacao.textoObrigatorio(nome, "nome do doador");
    }

    public TipoDoador getTipo() {
        return tipo;
    }

    public void setTipo(TipoDoador tipo) {
        if (tipo == null) {
            throw new RegraNegocioException("Informe o tipo do doador.");
        }
        this.tipo = tipo;
    }

    /** Contato (telefone ou e-mail); pode ser {@code null}. */
    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = Validacao.textoOpcional(contato, "contato");
    }
}
