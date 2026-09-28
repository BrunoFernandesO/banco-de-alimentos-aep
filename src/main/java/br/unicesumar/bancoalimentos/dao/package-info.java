/**
 * Camada de acesso a dados (DAO): concentra todo o SQL e a comunicação JDBC com o MySQL.
 * Nenhuma outra camada escreve SQL.
 *
 * <p>A infraestrutura de conexão está em {@code dao.conexao}. Os DAOs das entidades
 * (Doador, Alimento, Beneficiario, Distribuicao) são criados a partir da Sprint 2.</p>
 *
 * <h2>Convenções para os DAOs</h2>
 * <ul>
 *   <li><b>Conexão:</b> obter com {@code ConexaoBanco.getInstancia().getConexao()} e
 *       <b>não</b> fechá-la; fechar apenas {@code PreparedStatement} e {@code ResultSet}
 *       com try-with-resources.</li>
 *   <li><b>Parâmetros:</b> sempre {@code PreparedStatement} com {@code ?}; nunca concatenar
 *       valores digitados pelo usuário no texto do SQL.</li>
 *   <li><b>Chave gerada:</b> após um INSERT, obter o id com
 *       {@code prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)} e
 *       {@code getGeneratedKeys()}.</li>
 *   <li><b>Datas (coluna {@code DATE}, tipo Java {@code LocalDate}):</b> gravar com
 *       {@code setObject(i, data)} e ler com {@code rs.getObject("coluna", LocalDate.class)};
 *       para {@code data_validade} nula (não perecível), usar {@code setNull(i, Types.DATE)}.</li>
 *   <li><b>Tipos (coluna {@code VARCHAR}, enum Java):</b> gravar {@code tipo.name()} e ler
 *       {@code Enum.valueOf(...)}; os valores válidos estão comentados em
 *       {@code database/schema.sql}.</li>
 *   <li><b>Quantidades ({@code DECIMAL}):</b> {@code setDouble}/{@code getDouble}, conforme
 *       o tipo {@code double} do diagrama de classes.</li>
 *   <li><b>Erros:</b> converter {@code SQLException} em {@link PersistenciaException} com
 *       mensagem compreensível para o operador, preservando a causa.</li>
 *   <li><b>Transações:</b> operações que alteram mais de uma tabela (ex.: registrar a
 *       distribuição e dar baixa no estoque, RF05) usam {@code setAutoCommit(false)},
 *       {@code commit()} / {@code rollback()} e, ao final, {@code setAutoCommit(true)}.</li>
 * </ul>
 */
package br.unicesumar.bancoalimentos.dao;
