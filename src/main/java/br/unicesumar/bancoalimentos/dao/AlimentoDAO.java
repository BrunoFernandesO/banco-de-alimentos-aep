package br.unicesumar.bancoalimentos.dao;

import br.unicesumar.bancoalimentos.dao.conexao.ConexaoBanco;
import br.unicesumar.bancoalimentos.model.Alimento;
import br.unicesumar.bancoalimentos.model.AlimentoNaoPerecivel;
import br.unicesumar.bancoalimentos.model.AlimentoPerecivel;
import br.unicesumar.bancoalimentos.model.Doador;
import br.unicesumar.bancoalimentos.model.RegraNegocioException;
import br.unicesumar.bancoalimentos.model.TipoAlimento;
import br.unicesumar.bancoalimentos.model.TipoDoador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Acesso à tabela {@code alimentos} (RF02 e CRUD de Alimento).
 *
 * <p>A tabela guarda toda a hierarquia {@link Alimento} (tabela única com a coluna
 * discriminadora {@code tipo}). Este DAO é o <b>único</b> ponto que decide qual subclasse
 * instanciar a partir dessa coluna; as demais camadas trabalham apenas com o tipo
 * abstrato {@link Alimento} e seus métodos polimórficos.</p>
 */
public class AlimentoDAO {

    /** Código de erro do MySQL: exclusão bloqueada por chave estrangeira (linha referenciada). */
    private static final int ERRO_LINHA_REFERENCIADA = 1451;

    private static final String SQL_INSERIR =
            "INSERT INTO alimentos (nome, tipo, quantidade, data_recebimento, data_validade, doador_id) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    /** Colunas do alimento e do doador de origem (JOIN), lidas por {@link #mapear}. */
    private static final String SQL_SELECIONAR =
            "SELECT a.id, a.nome, a.tipo, a.quantidade, a.data_recebimento, a.data_validade, a.doador_id, "
            + "d.nome AS doador_nome, d.tipo AS doador_tipo, d.contato AS doador_contato "
            + "FROM alimentos a JOIN doadores d ON d.id = a.doador_id";

    private static final String SQL_LISTAR = SQL_SELECIONAR + " ORDER BY a.id";
    private static final String SQL_BUSCAR_POR_ID = SQL_SELECIONAR + " WHERE a.id = ?";
    private static final String SQL_LISTAR_POR_DOADOR = SQL_SELECIONAR + " WHERE a.doador_id = ? ORDER BY a.data_recebimento, a.id";
    private static final String SQL_LISTAR_COM_SALDO = SQL_SELECIONAR + " WHERE a.quantidade > 0 ORDER BY a.id";
    private static final String SQL_LISTAR_COM_VALIDADE_ENTRE = SQL_SELECIONAR
            + " WHERE a.data_validade BETWEEN ? AND ? AND a.quantidade > 0 ORDER BY a.data_validade, a.id";

    /** O tipo e o doador não são alteráveis: definem a natureza e a origem da doação. */
    private static final String SQL_ATUALIZAR =
            "UPDATE alimentos SET nome = ?, quantidade = ?, data_recebimento = ?, data_validade = ? WHERE id = ?";
    private static final String SQL_ATUALIZAR_QUANTIDADE = "UPDATE alimentos SET quantidade = ? WHERE id = ?";
    private static final String SQL_EXCLUIR = "DELETE FROM alimentos WHERE id = ?";
    private static final String SQL_CONTAR_DISTRIBUICOES =
            "SELECT COUNT(*) FROM distribuicoes WHERE alimento_id = ?";

    /** Grava um novo alimento e atribui a ele o id gerado pelo banco. */
    public void inserir(Alimento alimento) {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, alimento.getNome());
            comando.setString(2, alimento.getTipo().name());
            comando.setDouble(3, alimento.getQuantidade());
            comando.setObject(4, alimento.getDataRecebimento());
            definirValidade(comando, 5, alimento);
            comando.setInt(6, alimento.getDoador().getId());
            comando.executeUpdate();
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (!chaves.next()) {
                    throw new PersistenciaException("O banco nao retornou o id do alimento cadastrado.");
                }
                alimento.setId(chaves.getInt(1));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel registrar o alimento '" + alimento.getNome() + "'.", e);
        }
    }

    /** Todos os alimentos cadastrados (inclusive sem saldo e vencidos), por ordem de id. */
    public List<Alimento> listar() {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR)) {
            return executarConsulta(comando, null);
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel listar os alimentos.", e);
        }
    }

    /** Alimento com o id informado, se existir. */
    public Optional<Alimento> buscarPorId(int id) {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR_POR_ID)) {
            comando.setInt(1, id);
            List<Alimento> encontrados = executarConsulta(comando, null);
            return encontrados.isEmpty() ? Optional.empty() : Optional.of(encontrados.get(0));
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel consultar o alimento " + id + ".", e);
        }
    }

    /**
     * Alimentos doados pelo doador informado. Os objetos retornados referenciam a própria
     * instância recebida, o que permite adicioná-los à coleção do doador (composição).
     */
    public List<Alimento> listarPorDoador(Doador doador) {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR_POR_DOADOR)) {
            comando.setInt(1, doador.getId());
            return executarConsulta(comando, doador);
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel listar os alimentos do doador " + doador.getId() + ".", e);
        }
    }

    /**
     * Grava as alterações de nome, quantidade e datas de um alimento existente.
     *
     * @throws PersistenciaException se o alimento não existir mais no banco
     */
    public void atualizar(Alimento alimento) {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZAR)) {
            comando.setString(1, alimento.getNome());
            comando.setDouble(2, alimento.getQuantidade());
            comando.setObject(3, alimento.getDataRecebimento());
            definirValidade(comando, 4, alimento);
            comando.setInt(5, alimento.getId());
            if (comando.executeUpdate() == 0) {
                throw new PersistenciaException("Alimento " + alimento.getId() + " nao encontrado para atualizacao.");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel atualizar o alimento " + alimento.getId() + ".", e);
        }
    }

    /**
     * Grava somente o saldo em estoque (baixa por distribuição, RF05). Executado dentro da
     * transação aberta pela camada de serviço, sobre a conexão compartilhada.
     */
    public void atualizarQuantidade(Alimento alimento) {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZAR_QUANTIDADE)) {
            comando.setDouble(1, alimento.getQuantidade());
            comando.setInt(2, alimento.getId());
            if (comando.executeUpdate() == 0) {
                throw new PersistenciaException("Alimento " + alimento.getId() + " nao encontrado para baixa de estoque.");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel atualizar o saldo do alimento " + alimento.getId() + ".", e);
        }
    }

    /**
     * Exclui o alimento. A chave estrangeira de {@code distribuicoes} (sem {@code ON DELETE
     * CASCADE}) impede a exclusão de itens já distribuídos; a camada de serviço verifica
     * isso antes, e este tratamento é a última barreira.
     *
     * @return {@code true} se havia um alimento com o id informado
     */
    public boolean excluir(int id) {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_EXCLUIR)) {
            comando.setInt(1, id);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == ERRO_LINHA_REFERENCIADA) {
                throw new PersistenciaException("O alimento " + id
                        + " possui distribuicoes registradas e nao pode ser excluido.", e);
            }
            throw new PersistenciaException("Nao foi possivel excluir o alimento " + id + ".", e);
        }
    }

    /** Quantidade de distribuições registradas para o alimento. */
    public int contarDistribuicoes(int alimentoId) {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_CONTAR_DISTRIBUICOES)) {
            comando.setInt(1, alimentoId);
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel consultar as distribuicoes do alimento " + alimentoId + ".", e);
        }
    }

    /**
     * Alimentos com saldo em estoque (quantidade maior que zero), vencidos ou não. A
     * ordenação por prioridade é feita no domínio, por meio do método polimórfico
     * {@link Alimento#calcularPrioridade()}.
     */
    public List<Alimento> listarComSaldo() {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR_COM_SALDO)) {
            return executarConsulta(comando, null);
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel consultar o estoque.", e);
        }
    }

    /**
     * Alimentos com saldo cuja validade está entre as datas informadas, inclusive (RF06).
     * Itens sem validade ({@code data_validade} nula) nunca são retornados.
     */
    public List<Alimento> listarComValidadeEntre(LocalDate inicio, LocalDate fim) {
        Connection conexao = ConexaoBanco.getInstancia().getConexao();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR_COM_VALIDADE_ENTRE)) {
            comando.setObject(1, inicio);
            comando.setObject(2, fim);
            return executarConsulta(comando, null);
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel consultar os alimentos perto do vencimento.", e);
        }
    }

    /** Grava a data de validade, ou {@code NULL} quando o tipo de alimento não possui uma. */
    private static void definirValidade(PreparedStatement comando, int indice, Alimento alimento) throws SQLException {
        Optional<LocalDate> validade = alimento.consultarValidade();
        if (validade.isPresent()) {
            comando.setObject(indice, validade.get());
        } else {
            comando.setNull(indice, Types.DATE);
        }
    }

    /**
     * Executa a consulta e converte cada linha em {@link Alimento}. Linhas do mesmo doador
     * compartilham a mesma instância de {@link Doador}.
     *
     * @param doadorConhecido instância a reutilizar para todas as linhas, ou {@code null}
     */
    private List<Alimento> executarConsulta(PreparedStatement comando, Doador doadorConhecido) throws SQLException {
        List<Alimento> alimentos = new ArrayList<>();
        Map<Integer, Doador> doadores = new HashMap<>();
        if (doadorConhecido != null) {
            doadores.put(doadorConhecido.getId(), doadorConhecido);
        }
        try (ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) {
                alimentos.add(mapear(resultado, doadores));
            }
        }
        return alimentos;
    }

    /**
     * Constrói a subclasse correta a partir da coluna {@code tipo}. É a única decisão
     * baseada no tipo em todo o sistema, e serve apenas para criar o objeto.
     */
    private Alimento mapear(ResultSet resultado, Map<Integer, Doador> doadores) throws SQLException {
        int id = resultado.getInt("id");
        try {
            Doador doador = obterDoador(resultado, doadores);
            String nome = resultado.getString("nome");
            double quantidade = resultado.getDouble("quantidade");
            LocalDate dataRecebimento = resultado.getObject("data_recebimento", LocalDate.class);
            TipoAlimento tipo = TipoAlimento.valueOf(resultado.getString("tipo"));
            return switch (tipo) {
                case PERECIVEL -> new AlimentoPerecivel(id, nome, quantidade, dataRecebimento,
                        resultado.getObject("data_validade", LocalDate.class), doador);
                case NAO_PERECIVEL -> new AlimentoNaoPerecivel(id, nome, quantidade, dataRecebimento, doador);
            };
        } catch (IllegalArgumentException | RegraNegocioException e) {
            throw new PersistenciaException("Registro invalido na tabela alimentos (id " + id + "): " + e.getMessage(), e);
        }
    }

    private Doador obterDoador(ResultSet resultado, Map<Integer, Doador> doadores) throws SQLException {
        int doadorId = resultado.getInt("doador_id");
        Doador doador = doadores.get(doadorId);
        if (doador == null) {
            doador = new Doador(doadorId, resultado.getString("doador_nome"),
                    TipoDoador.valueOf(resultado.getString("doador_tipo")), resultado.getString("doador_contato"));
            doadores.put(doadorId, doador);
        }
        return doador;
    }
}
