-- Sistema de Gestao de Banco de Alimentos
-- Carga de dados de exemplo (MySQL 8) - para testes e demonstracao.
-- AEP 2026.2 - Engenharia de Software - UniCesumar
--
-- Execucao (depois de schema.sql, na raiz do repositorio):
--   mysql -u root -p -e "source database/dados_exemplo.sql"
--
-- ATENCAO: apaga todos os registros das quatro tabelas antes de inserir.
-- Pode ser executado quantas vezes for preciso para voltar ao estado inicial.
--
-- As datas sao calculadas a partir do dia da execucao (CURDATE()), de modo que
-- sempre existam alimentos vencidos (RF07), perto do vencimento (RF06) e com
-- validade distante, qualquer que seja a data em que o script for rodado.
-- Nomes, contatos e enderecos sao ficticios.

USE banco_alimentos;

-- Ordem inversa das chaves estrangeiras. O "WHERE id > 0" mantem o script
-- compativel com o modo "safe updates" do MySQL Workbench.
DELETE FROM distribuicoes WHERE id > 0;
DELETE FROM alimentos     WHERE id > 0;
DELETE FROM beneficiarios WHERE id > 0;
DELETE FROM doadores      WHERE id > 0;

INSERT INTO doadores (id, nome, tipo, contato) VALUES
    (1, 'Supermercado Modelo',               'MERCADO',       '(44) 3000-0001'),
    (2, 'Joao Carlos Pereira',               'PESSOA_FISICA', 'joao.pereira@exemplo.com'),
    (3, 'Industria de Conservas Vale Verde', 'INDUSTRIA',     'doacoes@valeverde.exemplo.com'),
    (4, 'Hortifruti Boa Colheita',           'MERCADO',       '(44) 3000-0004'),
    (5, 'Padaria Pao Dourado',               'MERCADO',       '(44) 3000-0005');

INSERT INTO beneficiarios (id, nome, contato) VALUES
    (1, 'Associacao Comunitaria Vila Esperanca', '(44) 99000-0101'),
    (2, 'Casa de Apoio Recomecar',               '(44) 99000-0102'),
    (3, 'Familia Oliveira',                      '(44) 99000-0103'),
    (4, 'Familia Santos',                        '(44) 99000-0104');

-- quantidade = saldo atual (ja descontadas as distribuicoes abaixo).
-- Pereciveis: data_validade relativa a hoje. Nao pereciveis: data_validade nula.
INSERT INTO alimentos (id, nome, tipo, quantidade, data_recebimento, data_validade, doador_id) VALUES
    (1,  'Leite integral 1L',        'PERECIVEL',     12.00, DATE_SUB(CURDATE(), INTERVAL 5 DAY),  DATE_ADD(CURDATE(), INTERVAL 2 DAY),  1),
    (2,  'Pao frances (kg)',         'PERECIVEL',      8.50, DATE_SUB(CURDATE(), INTERVAL 1 DAY),  DATE_ADD(CURDATE(), INTERVAL 1 DAY),  5),
    (3,  'Iogurte natural 170g',     'PERECIVEL',     20.00, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY),  1),
    (4,  'Banana prata (kg)',        'PERECIVEL',     15.00, DATE_SUB(CURDATE(), INTERVAL 3 DAY),  DATE_ADD(CURDATE(), INTERVAL 4 DAY),  4),
    (5,  'Alface crespa (unidade)',  'PERECIVEL',      9.00, DATE_SUB(CURDATE(), INTERVAL 6 DAY),  DATE_SUB(CURDATE(), INTERVAL 1 DAY),  4),
    (6,  'Queijo mussarela (kg)',    'PERECIVEL',      5.00, DATE_SUB(CURDATE(), INTERVAL 2 DAY),  DATE_ADD(CURDATE(), INTERVAL 20 DAY), 1),
    (7,  'Arroz tipo 1 (pacote 5kg)','NAO_PERECIVEL', 34.00, DATE_SUB(CURDATE(), INTERVAL 15 DAY), NULL, 3),
    (8,  'Feijao carioca (1kg)',     'NAO_PERECIVEL', 55.00, DATE_SUB(CURDATE(), INTERVAL 15 DAY), NULL, 2),
    (9,  'Oleo de soja 900ml',       'NAO_PERECIVEL', 25.00, DATE_SUB(CURDATE(), INTERVAL 7 DAY),  NULL, 3),
    (10, 'Macarrao espaguete 500g',  'NAO_PERECIVEL', 30.00, DATE_SUB(CURDATE(), INTERVAL 4 DAY),  NULL, 1),
    (11, 'Milho em conserva 200g',   'NAO_PERECIVEL', 48.00, DATE_SUB(CURDATE(), INTERVAL 20 DAY), NULL, 3);

INSERT INTO distribuicoes (id, alimento_id, beneficiario_id, data, quantidade) VALUES
    (1, 7,  1, DATE_SUB(CURDATE(), INTERVAL 10 DAY),  6.00),
    (2, 8,  2, DATE_SUB(CURDATE(), INTERVAL 9 DAY),   5.00),
    (3, 3,  1, DATE_SUB(CURDATE(), INTERVAL 5 DAY),  10.00),
    (4, 1,  3, DATE_SUB(CURDATE(), INTERVAL 3 DAY),  12.00),
    (5, 10, 4, DATE_SUB(CURDATE(), INTERVAL 1 DAY),   5.00);
