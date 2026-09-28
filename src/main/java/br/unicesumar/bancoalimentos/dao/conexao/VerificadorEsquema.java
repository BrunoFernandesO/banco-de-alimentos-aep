package br.unicesumar.bancoalimentos.dao.conexao;

import br.unicesumar.bancoalimentos.dao.PersistenciaException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Confere, na inicialização, se o banco conectado possui as tabelas criadas por
 * {@code database/schema.sql}, para que um banco incompleto seja apontado logo de
 * início, e não no meio de uma operação.
 */
public final class VerificadorEsquema {

    /** Tabelas definidas em database/schema.sql (e no DER). */
    public static final List<String> TABELAS_ESPERADAS =
            List.of("doadores", "alimentos", "beneficiarios", "distribuicoes");

    private final Connection conexao;

    public VerificadorEsquema(Connection conexao) {
        this.conexao = conexao;
    }

    /** Nome e versão do servidor e o banco em uso, ex.: {@code MySQL 8.4.6 (banco banco_alimentos)}. */
    public String descreverServidor() {
        try {
            DatabaseMetaData metadados = conexao.getMetaData();
            return metadados.getDatabaseProductName() + " " + metadados.getDatabaseProductVersion()
                    + " (banco " + conexao.getCatalog() + ")";
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel consultar os dados do servidor MySQL.", e);
        }
    }

    /** Retorna as tabelas esperadas que não existem no banco conectado (lista vazia se estiver completo). */
    public List<String> tabelasAusentes() {
        List<String> ausentes = new ArrayList<>();
        try {
            DatabaseMetaData metadados = conexao.getMetaData();
            for (String tabela : TABELAS_ESPERADAS) {
                try (ResultSet resultado = metadados.getTables(
                        conexao.getCatalog(), null, tabela, new String[] {"TABLE"})) {
                    if (!resultado.next()) {
                        ausentes.add(tabela);
                    }
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel verificar as tabelas do banco.", e);
        }
        return ausentes;
    }
}
