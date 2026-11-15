-- SIGC - Crea las bases de datos vacias. Las tablas las genera DataNucleus
-- al primer arranque (ver db/esquema-referencia.sql para consultarlas).
-- Ejecutar como root:  SOURCE db/crear-bases.sql;

CREATE DATABASE IF NOT EXISTS sigc
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- Base exclusiva para pruebas de integracion: sus tablas se VACIAN en cada corrida.
CREATE DATABASE IF NOT EXISTS sigc_test
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- Reemplazar TU_CLAVE_LOCAL por una contrasena propia. No subir esa clave al repositorio.
-- CREATE USER 'sigc'@'localhost' IDENTIFIED BY 'TU_CLAVE_LOCAL';
-- GRANT ALL PRIVILEGES ON sigc.*      TO 'sigc'@'localhost';
-- GRANT ALL PRIVILEGES ON sigc_test.* TO 'sigc'@'localhost';
-- FLUSH PRIVILEGES;
