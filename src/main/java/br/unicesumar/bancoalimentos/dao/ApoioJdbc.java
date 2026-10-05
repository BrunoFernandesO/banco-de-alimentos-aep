package br.unicesumar.bancoalimentos.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/** Rotinas de apoio compartilhadas pelos DAOs para preencher parâmetros de {@link PreparedStatement}. */
final class ApoioJdbc {

    private ApoioJdbc() {
    }

    /** Grava um texto opcional: {@code null} vira {@code NULL} na coluna {@code VARCHAR}. */
    static void definirTextoOpcional(PreparedStatement comando, int indice, String valor) throws SQLException {
        if (valor == null) {
            comando.setNull(indice, Types.VARCHAR);
        } else {
            comando.setString(indice, valor);
        }
    }
}
