package br.unicesumar.bancoalimentos.view;

import br.unicesumar.bancoalimentos.dao.PersistenciaException;
import br.unicesumar.bancoalimentos.model.RegraNegocioException;

import java.util.List;

/**
 * Esqueleto comum dos menus do console (padrão Template Method): exibe as opções numeradas,
 * lê a escolha e executa a ação correspondente, repetindo até o operador escolher 0.
 *
 * <p>Erros de uma ação são mostrados ao operador e o menu continua em execução: uma regra
 * de negócio violada ou uma falha do banco nunca derrubam o sistema.</p>
 */
public abstract class MenuConsole {

    protected final Entrada entrada;

    protected MenuConsole(Entrada entrada) {
        this.entrada = entrada;
    }

    /** Título exibido acima das opções. */
    protected abstract String getTitulo();

    /** Rótulos das opções, numeradas a partir de 1 na ordem da lista. */
    protected abstract List<String> getOpcoes();

    /** Executa a opção escolhida (de 1 ao tamanho de {@link #getOpcoes()}). */
    protected abstract void executarOpcao(int opcao);

    /** Rótulo da opção 0. */
    protected String getRotuloSaida() {
        return "Voltar";
    }

    /** Laço do menu; retorna quando o operador escolhe a opção 0. */
    public final void executar() {
        while (true) {
            List<String> opcoes = getOpcoes();
            System.out.println();
            System.out.println("===== " + getTitulo() + " =====");
            for (int i = 0; i < opcoes.size(); i++) {
                System.out.printf("%2d - %s%n", i + 1, opcoes.get(i));
            }
            System.out.printf("%2d - %s%n", 0, getRotuloSaida());
            int opcao = entrada.lerInteiro("Opcao", 0, opcoes.size());
            if (opcao == 0) {
                return;
            }
            System.out.println();
            executarComTratamento(opcao);
        }
    }

    private void executarComTratamento(int opcao) {
        try {
            executarOpcao(opcao);
        } catch (RegraNegocioException e) {
            System.out.println("ATENCAO: " + e.getMessage());
        } catch (PersistenciaException e) {
            System.out.println("ERRO: " + e.getMessage());
        } catch (EntradaEncerradaException e) {
            throw e;
        } catch (RuntimeException e) {
            System.out.println("ERRO inesperado: " + e);
        }
    }
}
