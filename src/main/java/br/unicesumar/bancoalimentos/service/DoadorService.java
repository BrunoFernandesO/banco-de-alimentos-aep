package br.unicesumar.bancoalimentos.service;

import br.unicesumar.bancoalimentos.dao.AlimentoDAO;
import br.unicesumar.bancoalimentos.dao.DoadorDAO;
import br.unicesumar.bancoalimentos.model.Doador;
import br.unicesumar.bancoalimentos.model.RegraNegocioException;
import br.unicesumar.bancoalimentos.model.TipoDoador;

import java.util.List;

/**
 * Casos de uso de doadores (RF01).
 *
 * <p>{@link #cadastrar} realiza a operação {@code Doador.cadastrar()} do diagrama de classes:
 * a persistência fica nesta camada (e no DAO) para que o domínio não dependa de JDBC.</p>
 */
public class DoadorService {

    private final DoadorDAO doadorDAO;
    private final AlimentoDAO alimentoDAO;

    public DoadorService() {
        this(new DoadorDAO(), new AlimentoDAO());
    }

    public DoadorService(DoadorDAO doadorDAO, AlimentoDAO alimentoDAO) {
        this.doadorDAO = doadorDAO;
        this.alimentoDAO = alimentoDAO;
    }

    /** Valida e grava um novo doador (RF01), retornando-o já com o id gerado. */
    public Doador cadastrar(String nome, TipoDoador tipo, String contato) {
        Doador doador = new Doador(nome, tipo, contato);
        doadorDAO.inserir(doador);
        return doador;
    }

    public List<Doador> listar() {
        return doadorDAO.listar();
    }

    /** @throws RegraNegocioException se não houver doador com o id informado */
    public Doador buscarPorId(int id) {
        return doadorDAO.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException("Doador " + id + " nao encontrado."));
    }

    /** Doador com a coleção de alimentos doados preenchida (composição 1:N). */
    public Doador buscarComAlimentos(int id) {
        Doador doador = buscarPorId(id);
        alimentoDAO.listarPorDoador(doador).forEach(doador::adicionarAlimento);
        return doador;
    }
}
