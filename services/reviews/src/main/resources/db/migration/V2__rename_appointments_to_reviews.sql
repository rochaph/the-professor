-- Domínio migrado de atendimento veterinário para análise de partida de CS2.
-- A V1 já foi aplicada e fica intocada (checksum no flyway_schema_history).
ALTER TABLE appointments RENAME TO reviews;
ALTER TABLE reviews RENAME CONSTRAINT appointments_pkey TO reviews_pkey;
ALTER TABLE reviews RENAME COLUMN pet_id TO team_id;
ALTER TABLE reviews RENAME COLUMN veterinarian_id TO match_id;
ALTER TABLE reviews RENAME COLUMN scheduled_at TO played_at;
