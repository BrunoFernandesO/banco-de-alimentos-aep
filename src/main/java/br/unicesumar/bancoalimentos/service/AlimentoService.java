package br.unicesumar.bancoalimentos.service;

import br.unicesumar.bancoalimentos.dao.AlimentoDAO;
import br.unicesumar.bancoalimentos.model.Alimento;
import br.unicesumar.bancoalimentos.model.RegraNegocioException;

import java.util.Comparator;
import java.util.List;

/** Casos de uso do estoque de alimentos (RF02, RF03 e CRUD de Alimento). */
public class AlimentoService {

    /**
     * Ordem de distribuição (RF03): maior {@link Alimento#calcularPrioridade()} primeiro.
     * A chamada é polimórfica: cada subclasse aplica a sua regra, sem testes de tipo aqui.
     * Em caso de empate, sai primeiro o que foi recebido antes e, por fim, o de menor id.
     */
    static final Comparator<Alimento> POR_PRIORIDADE =
            Comparator.comparingDouble(Alimento::calcularPrioridade).reversed()
                    .thenComparing(Alimento::getDataRecebimento)
                    .thenComparingInt(Alimento::getId);

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

    /**
     * Estoque disponível para distribuição (RF03): itens com saldo maior que zero e não
     * vencidos, do mais prioritário para o menos prioritário.
     */
    public List<Alimento> listarPorPrioridade() {
        return alimentoDAO.listarComSaldo().stream()
                .filter(alimento -> !alimento.estaVencido())
                .sorted(POR_PRIORIDADE)
                .toList();
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
