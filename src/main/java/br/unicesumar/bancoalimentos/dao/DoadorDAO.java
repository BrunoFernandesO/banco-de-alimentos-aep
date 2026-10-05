package br.unicesumar.bancoalimentos.dao;

import br.unicesumar.bancoalimentos.dao.conexao.ConexaoBanco;
import br.unicesumar.bancoalimentos.model.Doador;
import br.unicesumar.bancoalimentos.model.RegraNegocioException;
import br.unicesumar.bancoalimentos.model.TipoDoador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso à tabela {@code doadores} (RF01). */
public class DoadorDAO {

    private static final String SQL_INSERIR =
            "INSERT INTO doadores (nome, tipo, contato) VALUES (?, ?, ?)";
    private static final String SQL_SELECIONAR =
            "SELECT id, nome, tipo, contato FROM doadores";
    private static final String SQL_LISTAR = SQL_SELECIONAR + " ORDER BY nome, id";
    private static final String SQL_BUSCAR_POR_ID = SQL_SELECIONAR + " WHERE id = ?";

    /** Grava um novo doador e atribui a ele o id gerado pelo banco. */
    public void inserir(Doador doador) {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, doador.getNome());
            comando.setString(2, doador.getTipo().name());
            ApoioJdbc.definirTextoOpcional(comando, 3, doador.getContato());
            comando.executeUpdate();
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new PersistenciaException("O banco nao retornou o id do doador cadastrado.");
                }
                doador.setId(chaves.getInt(1));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel cadastrar o doador.", e);
        }
    }

    /** Todos os doadores, em ordem alfabética. */
    public List<Doador> listar() {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR);
             ResultSet resultado = comando.executeQuery()) {
            List<Doador> doadores = new ArrayList<>();
            while (resultado.next()) {
                doadores.add(mapear(resultado));
            }
            return doadores;
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel listar os doadores.", e);
        }
    }

    /** Doador com o id informado, se existir. */
    public Optional<Doador> buscarPorId(int id) {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR_POR_ID)) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel consultar o doador " + id + ".", e);
        }
    }

    private Doador mapear(ResultSet resultado) throws SQLException {
        int id = resultado.getInt("id");
        try {
            return new Doador(id, resultado.getString("nome"),
                    TipoDoador.valueOf(resultado.getString("tipo")), resultado.getString("contato"));
        } catch (IllegalArgumentException | RegraNegocioException e) {
            throw new PersistenciaException("Registro invalido na tabela doadores (id " + id + "): " + e.getMessage(), e);
        }
    }
}
