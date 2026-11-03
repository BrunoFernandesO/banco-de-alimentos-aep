package br.unicesumar.bancoalimentos.service;

import br.unicesumar.bancoalimentos.dao.BeneficiarioDAO;
import br.unicesumar.bancoalimentos.model.Beneficiario;
import br.unicesumar.bancoalimentos.model.RegraNegocioException;

import java.util.List;

/**
 * Casos de uso de beneficiários (RF04).
 *
 * <p>{@link #cadastrar} realiza a operação {@code Beneficiario.cadastrar()} do diagrama de
 * classes, com a persistência nesta camada (ver {@link DoadorService}).</p>
 */
public class BeneficiarioService {

    private final BeneficiarioDAO beneficiarioDAO;

    public BeneficiarioService() {
        this(new BeneficiarioDAO());
    }

    public BeneficiarioService(BeneficiarioDAO beneficiarioDAO) {
        this.beneficiarioDAO = beneficiarioDAO;
    }

    /** Valida e grava um novo beneficiário (RF04), retornando-o já com o id gerado. */
    public Beneficiario cadastrar(String nome, String contato) {
        Beneficiario beneficiario = new Beneficiario(nome, contato);
        beneficiarioDAO.inserir(beneficiario);
        return beneficiario;
    }

    public List<Beneficiario> listar() {
        return beneficiarioDAO.listar();
    }

    /** @throws RegraNegocioException se não houver beneficiário com o id informado */
    public Beneficiario buscarPorId(int id) {
        return beneficiarioDAO.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException("Beneficiario " + id + " nao encontrado."));
    }
}
