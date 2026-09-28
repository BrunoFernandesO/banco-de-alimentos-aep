/**
 * Camada de serviço: regras de negócio que coordenam o domínio e os DAOs, por exemplo
 * registrar uma distribuição com baixa automática de estoque (RF05), identificar os
 * alimentos perto do vencimento (RF06) e montar o relatório de desperdício (RF07).
 *
 * <p>Não escreve SQL (usa os DAOs) nem lê do teclado (tarefa da camada {@code view}).
 * Implementada a partir da Sprint 2.</p>
 */
package br.unicesumar.bancoalimentos.service;
