-- Sistema de Gestao de Banco de Alimentos
-- Script de criacao do banco (MySQL)
-- AEP 2026.2 - Engenharia de Software - UniCesumar

CREATE DATABASE banco_alimentos;
USE banco_alimentos;

CREATE TABLE doadores (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    nome     VARCHAR(120) NOT NULL,
    tipo     VARCHAR(20) NOT NULL,
    contato  VARCHAR(120)
);

CREATE TABLE alimentos (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    nome              VARCHAR(120) NOT NULL,
    tipo              VARCHAR(20) NOT NULL,
    quantidade        DECIMAL(10,2) NOT NULL,
    data_recebimento  DATE NOT NULL,
    data_validade     DATE,
    doador_id         INT NOT NULL,
    FOREIGN KEY (doador_id) REFERENCES doadores(id)
);

CREATE TABLE beneficiarios (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    nome     VARCHAR(120) NOT NULL,
    contato  VARCHAR(120)
);

CREATE TABLE distribuicoes (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    alimento_id      INT NOT NULL,
    beneficiario_id  INT NOT NULL,
    data             DATE NOT NULL,
    quantidade       DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (alimento_id) REFERENCES alimentos(id),
    FOREIGN KEY (beneficiario_id) REFERENCES beneficiarios(id)
);
