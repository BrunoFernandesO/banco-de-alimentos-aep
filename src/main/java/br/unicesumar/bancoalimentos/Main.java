package br.unicesumar.bancoalimentos;

import br.unicesumar.bancoalimentos.dao.PersistenciaException;
import br.unicesumar.bancoalimentos.dao.conexao.ConexaoBanco;
import br.unicesumar.bancoalimentos.dao.conexao.VerificadorEsquema;

import java.sql.Connection;
import java.util.List;

/**
 * Ponto de entrada do Sistema de Gestão de Banco de Alimentos.
 *
 * <p>Sprint 1: conecta ao MySQL e confere a estrutura do banco. Nas próximas
 * sprints, a inicialização exibirá os alertas de vencimento (RF06) e abrirá o
 * menu principal da camada {@code view}.</p>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        exibirCabecalho();
        ConexaoBanco banco = null;
        try {
            banco = ConexaoBanco.getInstancia();
            if (bancoPronto(banco.getConexao())) {
                System.out.println("Base pronta. O menu principal sera disponibilizado nas proximas sprints.");
            }
        } catch (PersistenciaException e) {
            System.err.println("ERRO: " + e.getMessage());
        } finally {
            if (banco != null) {
                banco.fechar();
            }
        }
    }

    private static void exibirCabecalho() {
        System.out.println("==================================================");
        System.out.println("  SISTEMA DE GESTAO DE BANCO DE ALIMENTOS");
        System.out.println("  AEP 2026.2 - Engenharia de Software - UniCesumar");
        System.out.println("==================================================");
    }

    /** Mostra o servidor conectado e confirma que todas as tabelas de schema.sql existem. */
    private static boolean bancoPronto(Connection conexao) {
        VerificadorEsquema verificador = new VerificadorEsquema(conexao);
        System.out.println("Conectado: " + verificador.descreverServidor());

        List<String> ausentes = verificador.tabelasAusentes();
        if (!ausentes.isEmpty()) {
            System.err.println("ERRO: o banco nao possui as tabelas " + String.join(", ", ausentes)
                    + ". Execute o script database/schema.sql.");
            return false;
        }
        System.out.println("Tabelas verificadas: " + String.join(", ", VerificadorEsquema.TABELAS_ESPERADAS) + ".");
        return true;
    }
}
