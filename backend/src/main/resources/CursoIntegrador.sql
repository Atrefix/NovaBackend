-- (1) TABLA Cuenta
CREATE TABLE Cuenta (
    IdCuenta        SERIAL PRIMARY KEY,
    Email           VARCHAR(150) NOT NULL UNIQUE,
    Password        VARCHAR(180) NOT NULL,
    Rol             VARCHAR(50),
    Estado          VARCHAR(20),
    FechaRegistro   TIMESTAMP
);

-- (2) TABLA ClientePersona
CREATE TABLE ClientePersona (
    IdClientePersona SERIAL PRIMARY KEY,
    IdCuenta         INTEGER REFERENCES Cuenta(IdCuenta) ON DELETE SET NULL,
    DNI              VARCHAR(8) UNIQUE,
    Nombre           VARCHAR(80),
    Apellido         VARCHAR(80),
    Direccion        VARCHAR(200),
    Telefono         VARCHAR(30)
);

-- (3) TABLA ClienteEmpresa
CREATE TABLE ClienteEmpresa (
    IdClienteEmpresa SERIAL PRIMARY KEY,
    IdCuenta         INTEGER REFERENCES cuenta(IdCuenta) ON DELETE SET NULL,
    RUC              VARCHAR(20) UNIQUE,
    RazonSocial      VARCHAR(150),
    Direccion        VARCHAR(200),
    Telefono         VARCHAR(30)
);

-- (4) TABLA Cliente
CREATE TABLE Cliente (
    IdCliente         SERIAL PRIMARY KEY,
    TipoCliente       VARCHAR(20) NOT NULL, -- 'persona' / 'empresa'
    IdClientePersona  INTEGER REFERENCES ClientePersona(IdClientePersona) ON DELETE SET NULL,
    IdClienteEmpresa  INTEGER REFERENCES ClienteEmpresa(IdClienteEmpresa) ON DELETE SET NULL
);

-- (5) TABLA Empleado
CREATE TABLE Empleado (
    IdEmpleado        SERIAL PRIMARY KEY,
    IdCuenta          INTEGER REFERENCES cuenta(idcuenta) ON DELETE SET NULL,
    Nombre            VARCHAR(80) NOT NULL,
    Apellido          VARCHAR(80) NOT NULL,
    Cargo             VARCHAR(60),
    FechaContratacion TIMESTAMP
);

-- (6) TABLA Sucursal
CREATE TABLE Sucursal (
    IdSucursal  SERIAL PRIMARY KEY,
    Nombre      VARCHAR(100) NOT NULL,
    Direccion   VARCHAR(200)
);

-- (7) TABLA Caja
CREATE TABLE Caja (
    IdCaja        SERIAL PRIMARY KEY,
    IdSucursal    INTEGER REFERENCES Sucursal(IdSucursal) ON DELETE CASCADE,
    FechaApertura TIMESTAMP,
    FechaCierre   TIMESTAMP,
    Estado        VARCHAR(20)
);

-- (8) TABLA Producto
CREATE TABLE Producto (
    IdProducto       SERIAL PRIMARY KEY,
    CodProducto      VARCHAR(30) UNIQUE NOT NULL,
    Nombre           VARCHAR(100) NOT NULL,
    Categoria        VARCHAR(60),
    Stock            INTEGER DEFAULT 0 CHECK (stock >= 0),
    PrecioCompra     NUMERIC(12,2),
    PrecioVenta      NUMERIC(12,2),
    FechaVencimiento DATE
);

-- (9) TABLA Venta
CREATE TABLE Venta (
    IdVenta           SERIAL PRIMARY KEY,
    CodVenta          VARCHAR(40) UNIQUE,
    TipoComprobante   VARCHAR(40),
    FechaEmision      TIMESTAMP DEFAULT now(),
    CantidadProductos INTEGER,
    Total             NUMERIC(12,2),
    FormaPago         VARCHAR(40),
    IdCaja            INTEGER REFERENCES Caja(IdCaja) ON DELETE SET NULL,
    IdEmpleado        INTEGER REFERENCES Empleado(IdEmpleado) ON DELETE SET NULL,
    IdCliente         INTEGER REFERENCES Cliente(IdCliente) ON DELETE SET NULL
);

-- (10) TABLA DetalleVenta
CREATE TABLE DetalleVenta (
    IdDetalle  SERIAL PRIMARY KEY,
    IdVenta    INTEGER REFERENCES Venta(IdVenta) ON DELETE CASCADE,
    IdProducto INTEGER REFERENCES Producto(IdProducto) ON DELETE RESTRICT,
    Cantidad   INTEGER NOT NULL CHECK (Cantidad > 0),
    Subtotal   NUMERIC(12,2) NOT NULL
);

--(11) TABLA Notificaciones
CREATE TABLE Notificaciones(
	IdNotificaciones SERIAL PRIMARY KEY,
    IdCuenta         INTEGER REFERENCES Cuenta(IdCuenta) ON DELETE SET NULL,
	Asunto			 VARCHAR(50),
	Descripcion 	 VARCHAR(300),
	FechaHoraEnvio   TIMESTAMP DEFAULT NOW()
);



---------------TRIGGER---------------

CREATE TRIGGER trg_bienvenida_notificacion
AFTER INSERT ON Cuenta 
FOR EACH ROW          
EXECUTE FUNCTION bienvenida_notificacion_func();

---------------FUNCIONES---------------

CREATE OR REPLACE FUNCTION bienvenida_notificacion_func()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO Notificaciones (
        IdCuenta,
        Asunto,
        Descripcion,
        FechaHoraEnvio
    )
    VALUES (
        NEW.IdCuenta, 
        '¡Bienvenido/a a nuestra Cafetería!',
        'Gracias por registrarte. Tu cuenta está activa y lista para ser usada ♡.',
        NOW()
    );

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

---------------------------------------------------------------------------------------------------

DROP FUNCTION obtener_datos_usuario_por_email;

CREATE OR REPLACE FUNCTION obtener_datos_usuario_por_email(p_email VARCHAR)
RETURNS TABLE (
    nombre_usuario VARCHAR,
    email_cuenta VARCHAR,
    contrasena VARCHAR
)
AS $$
BEGIN
    RETURN QUERY
    SELECT
        (CASE
            WHEN E.Nombre IS NOT NULL THEN E.Nombre || ' ' || E.Apellido 
            WHEN CE.RazonSocial IS NOT NULL THEN CE.RazonSocial 
            ELSE 'No Asociado'
        END)::VARCHAR AS nombre_usuario,
        C.Email AS email_cuenta,
        C.Password AS contrasena
    FROM 
        Cuenta C
    LEFT JOIN 
        Empleado E ON C.IdCuenta = E.IdCuenta
    LEFT JOIN 
        ClientePersona CP ON C.IdCuenta = CP.IdCuenta
    LEFT JOIN 
        ClienteEmpresa CE ON C.IdCuenta = CE.IdCuenta
    WHERE 
        C.Email = p_email
    LIMIT 1;
END;
$$ LANGUAGE plpgsql;

-------------Procedimiento Almacenado-------------
CREATE OR REPLACE PROCEDURE sp_crear_nueva_cuenta(
    p_email VARCHAR,
    p_password VARCHAR,
    p_rol VARCHAR,
    p_estado VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO Cuenta (
        Email,
        Password,
        Rol,
        Estado,
        FechaRegistro
    )
    VALUES (
        p_email,
        p_password,
        p_rol,
        p_estado,
        NOW()
    );
    
END;
$$;

---------------Prueba---------------

SELECT * FROM obtener_datos_usuario_por_email('pedrito@gmail.com') t;
truncate table notificaciones
select * from cuenta
select * from notificaciones
