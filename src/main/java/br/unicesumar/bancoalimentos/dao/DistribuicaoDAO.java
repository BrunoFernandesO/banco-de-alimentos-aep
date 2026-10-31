package br.unicesumar.bancoalimentos.dao;

import br.unicesumar.bancoalimentos.dao.conexao.ConexaoBanco;
import br.unicesumar.bancoalimentos.model.Alimento;
import br.unicesumar.bancoalimentos.model.Beneficiario;
import br.unicesumar.bancoalimentos.model.Distribuicao;
import br.unicesumar.bancoalimentos.model.RegraNegocioException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Acesso à tabela {@code distribuicoes} (RF05).
 *
 * <p>A gravação de uma distribuição faz parte de uma transação controlada pela camada de
 * serviço (inserção + baixa de estoque); por isso este DAO usa a conexão compartilhada e
 * não confirma nem desfaz nada por conta própria.</p>
 */
public class DistribuicaoDAO {

    private static final String SQL_INSERIR =
            "INSERT INTO distribuicoes (alimento_id, beneficiario_id, data, quantidade) VALUES (?, ?, ?, ?)";
    private static final String SQL_LISTAR =
            "SELECT di.id, di.alimento_id, di.data, di.quantidade, "
            + "b.id AS beneficiario_id, b.nome AS beneficiario_nome, b.contato AS beneficiario_contato "
            + "FROM distribuicoes di JOIN beneficiarios b ON b.id = di.beneficiario_id "
            + "ORDER BY di.data DESC, di.id DESC";

    private final AlimentoDAO alimentoDAO;

    public DistribuicaoDAO() {
        this(new AlimentoDAO());
    }

    public DistribuicaoDAO(AlimentoDAO alimentoDAO) {
        this.alimentoDAO = alimentoDAO;
    }

    /** Grava a distribuição e atribui a ela o id gerado pelo banco. */
    public void inserir(Distribuicao distribuicao) {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            comando.setInt(1, distribuicao.getAlimento().getId());
            comando.setInt(2, distribuicao.getBeneficiario().getId());
            comando.setObject(3, distribuicao.getData());
            comando.setDouble(4, distribuicao.getQuantidade());
            comando.executeUpdate();
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new PersistenciaException("O banco nao retornou o id da distribuicao registrada.");
                }
                distribuicao.setId(chaves.getInt(1));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel registrar a distribuicao.", e);
        }
    }

    /**
     * Histórico de distribuições, da mais recente para a mais antiga. Os alimentos são
     * obtidos pelo {@link AlimentoDAO}, único responsável por instanciar a subclasse correta.
     */
    public List<Distribuicao> listar() {
        Map<Integer, Alimento> alimentos = alimentoDAO.listar().stream()
                .collect(Collectors.toMap(Alimento::getId, Function.identity()));
        Map<Integer, Beneficiario> beneficiarios = new HashMap<>();
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR);
             ResultSet resultado = comando.executeQuery()) {
            List<Distribuicao> distribuicoes = new ArrayList<>();
            while (resultado.next()) {
                distribuicoes.add(mapear(resultado, alimentos, beneficiarios));
            }
            return distribuicoes;
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel listar as distribuicoes.", e);
        }
    }

    private Distribuicao mapear(ResultSet resultado, Map<Integer, Alimento> alimentos,
                                Map<Integer, Beneficiario> beneficiarios) throws SQLException {
        int id = resultado.getInt("id");
        try {
            Alimento alimento = alimentos.get(resultado.getInt("alimento_id"));
            int beneficiarioId = resultado.getInt("beneficiario_id");
            Beneficiario beneficiario = beneficiarios.get(beneficiarioId);
            if (beneficiario == null) {
                beneficiario = new Beneficiario(beneficiarioId, resultado.getString("beneficiario_nome"),
                        resultado.getString("beneficiario_contato"));
                beneficiarios.put(beneficiarioId, beneficiario);
            }
            return new Distribuicao(id, resultado.getObject("data", LocalDate.class),
                    resultado.getDouble("quantidade"), alimento, beneficiario);
        } catch (RegraNegocioException e) {
            throw new PersistenciaException("Registro invalido na tabela distribuicoes (id " + id + "): " + e.getMessage(), e);
        }
    }
}
