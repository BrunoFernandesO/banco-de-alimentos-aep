package br.unicesumar.bancoalimentos.service;

import br.unicesumar.bancoalimentos.dao.AlimentoDAO;
import br.unicesumar.bancoalimentos.dao.BeneficiarioDAO;
import br.unicesumar.bancoalimentos.dao.DistribuicaoDAO;
import br.unicesumar.bancoalimentos.dao.PersistenciaException;
import br.unicesumar.bancoalimentos.dao.conexao.ConexaoBanco;
import br.unicesumar.bancoalimentos.model.Alimento;
import br.unicesumar.bancoalimentos.model.Beneficiario;
import br.unicesumar.bancoalimentos.model.Distribuicao;
import br.unicesumar.bancoalimentos.model.RegraNegocioException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Registro de distribuições com baixa automática de estoque (RF05).
 *
 * <p>{@link #registrar} realiza a operação {@code Distribuicao.registrar()} do diagrama de
 * classes. Como altera duas tabelas ({@code distribuicoes} e {@code alimentos}), executa
 * tudo em <b>uma única transação</b> sobre a conexão compartilhada: ou a distribuição é
 * gravada e o saldo é reduzido, ou nada muda no banco.</p>
 */
public class DistribuicaoService {

    private final AlimentoDAO alimentoDAO;
    private final BeneficiarioDAO beneficiarioDAO;
    private final DistribuicaoDAO distribuicaoDAO;

    public DistribuicaoService() {
        this(new AlimentoDAO(), new BeneficiarioDAO());
    }

    public DistribuicaoService(AlimentoDAO alimentoDAO, BeneficiarioDAO beneficiarioDAO) {
        this.alimentoDAO = alimentoDAO;
        this.beneficiarioDAO = beneficiarioDAO;
        this.distribuicaoDAO = new DistribuicaoDAO(alimentoDAO);
    }

    /**
     * Entrega {@code quantidade} do alimento ao beneficiário, com a data de hoje.
     *
     * <p>Passos, na mesma transação: (1) carrega o alimento e o beneficiário; (2) recusa
     * alimento vencido; (3) valida o saldo e dá a baixa no objeto ({@link Alimento#darBaixa});
     * (4) grava a distribuição; (5) grava o novo saldo; (6) confirma. Qualquer falha desfaz
     * a transação, e o modo de confirmação automática é sempre restaurado ao final.</p>
     *
     * @return a distribuição gravada; seu alimento já reflete o saldo restante
     * @throws RegraNegocioException se o alimento estiver vencido, o saldo for insuficiente
     *                               ou algum dos registros não existir
     * @throws PersistenciaException se houver falha de acesso ao banco
     */
    public Distribuicao registrar(int alimentoId, int beneficiarioId, double quantidade) {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try {
            conexao.setAutoCommit(false);

            Alimento alimento = alimentoDAO.buscarPorId(alimentoId)
                    .orElseThrow(() -> new RegraNegocioException("Alimento " + alimentoId + " nao encontrado."));
            Beneficiario beneficiario = beneficiarioDAO.buscarPorId(beneficiarioId)
                    .orElseThrow(() -> new RegraNegocioException("Beneficiario " + beneficiarioId + " nao encontrado."));
            if (alimento.estaVencido()) {
                throw new RegraNegocioException("O alimento '" + alimento.getNome()
                        + "' esta vencido e nao pode ser distribuido.");
            }
            alimento.darBaixa(quantidade);

            Distribuicao distribuicao = new Distribuicao(LocalDate.now(), quantidade, alimento, beneficiario);
            distribuicaoDAO.inserir(distribuicao);
            alimentoDAO.atualizarQuantidade(alimento);

            conexao.commit();
            return distribuicao;
        } catch (SQLException e) {
            desfazer(conexao);
            throw new PersistenciaException("Falha na transacao da distribuicao; nenhuma alteracao foi gravada.", e);
        } catch (RuntimeException e) {
            desfazer(conexao);
            throw e;
        } finally {
            restaurarConfirmacaoAutomatica(conexao);
        }
    }

    /** Histórico de distribuições, da mais recente para a mais antiga. */
    public List<Distribuicao> listar() {
        return distribuicaoDAO.listar();
    }

    private static void desfazer(Connection conexao) {
        try {
            conexao.rollback();
        } catch (SQLException e) {
            // A conexão caiu: o MySQL descarta a transação não confirmada.
        }
    }

    private static void restaurarConfirmacaoAutomatica(Connection conexao) {
        try {
            conexao.setAutoCommit(true);
        } catch (SQLException e) {
            // Conexão inutilizável; ConexaoBanco abrirá uma nova no próximo uso.
        }
    }
}
