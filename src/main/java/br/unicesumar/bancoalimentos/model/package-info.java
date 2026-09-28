/**
 * Camada de domínio (model): classes de negócio do banco de alimentos, conforme o
 * diagrama de classes ({@code docs/diagrama_classes.png}).
 *
 * <ul>
 *   <li>{@code Alimento} (abstrata), especializada por {@code AlimentoPerecivel} e
 *       {@code AlimentoNaoPerecivel}: herança;</li>
 *   <li>{@code calcularPrioridade()} abstrato em {@code Alimento} e sobrescrito
 *       ({@code @Override}) em cada subclasse: polimorfismo que ordena o estoque (RF03);</li>
 *   <li>{@code Doador} 1:N {@code Alimento}; {@code Alimento} 1:N {@code Distribuicao};
 *       {@code Beneficiario} 1:N {@code Distribuicao}.</li>
 * </ul>
 *
 * <p>Não conhece JDBC nem SQL. Implementada a partir da Sprint 2.</p>
 */
package br.unicesumar.bancoalimentos.model;
