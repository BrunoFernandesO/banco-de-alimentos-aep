/**
 * Infraestrutura de conexão com o MySQL: leitura de {@code db.properties}
 * ({@link br.unicesumar.bancoalimentos.dao.conexao.ConfiguracaoBanco}), conexão única
 * compartilhada ({@link br.unicesumar.bancoalimentos.dao.conexao.ConexaoBanco}, Singleton)
 * e verificação das tabelas na inicialização
 * ({@link br.unicesumar.bancoalimentos.dao.conexao.VerificadorEsquema}).
 *
 * <p>É o único ponto do código ligado ao SGBD escolhido: trocar de banco exige alterar
 * apenas a URL em {@code db.properties}, o driver aqui e o dialeto de {@code schema.sql}.</p>
 */
package br.unicesumar.bancoalimentos.dao.conexao;
