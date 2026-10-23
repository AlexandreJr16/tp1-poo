-- Dados de demonstracao, carregados so na primeira execucao.
-- As datas sao relativas ao dia de hoje para o painel sempre ter atrasadas e devolucoes proximas.
-- Logins: admin / admin123 (gerente), joana / joana123 e marcos / marcos123 (atendentes).

INSERT INTO funcionario (id, nome, cpf, telefone, email, login, senha_hash, sal, cargo) VALUES
    (1, 'Administrador', '56884911247', '(92) 3232-1000', 'admin@locadora.com', 'admin',
     'f3893f3bbb167b24641d7f3e6b54918e3e1f1687f0465b5b1e9026a71280a315', '5f2b8c1d9e7a4f30b6c2d8e1a9f7c3b5', 'GERENTE'),
    (2, 'Joana Ribeiro', '04132450635', '(92) 99101-2020', 'joana@locadora.com', 'joana',
     '94f51198fb72fd87d26f7b1dcea29f31977a3bc5fddc4dc958d163e4924ae4d7', '9c4e1a7b3d5f2e8c0a6b4d9f1e3c7a2b', 'ATENDENTE'),
    (3, 'Marcos Tavares', '83892320306', '(92) 99202-3030', 'marcos@locadora.com', 'marcos',
     'ba0c0488205eaa62889203c8bbfbb83e823218ae77084b35cf1b15b4ccf912fc', '3e7a9c1f5b2d8e4a6c0f9b3d7e1a5c8f', 'ATENDENTE');

INSERT INTO cliente (id, nome, cpf, telefone, email, endereco, ativo) VALUES
    (1, 'Ana Beatriz Souza', '23915997064', '(92) 99123-4567', 'ana.souza@email.com', 'Rua 10 de Julho, 450 - Centro', 1),
    (2, 'Bruno Carvalho', '61380180872', '(92) 99234-5678', 'bruno.carvalho@email.com', 'Av. Djalma Batista, 1200 - Chapada', 1),
    (3, 'Camila Rocha', '62473427801', '(92) 99345-6789', 'camila.rocha@email.com', 'Rua Ferreira Pena, 88 - Centro', 1),
    (4, 'Diego Martins', '13355884502', '(92) 99456-7890', 'diego.martins@email.com', 'Av. Constantino Nery, 3000 - Flores', 1),
    (5, 'Eduarda Lima', '60051800020', '(92) 99567-8901', 'eduarda.lima@email.com', 'Rua Rio Purus, 215 - Vieiralves', 1),
    (6, 'Felipe Andrade', '40632650303', '(92) 99678-9012', 'felipe.andrade@email.com', 'Av. Torquato Tapajós, 5100 - Flores', 1),
    (7, 'Gabriela Nunes', '08158908500', '(92) 99789-0123', 'gabriela.nunes@email.com', 'Rua Pará, 640 - Vieiralves', 1),
    (8, 'Henrique Barros', '15844474350', '(92) 99890-1234', 'henrique.barros@email.com', 'Av. Efigênio Salles, 2100 - Aleixo', 1),
    (9, 'Isabela Freitas', '66899085186', '(92) 99901-2345', 'isabela.freitas@email.com', 'Rua Salvador, 120 - Adrianópolis', 1),
    (10, 'João Pedro Alves', '11363034308', '(92) 98112-3456', 'joao.alves@email.com', 'Av. André Araújo, 900 - Aleixo', 1),
    (11, 'Larissa Monteiro', '96804262204', '(92) 98223-4567', 'larissa.monteiro@email.com', 'Rua Belo Horizonte, 33 - Adrianópolis', 1),
    (12, 'Mateus Oliveira', '34943763790', '(92) 98334-5678', 'mateus.oliveira@email.com', 'Av. Ephigênio Salles, 1500 - Parque 10', 1),
    (13, 'Otávio Prado', '05238457820', '(92) 98445-6789', 'otavio.prado@email.com', 'Rua Recife, 410 - Parque 10', 0);

INSERT INTO titulo (id, tipo, nome, descricao, ano, prazo_maximo_dias) VALUES
    (1, 'Blu-ray', 'Duna: Parte Dois', 'Ficção científica de Denis Villeneuve.', 2024, 3),
    (2, 'Blu-ray', 'Interestelar', 'Ficção científica de Christopher Nolan.', 2014, 3),
    (3, 'Blu-ray', 'Homem-Aranha: Através do Aranhaverso', 'Animação premiada com estética de quadrinhos.', 2023, 3),
    (4, 'DVD', 'Cidade de Deus', 'Drama de Fernando Meirelles e Kátia Lund.', 2002, 5),
    (5, 'DVD', 'O Auto da Compadecida', 'Comédia de Guel Arraes baseada em Ariano Suassuna.', 2000, 5),
    (6, 'DVD', 'Central do Brasil', 'Drama de Walter Salles.', 1998, 5),
    (7, 'DVD', 'Matrix', 'Ação e ficção científica das irmãs Wachowski.', 1999, 5),
    (8, 'DVD', 'O Senhor dos Anéis: A Sociedade do Anel', 'Fantasia de Peter Jackson.', 2001, 5),
    (9, 'DVD', 'Bacurau', 'Suspense de Kleber Mendonça Filho e Juliano Dornelles.', 2019, 5),
    (10, 'CD', 'Clube da Esquina', 'Milton Nascimento e Lô Borges.', 1972, 7),
    (11, 'CD', 'Acabou Chorare', 'Novos Baianos.', 1972, 7),
    (12, 'CD', 'Tropicália ou Panis et Circencis', 'Caetano, Gil, Gal, Os Mutantes e Tom Zé.', 1968, 7),
    (13, 'CD', 'Construção', 'Chico Buarque.', 1971, 7),
    (14, 'CD', 'Sobrevivendo no Inferno', 'Racionais MCs.', 1997, 7),
    (15, 'Fita', 'De Volta para o Futuro', 'Aventura de Robert Zemeckis.', 1985, 2),
    (16, 'Fita', 'Os Trapalhões na Guerra dos Planetas', 'Comédia com Didi, Dedé, Mussum e Zacarias.', 1978, 2),
    (17, 'Fita', 'Tubarão', 'Suspense de Steven Spielberg.', 1975, 2),
    (18, 'Fita', 'E.T. - O Extraterrestre', 'Aventura de Steven Spielberg.', 1982, 2);

INSERT INTO aquisicao (id, titulo_id, quantidade, custo_unitario_centavos, fornecedor, data, funcionario_id) VALUES
    (1, 1, 2, 8990, 'Distribuidora Amazônia Filmes', date('now', 'localtime', '-90 days'), 1),
    (2, 2, 2, 7990, 'Distribuidora Amazônia Filmes', date('now', 'localtime', '-90 days'), 1),
    (3, 3, 2, 8490, 'Distribuidora Amazônia Filmes', date('now', 'localtime', '-85 days'), 1),
    (4, 4, 3, 2990, 'Distribuidora Amazônia Filmes', date('now', 'localtime', '-85 days'), 1),
    (5, 5, 3, 2490, 'Distribuidora Amazônia Filmes', date('now', 'localtime', '-80 days'), 1),
    (6, 6, 2, 2490, 'Distribuidora Amazônia Filmes', date('now', 'localtime', '-80 days'), 1),
    (7, 7, 3, 2790, 'Distribuidora Amazônia Filmes', date('now', 'localtime', '-80 days'), 1),
    (8, 8, 2, 3490, 'Distribuidora Amazônia Filmes', date('now', 'localtime', '-75 days'), 1),
    (9, 9, 1, 3290, 'Distribuidora Amazônia Filmes', date('now', 'localtime', '-75 days'), 1),
    (10, 10, 2, 3990, 'Discos Rio Negro', date('now', 'localtime', '-70 days'), 1),
    (11, 11, 2, 3490, 'Discos Rio Negro', date('now', 'localtime', '-70 days'), 1),
    (12, 12, 1, 4490, 'Discos Rio Negro', date('now', 'localtime', '-70 days'), 1),
    (13, 13, 2, 3690, 'Discos Rio Negro', date('now', 'localtime', '-65 days'), 1),
    (14, 14, 2, 4290, 'Discos Rio Negro', date('now', 'localtime', '-65 days'), 1),
    (15, 15, 2, 1990, 'Sebo do Largo', date('now', 'localtime', '-60 days'), 1),
    (16, 16, 2, 1490, 'Sebo do Largo', date('now', 'localtime', '-60 days'), 1),
    (17, 17, 1, 1790, 'Sebo do Largo', date('now', 'localtime', '-60 days'), 1),
    (18, 18, 1, 1790, 'Sebo do Largo', date('now', 'localtime', '-60 days'), 1),
    (19, 7, 1, 2690, 'Distribuidora Amazônia Filmes', date('now', 'localtime', '-20 days'), 1);

WITH RECURSIVE sequencia(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM sequencia WHERE n < 5)
INSERT INTO exemplar (codigo, titulo_id, aquisicao_id, custo_centavos, situacao)
SELECT printf('T%d-%03d', a.titulo_id, s.n), a.titulo_id, a.id, a.custo_unitario_centavos, 'DISPONIVEL'
FROM aquisicao a JOIN sequencia s ON s.n <= a.quantidade
WHERE a.id <= 18
ORDER BY a.titulo_id, s.n;

INSERT INTO exemplar (codigo, titulo_id, aquisicao_id, custo_centavos, situacao) VALUES ('T7-004', 7, 19, 2690, 'DISPONIVEL');

INSERT INTO locacao (exemplar_id, cliente_id, funcionario_locacao_id, funcionario_devolucao_id, data_locacao, data_prevista,
                     data_devolucao, valor_centavos, multa_centavos, data_pagamento_multa, renovada) VALUES
    ((SELECT id FROM exemplar WHERE codigo = 'T11-001'), 2, 2, 2, date('now', 'localtime', '-40 days'), date('now', 'localtime', '-33 days'),
     date('now', 'localtime', '-35 days'), 500, 0, NULL, 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T5-001'), 1, 2, 3, date('now', 'localtime', '-30 days'), date('now', 'localtime', '-25 days'),
     date('now', 'localtime', '-23 days'), 800, 700, date('now', 'localtime', '-23 days'), 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T7-002'), 5, 1, 1, date('now', 'localtime', '-25 days'), date('now', 'localtime', '-20 days'),
     date('now', 'localtime', '-21 days'), 800, 0, NULL, 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T6-001'), 9, 2, 1, date('now', 'localtime', '-25 days'), date('now', 'localtime', '-20 days'),
     date('now', 'localtime', '-18 days'), 800, 3190, date('now', 'localtime', '-18 days'), 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T8-001'), 8, 3, 2, date('now', 'localtime', '-20 days'), date('now', 'localtime', '-15 days'),
     date('now', 'localtime', '-12 days'), 800, 1050, NULL, 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T14-001'), 11, 3, 3, date('now', 'localtime', '-16 days'), date('now', 'localtime', '-9 days'),
     date('now', 'localtime', '-6 days'), 500, 600, date('now', 'localtime', '-6 days'), 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T9-001'), 6, 2, 2, date('now', 'localtime', '-15 days'), date('now', 'localtime', '-10 days'),
     date('now', 'localtime', '-10 days'), 800, 0, NULL, 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T13-002'), 3, 3, 3, date('now', 'localtime', '-14 days'), date('now', 'localtime', '-7 days'),
     date('now', 'localtime', '-8 days'), 500, 0, NULL, 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T4-002'), 12, 2, 2, date('now', 'localtime', '-12 days'), date('now', 'localtime', '-7 days'),
     date('now', 'localtime', '-7 days'), 800, 0, NULL, 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T13-001'), 11, 2, NULL, date('now', 'localtime', '-10 days'), date('now', 'localtime', '+4 days'),
     NULL, 500, 0, NULL, 1),
    ((SELECT id FROM exemplar WHERE codigo = 'T7-001'), 4, 2, NULL, date('now', 'localtime', '-9 days'), date('now', 'localtime', '-4 days'),
     NULL, 800, 0, NULL, 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T2-001'), 6, 3, NULL, date('now', 'localtime', '-6 days'), date('now', 'localtime', '-3 days'),
     NULL, 1200, 0, NULL, 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T17-001'), 7, 2, NULL, date('now', 'localtime', '-5 days'), date('now', 'localtime', '-3 days'),
     NULL, 400, 0, NULL, 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T10-001'), 3, 2, NULL, date('now', 'localtime', '-3 days'), date('now', 'localtime', '+4 days'),
     NULL, 500, 0, NULL, 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T4-001'), 1, 2, NULL, date('now', 'localtime', '-2 days'), date('now', 'localtime', '+3 days'),
     NULL, 800, 0, NULL, 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T3-001'), 10, 1, NULL, date('now', 'localtime', '-2 days'), date('now', 'localtime', '+1 days'),
     NULL, 1200, 0, NULL, 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T1-001'), 2, 3, NULL, date('now', 'localtime', '-1 days'), date('now', 'localtime', '+2 days'),
     NULL, 1200, 0, NULL, 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T15-001'), 5, 3, NULL, date('now', 'localtime', '-1 days'), date('now', 'localtime', '+1 days'),
     NULL, 400, 0, NULL, 0),
    ((SELECT id FROM exemplar WHERE codigo = 'T1-002'), 9, 2, NULL, date('now', 'localtime'), date('now', 'localtime', '+3 days'),
     NULL, 1200, 0, NULL, 0);

UPDATE exemplar SET situacao = 'ALUGADO'
WHERE id IN (SELECT exemplar_id FROM locacao WHERE data_devolucao IS NULL);

INSERT INTO reserva (titulo_id, cliente_id, funcionario_id, data, situacao, exemplar_id) VALUES
    (8, 2, 2, date('now', 'localtime', '-18 days'), 'CANCELADA', NULL),
    (9, 12, 2, date('now', 'localtime', '-12 days'), 'PRONTA', (SELECT id FROM exemplar WHERE codigo = 'T9-001')),
    (3, 10, 3, date('now', 'localtime', '-4 days'), 'ATENDIDA', (SELECT id FROM exemplar WHERE codigo = 'T3-001')),
    (17, 1, 3, date('now', 'localtime', '-2 days'), 'AGUARDANDO', NULL),
    (1, 11, 2, date('now', 'localtime', '-1 days'), 'AGUARDANDO', NULL),
    (1, 4, 3, date('now', 'localtime'), 'AGUARDANDO', NULL);

UPDATE exemplar SET situacao = 'RESERVADO' WHERE codigo = 'T9-001';

INSERT INTO perda (exemplar_id, motivo, data, funcionario_id, locacao_id) VALUES
    ((SELECT id FROM exemplar WHERE codigo = 'T16-002'), 'Fita mofada na prateleira.', date('now', 'localtime', '-15 days'), 1, NULL),
    ((SELECT id FROM exemplar WHERE codigo = 'T6-001'), 'Cliente informou que perdeu o disco.', date('now', 'localtime', '-18 days'), 1,
     (SELECT id FROM locacao WHERE exemplar_id = (SELECT id FROM exemplar WHERE codigo = 'T6-001')));

UPDATE exemplar SET situacao = 'PERDIDO' WHERE codigo IN ('T16-002', 'T6-001');
