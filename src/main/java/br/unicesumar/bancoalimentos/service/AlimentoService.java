package br.unicesumar.bancoalimentos.service;

import br.unicesumar.bancoalimentos.dao.AlimentoDAO;
import br.unicesumar.bancoalimentos.model.Alimento;
import br.unicesumar.bancoalimentos.model.RegraNegocioException;

import java.util.List;

/** Casos de uso do estoque de alimentos (RF02). */
public class AlimentoService {

    private final AlimentoDAO alimentoDAO;

    public AlimentoService() {
        this(new AlimentoDAO());
    }

    public AlimentoService(AlimentoDAO alimentoDAO) {
        this.alimentoDAO = alimentoDAO;
    }

    /**
     * Registra a entrada de um alimento no estoque (RF02). O objeto já chega validado pelo
     * domínio; aqui se exige, além disso, que a entrada tenha quantidade positiva e que o
     * doador já esteja cadastrado.
     */
    public Alimento cadastrar(Alimento alimento) {
        if (alimento.getQuantidade() <= 0) {
            throw new RegraNegocioException("A entrada no estoque deve ter quantidade maior que zero.");
        }
        if (alimento.getDoador().getId() <= 0) {
            throw new RegraNegocioException("O doador precisa estar cadastrado antes de registrar a doacao.");
        }
        alimentoDAO.inserir(alimento);
        return alimento;
    }

    /** Todos os alimentos cadastrados, por ordem de id. */
    public List<Alimento> listar() {
        return alimentoDAO.listar();
    }

    /** @throws RegraNegocioException se não houver alimento com o id informado */
    public Alimento buscarPorId(int id) {
        return alimentoDAO.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException("Alimento " + id + " nao encontrado."));
    }
}
