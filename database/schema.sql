-- Sistema de Gestao de Banco de Alimentos
-- Script de criacao do banco (MySQL 8)
-- AEP 2026.2 - Engenharia de Software - UniCesumar
--
-- Execucao (na raiz do repositorio):
--   mysql -u root -p -e "source database/schema.sql"
--
-- O script pode ser executado mais de uma vez: o banco e as tabelas so sao
-- criados se ainda nao existirem, e nenhum dado e apagado.
-- Para recriar tudo do zero (APAGA os dados): DROP DATABASE banco_alimentos;
-- e execute este script novamente.
--
-- Valores de dominio (colunas "tipo") sao validados na camada Java (enums),
-- nao no banco.

CREATE DATABASE IF NOT EXISTS banco_alimentos;
USE banco_alimentos;

-- Quem realiza as doacoes (RF01).
-- tipo: PESSOA_FISICA | MERCADO | INDUSTRIA  (enum TipoDoador)
CREATE TABLE IF NOT EXISTS doadores (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    nome     VARCHAR(120) NOT NULL,
    tipo     VARCHAR(20) NOT NULL,
    contato  VARCHAR(120)
);

-- Itens em estoque (RF02). Uma unica tabela guarda a hierarquia Alimento:
-- a coluna tipo indica a subclasse Java correspondente.
-- tipo: PERECIVEL (AlimentoPerecivel) | NAO_PERECIVEL (AlimentoNaoPerecivel)
-- data_validade: obrigatoria para PERECIVEL e nula para NAO_PERECIVEL.
-- quantidade: saldo atual em estoque (reduzido a cada distribuicao, RF05).
CREATE TABLE IF NOT EXISTS alimentos (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    nome              VARCHAR(120) NOT NULL,
    tipo              VARCHAR(20) NOT NULL,
    quantidade        DECIMAL(10,2) NOT NULL,
    data_recebimento  DATE NOT NULL,
    data_validade     DATE,
    doador_id         INT NOT NULL,
    FOREIGN KEY (doador_id) REFERENCES doadores(id)
);

-- Pessoas ou familias atendidas (RF04).
CREATE TABLE IF NOT EXISTS beneficiarios (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    nome     VARCHAR(120) NOT NULL,
    contato  VARCHAR(120)
);

-- Cada entrega realizada (RF05). As chaves estrangeiras nao usam
-- ON DELETE CASCADE de proposito: um alimento ou beneficiario com
-- distribuicoes registradas nao pode ser excluido, preservando o historico.
CREATE TABLE IF NOT EXISTS distribuicoes (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    alimento_id      INT NOT NULL,
    beneficiario_id  INT NOT NULL,
    data             DATE NOT NULL,
    quantidade       DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (alimento_id) REFERENCES alimentos(id),
    FOREIGN KEY (beneficiario_id) REFERENCES beneficiarios(id)
);
