-- Locadora: dinheiro em centavos (INTEGER), datas em texto ISO (AAAA-MM-DD)
-- Titulo usa tabela unica com a coluna tipo escolhendo a subclasse

CREATE TABLE titulo (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo              TEXT    NOT NULL CHECK (tipo IN ('Blu-ray', 'DVD', 'CD', 'Fita')),
    nome              TEXT    NOT NULL,
    descricao         TEXT    NOT NULL DEFAULT '',
    ano               INTEGER NOT NULL,
    prazo_maximo_dias INTEGER NOT NULL CHECK (prazo_maximo_dias > 0),
    capa              BLOB
);

CREATE TABLE cliente (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    nome     TEXT    NOT NULL,
    cpf      TEXT    NOT NULL UNIQUE,
    telefone TEXT    NOT NULL DEFAULT '',
    email    TEXT    NOT NULL DEFAULT '',
    endereco TEXT    NOT NULL DEFAULT '',
    ativo    INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE funcionario (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    nome       TEXT NOT NULL,
    cpf        TEXT NOT NULL UNIQUE,
    telefone   TEXT NOT NULL DEFAULT '',
    email      TEXT NOT NULL DEFAULT '',
    login      TEXT NOT NULL UNIQUE,
    senha_hash TEXT NOT NULL,
    sal        TEXT NOT NULL,
    cargo      TEXT NOT NULL CHECK (cargo IN ('GERENTE', 'ATENDENTE'))
);

CREATE TABLE aquisicao (
    id                      INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo_id               INTEGER NOT NULL REFERENCES titulo (id),
    quantidade              INTEGER NOT NULL CHECK (quantidade > 0),
    custo_unitario_centavos INTEGER NOT NULL CHECK (custo_unitario_centavos >= 0),
    fornecedor              TEXT    NOT NULL,
    data                    TEXT    NOT NULL,
    funcionario_id          INTEGER NOT NULL REFERENCES funcionario (id)
);

CREATE TABLE exemplar (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo         TEXT    NOT NULL UNIQUE,
    titulo_id      INTEGER NOT NULL REFERENCES titulo (id),
    aquisicao_id   INTEGER NOT NULL REFERENCES aquisicao (id),
    custo_centavos INTEGER NOT NULL,
    situacao       TEXT    NOT NULL CHECK (situacao IN ('DISPONIVEL', 'ALUGADO', 'RESERVADO', 'PERDIDO'))
);

CREATE TABLE locacao (
    id                       INTEGER PRIMARY KEY AUTOINCREMENT,
    exemplar_id              INTEGER NOT NULL REFERENCES exemplar (id),
    cliente_id               INTEGER NOT NULL REFERENCES cliente (id),
    funcionario_locacao_id   INTEGER NOT NULL REFERENCES funcionario (id),
    funcionario_devolucao_id INTEGER REFERENCES funcionario (id),
    data_locacao             TEXT    NOT NULL,
    data_prevista            TEXT    NOT NULL,
    data_devolucao           TEXT,
    valor_centavos           INTEGER NOT NULL,
    multa_centavos           INTEGER NOT NULL DEFAULT 0,
    data_pagamento_multa     TEXT,
    renovada                 INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE reserva (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo_id      INTEGER NOT NULL REFERENCES titulo (id),
    cliente_id     INTEGER NOT NULL REFERENCES cliente (id),
    funcionario_id INTEGER NOT NULL REFERENCES funcionario (id),
    data           TEXT    NOT NULL,
    situacao       TEXT    NOT NULL CHECK (situacao IN ('AGUARDANDO', 'PRONTA', 'ATENDIDA', 'CANCELADA')),
    exemplar_id    INTEGER REFERENCES exemplar (id)
);

CREATE TABLE perda (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    exemplar_id    INTEGER NOT NULL REFERENCES exemplar (id),
    motivo         TEXT    NOT NULL,
    data           TEXT    NOT NULL,
    funcionario_id INTEGER NOT NULL REFERENCES funcionario (id),
    locacao_id     INTEGER REFERENCES locacao (id)
);

CREATE INDEX idx_exemplar_titulo ON exemplar (titulo_id);
CREATE INDEX idx_locacao_cliente ON locacao (cliente_id);
CREATE INDEX idx_locacao_exemplar ON locacao (exemplar_id);
CREATE INDEX idx_reserva_titulo ON reserva (titulo_id, situacao);
