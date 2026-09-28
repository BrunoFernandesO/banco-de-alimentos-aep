package br.unicesumar.bancoalimentos.dao.conexao;

import br.unicesumar.bancoalimentos.dao.PersistenciaException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Ponto único de acesso à conexão JDBC com o MySQL (padrão Singleton).
 *
 * <p>A aplicação é de console e atende um operador por vez; por isso mantém
 * <b>uma única</b> {@link Connection}, aberta na primeira chamada a
 * {@link #getConexao()} e reaproveitada por todos os DAOs. Se ela cair (MySQL
 * reiniciado, tempo de inatividade esgotado), é reaberta automaticamente na
 * chamada seguinte.</p>
 *
 * <p><b>Regra para os DAOs:</b> obter a conexão com
 * {@code ConexaoBanco.getInstancia().getConexao()} e <b>nunca fechá-la</b>; fechar
 * apenas {@code PreparedStatement} e {@code ResultSet} (try-with-resources). A
 * conexão é encerrada uma única vez, ao sair do sistema, por {@link #fechar()}.</p>
 */
public final class ConexaoBanco {

    private static final String DRIVER_MYSQL = "com.mysql.cj.jdbc.Driver";
    private static final int SEGUNDOS_VALIDACAO = 2;

    /** Código de erro do MySQL: usuário ou senha inválidos. */
    private static final int ERRO_ACESSO_NEGADO = 1045;
    /** Código de erro do MySQL: o banco informado na URL não existe. */
    private static final int ERRO_BANCO_INEXISTENTE = 1049;
    /** Prefixo SQLState (padrão SQL) das falhas de comunicação com o servidor. */
    private static final String SQLSTATE_FALHA_CONEXAO = "08";

    private static ConexaoBanco instancia;

    private final ConfiguracaoBanco configuracao;
    private Connection conexao;

    private ConexaoBanco(ConfiguracaoBanco configuracao) {
        this.configuracao = configuracao;
        carregarDriver();
    }

    /**
     * Retorna a instância única, criando-a no primeiro uso a partir de
     * {@code db.properties}.
     *
     * @throws PersistenciaException se a configuração estiver ausente ou inválida,
     *                               ou se o driver do MySQL não estiver no classpath
     */
    public static synchronized ConexaoBanco getInstancia() {
        if (instancia == null) {
            instancia = new ConexaoBanco(ConfiguracaoBanco.carregar());
        }
        return instancia;
    }

    /**
     * Retorna a conexão ativa, abrindo-a (ou reabrindo-a) se necessário.
     *
     * @throws PersistenciaException com mensagem orientando a correção, se não for
     *                               possível conectar
     */
    public synchronized Connection getConexao() {
        try {
            if (conexao == null || !conexao.isValid(SEGUNDOS_VALIDACAO)) {
                fecharSilenciosamente();
                conexao = DriverManager.getConnection(
                        configuracao.getUrl(), configuracao.getUsuario(), configuracao.getSenha());
            }
            return conexao;
        } catch (SQLException e) {
            throw new PersistenciaException(descreverFalha(e), e);
        }
    }

    /** Encerra a conexão, se aberta. Chamado uma vez, ao sair do sistema. */
    public synchronized void fechar() {
        fecharSilenciosamente();
    }

    private void fecharSilenciosamente() {
        if (conexao == null) {
            return;
        }
        try {
            conexao.close();
        } catch (SQLException e) {
            // A conexão já estava inutilizável; não há o que recuperar.
        } finally {
            conexao = null;
        }
    }

    private void carregarDriver() {
        try {
            Class.forName(DRIVER_MYSQL);
        } catch (ClassNotFoundException e) {
            throw new PersistenciaException("Driver JDBC do MySQL (Connector/J) nao encontrado no classpath. "
                    + "Execute o sistema pelo Maven Wrapper (mvnw) ou pela IDE com o projeto Maven importado.", e);
        }
    }

    /** Traduz as falhas de conexão mais comuns em instruções para quem está executando o sistema. */
    private String descreverFalha(SQLException e) {
        String mensagem = e.getMessage() == null ? "" : e.getMessage();
        String sqlState = e.getSQLState() == null ? "" : e.getSQLState();

        if (e.getErrorCode() == ERRO_ACESSO_NEGADO) {
            return "Acesso negado ao MySQL para o usuario '" + configuracao.getUsuario()
                    + "'. Verifique db.usuario e db.senha em " + ConfiguracaoBanco.NOME_ARQUIVO + ".";
        }
        if (e.getErrorCode() == ERRO_BANCO_INEXISTENTE) {
            return "O banco de dados informado em db.url nao existe. "
                    + "Crie-o executando o script database/schema.sql.";
        }
        if (mensagem.contains("Public Key Retrieval")) {
            return "O MySQL exige a troca da chave publica para autenticar. "
                    + "Acrescente ?allowPublicKeyRetrieval=true ao final de db.url em "
                    + ConfiguracaoBanco.NOME_ARQUIVO + ".";
        }
        if (sqlState.startsWith(SQLSTATE_FALHA_CONEXAO)) {
            return "Nao foi possivel conectar ao MySQL em " + configuracao.getUrl()
                    + ". Verifique se o servico do MySQL esta em execucao e se o endereco e a porta em db.url estao corretos.";
        }
        return "Falha ao conectar ao MySQL: " + mensagem;
    }
}
