

--CREATE DATABASE propraganda IF NOT EXISTS

-- DO '
-- DECLARE
--     BEGIN
--         IF EXISTS (SELECT FROM pg_database WHERE datname = ''propraganda'') THEN
--             RAISE NOTICE ''Database already exists'';  -- optional
--         ELSE
--             PERFORM dblink_exec(''dbname='' || current_database()  -- current db
--                 , ''CREATE DATABASE propraganda'');
--         END IF;
--     END;
-- ';

CREATE TABLE IF NOT EXISTS Benutzer (
                                        benutzer_id BIGSERIAL NOT NULL PRIMARY KEY,
                                        git_hub_user_id int4 NOT NULL,
                                        git_hub_name VARCHAR(50) NOT NULL,
                                        benutzer_typ VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS Uebung (
                                      uebung_id BIGSERIAL NOT NULL PRIMARY KEY,
                                      gruppen_groesse int2 NOT NULL,
                                      anmelde_typ VARCHAR(50) NOT NULL,
                                      anmeldezeitraum_zeitraum_start timestamp NOT NULL,
                                      anmeldezeitraum_zeitraum_ende timestamp NOT NULL,
                                      uebungszeitraum_zeitraum_start timestamp NOT NULL,
                                      uebungszeitraum_zeitraum_ende timestamp NOT NULL

);

CREATE TABLE IF NOT EXISTS Zeitslot (
                                      zeitslot_id BIGSERIAL NOT NULL PRIMARY KEY,
                                      groesse int2 NOT NULL,
                                      tutor_id bigint[],
                                      datum timestamp NOT NULL,
                                      UEBUNG BIGINT NOT NULL
);


CREATE TABLE IF NOT EXISTS Termin (
                                      termin_id SERIAL PRIMARY KEY NOT NULL,
                                      belegt boolean,
                                      datum TIMESTAMP NOT NULL,
                                      tutor BIGINT NULL REFERENCES Benutzer(benutzer_id),
                                      UEBUNG BIGINT NOT NULL

--                                      PRIMARY KEY (termin_id, Uebung)
);

CREATE TABLE IF NOT EXISTS Gruppe (
                                      Gruppen_ID SERIAL PRIMARY KEY,
                                      name VARCHAR(50),
                                      mitglieder BIGINT[],
--                                      mitglieder_key SMALLINT,
                                      termin BIGINT REFERENCES Termin(termin_id)
                                      -- Repo BIGINT REFERENCES Repo(repo_id), REFERENCES Benutzer(benutzer_id),
                                      --PRIMARY KEY (Gruppen_ID, termin)
);

-- CREATE TABLE IF NOT EXISTS Repo (
--                                     Repo_id SERIAL PRIMARY KEY,
--                                     url VARCHAR(255) NULL,
--                                     name VARCHAR(255) NULL,
--
--                                     gruppe BIGINT REFERENCES Gruppe(Gruppen_ID)
--     --termin BIGINT REFERENCES Termin(termin_id)
-- );