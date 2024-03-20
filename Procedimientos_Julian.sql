--CREAR PROVEEDOR
CREATE OR REPLACE PROCEDURE crear_proveedor(
    id_proveedor IN INT,
    nombre IN VARCHAR2,
    telefono IN VARCHAR2,
    correo IN VARCHAR2
)
AS 
BEGIN
    INSERT INTO proveedores (id_proveedor, nombre, telefono, correo)
    VALUES (id_proveedor, nombre, telefono, correo);
    COMMIT;
END;

--ACTUALIZAR PROVEEDOR
CREATE OR REPLACE PROCEDURE actualizar_proveedor(
    id_proveedor IN INT,
    nombre IN VARCHAR2,
    telefono IN VARCHAR2,
    correo IN VARCHAR2
)
AS
BEGIN
    UPDATE proveedores
    SET nombre = nombre,
        telefono = telefono,
        correo = correo
    WHERE id_proveedor = id_proveedor;
    COMMIT;
END;

--ELIMINAR PROVEEDOR 
CREATE OR REPLACE PROCEDURE eliminar_proveedor(
    id_proveedor IN INT
)
AS
BEGIN
    DELETE FROM proveedores
    WHERE id_proveedor = id_proveedor;
    COMMIT;
END;


--CREAR RECEPCION
CREATE OR REPLACE PROCEDURE crear_recepcion(
    id_recepcion IN INT,
    id_equipo IN INT,
    id_almacen IN INT,
    fecha IN DATE,
    hora IN TIMESTAMP
)
AS
BEGIN
    INSERT INTO recepciones (id_recepcion, id_equipo, id_almacen, fecha, hora)
    VALUES (id_recepcion, id_equipo, id_almacen, fecha, hora);
    COMMIT;
END;

--ACTUALIZAR RECEPCION
CREATE OR REPLACE PROCEDURE actualizar_recepcion(
    id_recepcion IN INT,
    id_equipo IN INT,
    id_almacen IN INT,
    fecha IN DATE,
    hora IN TIMESTAMP
)
AS
BEGIN
    UPDATE recepciones
    SET id_equipo = id_equipo,
        id_almacen = id_almacen,
        fecha = fecha,
        hora = hora
    WHERE id_recepcion = id_recepcion;
    COMMIT;
END;

--ELIMINAR RECEPCION
CREATE OR REPLACE PROCEDURE eliminar_recepcion(
    id_recepcion IN INT
)
AS
BEGIN
    DELETE FROM recepciones
    WHERE id_recepcion = id_recepcion;
    COMMIT;
END;


--PROCEDIMIENTO PARA ACTUALIZAR LA CANTIDAD DISPONIBLE DE UN EQUIPO
--Ya se incluyo en los procedimientos del CRUD de equipos anteriormente

CREATE OR REPLACE PROCEDURE actualizar_cantidad_disponibles(
    equipo_id IN INT,
    nueva_cantidad IN INT
)
AS
BEGIN
    UPDATE equipos
    SET cantidad_disponible = nueva_cantidad
    WHERE id_equipo = equipo_id;
    COMMIT;
END;


--PROCEDIMIENTO PARA ELIMINAR UN PROVEEDOR Y SUS COMPRAS ASOCIADAS 
--Ya se incluyo en los procedimientos del CRUD de proveedores.

CREATE OR REPLACE PROCEDURE eliminar_proveedor(
    id_proveedor IN INT
)
AS
BEGIN
    DELETE FROM compras
    WHERE id_proveedor = id_proveedor;
    
    DELETE FROM proveedores
    WHERE id_proveedor = id_proveedor;
    COMMIT;
END;


CREATE OR REPLACE PROCEDURE obtener_total_inventario(
    total OUT INT
)
AS
BEGIN
    SELECT COALESCE(SUM(cantidad_disponible), 0)
    INTO total
    FROM equipos;
 
    IF total IS NULL THEN
        total := 0; -- Si no hay registros, establecemos total en 0
    END IF;
END;



--CREAR USUARIOS
CREATE OR REPLACE PROCEDURE crear_usuario(
    id_usuario IN INT,
    id_personal IN INT,
    username IN VARCHAR2,
    password IN VARCHAR2,
    id_rol IN INT
)
AS 
BEGIN
    INSERT INTO usuarios (id_usuarios, id_personal, username, password, id_rol)
    VALUES (id_usuario, id_personal, username, password, id_rol);
    COMMIT;
END;

