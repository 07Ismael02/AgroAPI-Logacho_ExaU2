-- Usuarios de demostracion del anexo oficial. Los valores almacenados son hashes BCrypt
-- diferentes para la contrasena de prueba indicada por el docente.
INSERT INTO rol (id, nombre) VALUES (1, 'TECNICO');
INSERT INTO rol (id, nombre) VALUES (2, 'JEFE_CAMPO');
INSERT INTO rol (id, nombre) VALUES (3, 'ADMINISTRADOR');

INSERT INTO privilegio (id, codigo) VALUES (1, 'LOTE_LEER');
INSERT INTO privilegio (id, codigo) VALUES (2, 'LABOR_LEER');
INSERT INTO privilegio (id, codigo) VALUES (3, 'LABOR_CREAR');
INSERT INTO privilegio (id, codigo) VALUES (4, 'LABOR_EDITAR');
INSERT INTO privilegio (id, codigo) VALUES (5, 'LABOR_ELIMINAR');
INSERT INTO privilegio (id, codigo) VALUES (6, 'CAMPANIA_CERRAR');
INSERT INTO privilegio (id, codigo) VALUES (7, 'USUARIO_GESTIONAR');

INSERT INTO rol_privilegio (rol_id, privilegio_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4),
    (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6),
    (3, 1), (3, 2), (3, 7);

INSERT INTO usuario (id, username, password_hash, estado, rol_id) VALUES
    (1, 'tecnico1', '$2a$10$bUaYqvAr.aa32jjaDvJQROW8.veu8WI29d1YO./0cX1U5nz4NQRiS', TRUE, 1),
    (2, 'jefe1', '$2a$10$yzMX.bMlFXQfzoGUZZFFsuWB20M1cWSnaTT9/NL1g5e6wPsDRQ7xy', TRUE, 2),
    (3, 'admin', '$2a$10$xlTyFJeTEvUqtdwA6bRCp.//GlRE6kzF.d.LbcSOZeV8B2ZwHzipe', TRUE, 3);

-- Ocho lotes en tres valles; 4 y 8 tienen campania cerrada.
INSERT INTO lote (id, codigo, nombre, hectareas, cultivo, valle, campania_cerrada, dosis_maxima_por_hectarea) VALUES
    (1, 'ALT-001', 'Lote Mirador', 2.5, 'Papa', 'Valle Alto', FALSE, 2.0),
    (2, 'ALT-002', 'Lote Quebrada', 3.0, 'Maiz', 'Valle Alto', FALSE, 1.5),
    (3, 'ALT-003', 'Lote Pampa', 1.8, 'Quinua', 'Valle Alto', FALSE, 1.2),
    (4, 'MED-001', 'Lote Molino', 4.0, 'Trigo', 'Valle Medio', TRUE, 2.0),
    (5, 'MED-002', 'Lote Sauces', 2.2, 'Papa', 'Valle Medio', FALSE, 1.8),
    (6, 'MED-003', 'Lote Acequia', 3.4, 'Maiz', 'Valle Medio', FALSE, 1.6),
    (7, 'BAJ-001', 'Lote Ribera', 2.7, 'Arroz', 'Valle Bajo', FALSE, 2.5),
    (8, 'BAJ-002', 'Lote Ladera', 1.6, 'Quinua', 'Valle Bajo', TRUE, 1.3);

-- Veinte labores; todos los lotes abiertos tienen al menos dos.
-- El lote 1 tiene COSECHA y el lote 2 no la tiene, para demostrar RN-03.
INSERT INTO labor (id, lote_id, tipo, fecha, responsable_usuario_id, cantidad, observacion, creado_en) VALUES
    (1, 1, 'COSECHA', CURRENT_DATE, 1, 1.0, 'Cosecha del sector norte', DATEADD('HOUR', -2, CURRENT_TIMESTAMP())),
    (2, 1, 'RIEGO', CURRENT_DATE, 1, 1.0, 'Riego de mantenimiento', DATEADD('HOUR', -3, CURRENT_TIMESTAMP())),
    (3, 1, 'FUMIGACION', CURRENT_DATE, 2, 2.0, 'Aplicacion preventiva', DATEADD('HOUR', -4, CURRENT_TIMESTAMP())),
    (4, 2, 'RIEGO', CURRENT_DATE, 1, 1.0, 'Primer riego', DATEADD('HOUR', -2, CURRENT_TIMESTAMP())),
    (5, 2, 'FUMIGACION', CURRENT_DATE, 1, 2.0, 'Control preventivo', DATEADD('HOUR', -5, CURRENT_TIMESTAMP())),
    (6, 2, 'RIEGO', DATEADD('DAY', -2, CURRENT_DATE), 2, 1.0, 'Segundo riego', DATEADD('HOUR', -48, CURRENT_TIMESTAMP())),
    (7, 3, 'RIEGO', CURRENT_DATE, 1, 1.0, 'Riego de quinua', DATEADD('HOUR', -2, CURRENT_TIMESTAMP())),
    (8, 3, 'FUMIGACION', CURRENT_DATE, 2, 1.0, 'Prevencion de plagas', DATEADD('HOUR', -3, CURRENT_TIMESTAMP())),
    (9, 4, 'COSECHA', DATEADD('DAY', -3, CURRENT_DATE), 2, 2.0, 'Campania concluida', DATEADD('HOUR', -72, CURRENT_TIMESTAMP())),
    (10, 4, 'RIEGO', DATEADD('DAY', -4, CURRENT_DATE), 1, 1.0, 'Ultimo riego', DATEADD('HOUR', -96, CURRENT_TIMESTAMP())),
    (11, 5, 'RIEGO', CURRENT_DATE, 1, 1.0, 'Riego de papa', DATEADD('HOUR', -2, CURRENT_TIMESTAMP())),
    (12, 5, 'FUMIGACION', CURRENT_DATE, 2, 1.5, 'Aplicacion foliar', DATEADD('HOUR', -3, CURRENT_TIMESTAMP())),
    (13, 5, 'COSECHA', CURRENT_DATE, 1, 1.0, 'Cosecha parcial', DATEADD('HOUR', -4, CURRENT_TIMESTAMP())),
    (14, 6, 'RIEGO', CURRENT_DATE, 1, 1.0, 'Riego de maiz', DATEADD('HOUR', -2, CURRENT_TIMESTAMP())),
    (15, 6, 'FUMIGACION', CURRENT_DATE, 2, 2.0, 'Control sanitario', DATEADD('HOUR', -3, CURRENT_TIMESTAMP())),
    (16, 7, 'RIEGO', CURRENT_DATE, 1, 1.0, 'Riego de arroz', DATEADD('HOUR', -2, CURRENT_TIMESTAMP())),
    (17, 7, 'FUMIGACION', CURRENT_DATE, 1, 3.0, 'Control de maleza', DATEADD('HOUR', -3, CURRENT_TIMESTAMP())),
    (18, 7, 'COSECHA', CURRENT_DATE, 2, 1.0, 'Cosecha del primer tramo', DATEADD('HOUR', -4, CURRENT_TIMESTAMP())),
    (19, 8, 'COSECHA', DATEADD('DAY', -3, CURRENT_DATE), 2, 1.0, 'Cosecha final', DATEADD('HOUR', -72, CURRENT_TIMESTAMP())),
    (20, 8, 'RIEGO', DATEADD('DAY', -4, CURRENT_DATE), 1, 1.0, 'Riego previo', DATEADD('HOUR', -96, CURRENT_TIMESTAMP()));
