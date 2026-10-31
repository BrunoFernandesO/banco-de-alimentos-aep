package br.unicesumar.bancoalimentos.dao;

import br.unicesumar.bancoalimentos.dao.conexao.ConexaoBanco;
import br.unicesumar.bancoalimentos.model.Beneficiario;
import br.unicesumar.bancoalimentos.model.RegraNegocioException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso à tabela {@code beneficiarios} (RF04). */
public class BeneficiarioDAO {

    private static final String SQL_INSERIR = "INSERT INTO beneficiarios (nome, contato) VALUES (?, ?)";
    private static final String SQL_SELECIONAR = "SELECT id, nome, contato FROM beneficiarios";
    private static final String SQL_LISTAR = SQL_SELECIONAR + " ORDER BY nome, id";
    private static final String SQL_BUSCAR_POR_ID = SQL_SELECIONAR + " WHERE id = ?";

    /** Grava um novo beneficiário e atribui a ele o id gerado pelo banco. */
    public void inserir(Beneficiario beneficiario) {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, beneficiario.getNome());
            ApoioJdbc.definirTextoOpcional(comando, 2, beneficiario.getContato());
            comando.executeUpdate();
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new PersistenciaException("O banco nao retornou o id do beneficiario cadastrado.");
                }
                beneficiario.setId(chaves.getInt(1));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel cadastrar o beneficiario.", e);
        }
    }

    /** Todos os beneficiários, em ordem alfabética. */
    public List<Beneficiario> listar() {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR);
             ResultSet resultado = comando.executeQuery()) {
            List<Beneficiario> beneficiarios = new ArrayList<>();
            while (resultado.next()) {
                beneficiarios.add(mapear(resultado));
            }
            return beneficiarios;
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel listar os beneficiarios.", e);
        }
    }

    /** Beneficiário com o id informado, se existir. */
    public Optional<Beneficiario> buscarPorId(int id) {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR_POR_ID)) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel consultar o beneficiario " + id + ".", e);
        }
    }

    private Beneficiario mapear(ResultSet resultado) throws SQLException {
        int id = resultado.getInt("id");
        try {
            return new Beneficiario(id, resultado.getString("nome"), resultado.getString("contato"));
        } catch (RegraNegocioException e) {
            throw new PersistenciaException("Registro invalido na tabela beneficiarios (id " + id + "): " + e.getMessage(), e);
        }
    }
}
