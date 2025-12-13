DROP TABLE IF EXISTS nomic_games;

CREATE TABLE IF NOT EXISTS nomic_games (
id bigserial PRIMARY KEY,
description TEXT NOT NULL,
name VARCHAR(50) NOT NULL,
metacritic INTEGER NOT NULL,
released VARCHAR(50) NOT NULL,
playtime INTEGER NOT NULL,
embedding vector(768)
);

DROP TABLE IF EXISTS snowflake_games;

CREATE TABLE IF NOT EXISTS snowflake_games (
id bigserial PRIMARY KEY,
description TEXT NOT NULL,
name VARCHAR(50) NOT NULL,
metacritic INTEGER NOT NULL,
released VARCHAR(50) NOT NULL,
playtime INTEGER NOT NULL,
embedding vector(1024)
);

DROP TABLE IF EXISTS mxbai_games;

CREATE TABLE IF NOT EXISTS mxbai_games (
id bigserial PRIMARY KEY,
description TEXT NOT NULL,
name VARCHAR(50) NOT NULL,
metacritic INTEGER NOT NULL,
released VARCHAR(50) NOT NULL,
playtime INTEGER NOT NULL,
embedding vector(1024)
)