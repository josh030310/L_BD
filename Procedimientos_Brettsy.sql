---Procedimientos PROCEDURE

--PROCEDIMIENTO CRUD PARA TODOS LOS EQUIPOS DISPONIBLES

--CREAR EQUIPO
CREATE OR REPLACE PROCEDURE crear_equipo(
    id_equipo IN INT,
    nombre_equipo IN VARCHAR2,
    descripcion IN VARCHAR2,
    num_serie IN VARCHAR2,
    cantidad_disponible IN INT,
    categoria IN INT
)
AS
BEGIN
    INSERT INTO equipos (id_equipo, nombre_equipo, descripcion, num_serie, cantidad_disponible,
    id_categoria)
    VALUES (id_equipo, nombre_equipo, descripcion, num_serie, cantidad_disponible,
    categoria);
      COMMIT;
END;
 

--ACTUALIZAR EQUIPO
CREATE OR REPLACE PROCEDURE actualizar_equipo(
    id_equipo IN INT,
    nombre_equipo IN VARCHAR2,
    descripcion IN VARCHAR2,
    num_serie IN VARCHAR2,
    cantidad_disponible IN INT,
    categoria IN INT
)
AS
BEGIN
    UPDATE equipos
    SET nombre_equipo = nombre_equipo,
        descripcion = descripcion,
        num_serie = num_serie,
        cantidad_disponible = cantidad_disponible,
        id_categoria = categoria
    WHERE id_equipo = id_equipo;
    COMMIT;
END;

--ELIMINAR EQUIPO
CREATE OR REPLACE PROCEDURE eliminar_equipo(
    id_equipo IN INT
)
AS
BEGIN
    DELETE FROM equipos
    WHERE id_equipo = id_equipo;
    COMMIT;
END;
    
--CREAR

CREATE OR REPLACE PROCEDURE crear_departamento(
    id_departamento IN INT,
    nombre_departamento IN VARCHAR2
)
AS
BEGIN
    INSERT INTO departamentos (id_departamento, nombre_departamento)
    VALUES (id_departamento, nombre_departamento);
    COMMIT;
END;

--ACTUALIZAR DEPARTAMENTO
CREATE OR REPLACE PROCEDURE actualizar_departamento(
    id_departamento IN INT,
    nombre_departamento IN VARCHAR2
)
AS
BEGIN
    UPDATE departamentos
    SET nombre_departamento = nombre_departamento
    WHERE id_departamento = id_departamento;
    COMMIT;
END;

--ELIMINAR DEPARTAMENTO
CREATE OR REPLACE PROCEDURE eliminar_departamento(
    id_departamento IN INT
)
AS
BEGIN
    DELETE FROM departamentos
    WHERE id_departamento = id_departamento;
    COMMIT;
END;



--CREAR ROL
CREATE OR REPLACE PROCEDURE crear_rol(
    id_rol IN INT,
    nombre IN VARCHAR2,
    descripcion IN VARCHAR2
)
AS 
BEGIN
    INSERT INTO roles (id_rol, nombre, descripcion)
    VALUES (id_rol, nombre, descripcion);
    COMMIT;
END;

--ACTUALIZAR ROL
CREATE OR REPLACE PROCEDURE actualizar_rol(
    id_rol IN INT,
    nombre IN VARCHAR2,
    descripcion IN VARCHAR2
)
AS 
BEGIN
    UPDATE roles
    SET nombre = nombre,
        descripcion = descripcion
    WHERE id_rol = id_rol;
    COMMIT;
END;


--ELIMINAR ROL
CREATE OR REPLACE PROCEDURE eliminar_rol(
    id_rol IN INT
)
AS
BEGIN
    DELETE FROM roles
    WHERE id_rol = id_rol;
    COMMIT;
END;


