USE barbearia;

-- Execute uma vez no banco existente, com o desktop e a web fechados.
ALTER TABLE agendamento
    ADD COLUMN duracao_reservada INT NULL,
    ADD COLUMN horario_ativo DATETIME GENERATED ALWAYS AS
        (CASE WHEN status <> 'cancelado' THEN data_hora ELSE NULL END) STORED,
    DROP INDEX uq_barbeiro_horario,
    ADD UNIQUE KEY uq_barbeiro_horario (id_barbeiro, horario_ativo);

-- Para registros antigos, a única duração disponível é a do cadastro atual.
UPDATE agendamento a
JOIN (
    SELECT i.id_agendamento, SUM(s.duracao_min) AS minutos
    FROM agendamento_servico i
    JOIN servico s ON s.id_servico = i.id_servico
    GROUP BY i.id_agendamento
) AS duracoes ON duracoes.id_agendamento = a.id_agendamento
SET a.duracao_reservada = duracoes.minutos
WHERE a.duracao_reservada IS NULL AND duracoes.minutos > 0;
