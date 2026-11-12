package br.unicesumar.bancoalimentos;

import br.unicesumar.bancoalimentos.dao.PersistenciaException;
import br.unicesumar.bancoalimentos.dao.conexao.ConexaoBanco;
import br.unicesumar.bancoalimentos.dao.conexao.VerificadorEsquema;
import br.unicesumar.bancoalimentos.service.RelatorioService;
import br.unicesumar.bancoalimentos.view.Entrada;
import br.unicesumar.bancoalimentos.view.EntradaEncerradaException;
import br.unicesumar.bancoalimentos.view.MenuPrincipal;
import br.unicesumar.bancoalimentos.view.MenuRelatorios;

import java.sql.Connection;
import java.util.List;

/**
 * Ponto de entrada do Sistema de Gestão de Banco de Alimentos.
 *
 * <p>Conecta ao MySQL, confere a estrutura do banco, exibe os alertas de vencimento
 * (RF06) e abre o menu principal da camada {@code view}. Ao sair (opção 0 ou fim da
 * entrada), a conexão é encerrada.</p>
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
                System.out.println();
                MenuRelatorios.exibirAlertasVencimento(new RelatorioService());
                new MenuPrincipal(new Entrada(System.in)).executar();
                System.out.println("Sistema encerrado.");
            }
        } catch (EntradaEncerradaException e) {
            System.out.println();
            System.out.println("Entrada encerrada. Sistema finalizado.");
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
