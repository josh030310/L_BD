--ACTUALIZAR USUARIO
CREATE OR REPLACE PROCEDURE actualizar_usuario(
    id_usuario IN INT,
    id_personal IN INT, 
    username IN VARCHAR2,
    password IN VARCHAR2,
    id_rol IN INT
)
AS
BEGIN
    UPDATE usuarios
    SET id_personal = id_personal,
        username = username,
        password = password,
        id_rol = id_rol
    WHERE id_usuarios = id_usuario;
    COMMIT;
END;

--ELIMINAR USUARIO
CREATE OR REPLACE PROCEDURE eliminar_usuario(
    id_usuario IN INT
)
AS 
BEGIN
    DELETE FROM usuarios
    WHERE id_usuario = id_usuario;
    COMMIT;
END;

--PROCEDIMIENTO PARA OBTENER LA CANTIDAD TOTAL DE EQUIPOS POR CATEGORIA

CREATE OR REPLACE PROCEDURE obtener_cantidad_equipos_por_categoria(
    categoria_id IN INT,
    cantidad_equipos OUT INT
)
AS
BEGIN
    SELECT COUNT(*)
    INTO cantidad_equipos
    FROM equipos
    WHERE id_categoria = categoria_id;
END;

--CREAR CATEGORIA
CREATE OR REPLACE PROCEDURE crear_categoria(
    id_categoria IN INT,
    nombre_categoria IN VARCHAR2
)
AS
BEGIN
    INSERT INTO categorias (id_categoria, nombre_categoria)
    VALUES (id_categoria, nombre_categoria);
    COMMIT;
END;

--ACTUALIZAR CATEGORIA
CREATE OR REPLACE PROCEDURE actualizar_categoria(
    id_categoria IN INT,
    nombre_categoria IN VARCHAR2
)
AS
BEGIN
    UPDATE categorias
    SET nombre_categoria = nombre_categoria
    WHERE id_categoria = id_categoria;
    COMMIT;
END;

--ELIMINAR CATEGORIA
CREATE OR REPLACE PROCEDURE eliminar_categoria(
    id_categoria IN INT
)
AS
BEGIN
    DELETE FROM categorias
    WHERE id_categoria = id_categoria;
    COMMIT;
END;

--CREAR DETALLE COMPRA
CREATE OR REPLACE PROCEDURE crear_detalle_compra(
    id_detalle_compra IN INT,
    material IN VARCHAR2,
    cantidad IN INT,
    precio_unitario IN DECIMAL
)
AS 
BEGIN
    INSERT INTO detalle_compras (id_detalle_compra, material, cantidad, precio_unitario)
    VALUES (id_detalle_compra, material, cantidad, precio_unitario);
    COMMIT;
END;

--ACTUALIZAR DETALLE COMPRA
CREATE OR REPLACE PROCEDURE actualizar_detalle_compra(
    id_detalle_compra IN INT,
    material IN VARCHAR2,
    cantidad IN INT,
    precio_unitario IN DECIMAL
)
AS
BEGIN
    UPDATE detalle_compras
    SET material = material,
        cantidad = cantidad,
        precio_unitario = precio_unitario
    WHERE id_detalle_compra = id_detalle_compra;
    COMMIT;
END;



--ELIMINAR DETALLE COMPRA
CREATE OR REPLACE PROCEDURE eliminar_detalle_compra(
    id_detalle_compra IN INT
)
AS
BEGIN
    DELETE FROM detalle_compras
    WHERE id_detalle_compra = id_detalle_compra;
    COMMIT;
END;












