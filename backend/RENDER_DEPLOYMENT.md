# Despliegue en Render.com

Este documento describe cómo desplegar la aplicación NovaBackend en Render.com usando Docker.

## Requisitos previos

1. Una cuenta en [Render.com](https://render.com)
2. Un repositorio Git con el código (GitHub, GitLab, etc.)
3. Una base de datos PostgreSQL (puede ser en Neon.tech, Supabase, etc.)

## Paso 1: Preparar la base de datos

1. Si aún no tienes una base de datos PostgreSQL, crea una en:
   - [Neon.tech](https://neon.tech) (recomendado, gratuito)
   - [Supabase](https://supabase.com)
   - O cualquier proveedor de PostgreSQL

2. Anota los siguientes valores:
   - `DATABASE_URL`: URL de conexión completa
   - `DB_USER`: Usuario de la base de datos
   - `DB_PASSWORD`: Contraseña de la base de datos

## Paso 2: Conectar repositorio a Render.com

1. Inicia sesión en [Render.com](https://render.com)
2. Ve a **Dashboard** > **New +** > **Web Service**
3. Selecciona **Build and deploy from a Git repository**
4. Conecta tu cuenta de GitHub/GitLab
5. Selecciona el repositorio y rama (por defecto `main`)

## Paso 3: Configurar el servicio web

1. **Name**: Ingresa un nombre para tu servicio (ej: `nova-backend`)

2. **Region**: Selecciona la región más cercana (ej: Ohio)

3. **Branch**: Asegúrate que sea la rama correcta (ej: `main`)

4. **Runtime**: Selecciona **Docker**

5. **Build Command**: Dejar vacío (el Dockerfile maneja la compilación)

6. **Start Command**: Dejar vacío (el Dockerfile tiene ENTRYPOINT)

## Paso 4: Configurar variables de entorno

En la sección **Environment Variables**, agrega:

| Key                      | Value                                                  | Tipo    |
| ------------------------ | ------------------------------------------------------ | ------- |
| `DATABASE_URL`           | `jdbc:postgresql://host:port/database?sslmode=require` | Public  |
| `DB_USER`                | Tu usuario de BD                                       | Public  |
| `DB_PASSWORD`            | Tu contraseña de BD                                    | Private |
| `SPRING_PROFILES_ACTIVE` | `production`                                           | Public  |

### Ejemplo de DATABASE_URL para Neon.tech:

```
jdbc:postgresql://ep-jolly-morning-acewwzm7-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require&channelBinding=require
```

## Paso 5: Configuración de puertos

Render.com asignará automáticamente una variable `PORT`. El Dockerfile está configurado para:

- Escuchar en el puerto `${PORT}` (por defecto 8080)
- Render.com inyecta automáticamente esta variable

## Paso 6: Health Check

El Dockerfile usa el endpoint `/actuator/health` como health check. Asegúrate que:

- Management endpoints estén habilitados en `application-production.properties`
- La aplicación responda correctamente

## Paso 7: Desplegar

1. Haz clic en **Create Web Service**
2. Render.com automáticamente:
   - Clonará el repositorio
   - Compilará el Dockerfile
   - Desplegará la aplicación
   - Asignará una URL pública

3. Puedes ver el progreso en el dashboard

## Variables de entorno en Render.com

Render.com proporciona automáticamente:

- `PORT`: Puerto en el que debe escuchar la aplicación
- `RENDER`: Indicador de que se está ejecutando en Render.com

El Dockerfile está configurado para usar estas variables.

## URLs importantes

Una vez desplegada, tu aplicación será accesible en:

```
https://nova-backend.onrender.com
```

Los endpoints será:

```
https://nova-backend.onrender.com/api/...
https://nova-backend.onrender.com/actuator/health
```

## Logs

Para ver los logs en Render.com:

1. Ve a tu servicio en el dashboard
2. Haz clic en **Logs**
3. Filtra por tiempo o error

## Troubleshooting

### Error de conexión a base de datos

- Verifica que `DATABASE_URL`, `DB_USER` y `DB_PASSWORD` sean correctos
- Asegúrate que la BD acepta conexiones SSL
- Comprueba la IP de Render.com esté whitelisted en tu BD

### Aplicación no inicia

- Revisa los logs en Render.com
- Verifica que `application-production.properties` sea válido
- Asegúrate que el Dockerfile compila correctamente localmente:
  ```bash
  docker build -t nova-backend .
  docker run -e DATABASE_URL="..." -e DB_USER="..." -e DB_PASSWORD="..." nova-backend
  ```

### Puerto incorrecto

- Render.com asigna automáticamente `PORT`
- El Dockerfile usa `-Dserver.port=${PORT}`
- No intentes hardcodear el puerto

## Redeploy automático

Cada vez que hagas push a la rama configurada (`main`), Render.com automáticamente:

1. Clona el repositorio
2. Construye la imagen Docker
3. Despliega la nueva versión

## Escalar la aplicación

Si la aplicación crece:

### Plan Free:

- CPU compartida
- 0.5GB RAM
- Se duerme después de 15 minutos sin uso

### Plan Starter ($7/mes):

- CPU dedicada
- 2GB RAM
- Siempre activo

Para cambiar el plan, ve a **Settings** > **Plan**

## Monitoreo

Render.com proporciona:

- Métricas de CPU y memoria
- Logs en tiempo real
- Health checks automáticos
- Redeploy automático si falla

## Backup de base de datos

Asegúrate de hacer backup regular de tu base de datos PostgreSQL:

- Neon.tech: Automatic backups cada 24h
- Configura backups automáticos en tu proveedor

## Actualizaciones

Para actualizar la aplicación:

1. Hace cambios en tu código
2. Commit y push a la rama configurada
3. Render.com automáticamente:
   - Detecta los cambios
   - Reconstruye la imagen
   - Despliega la nueva versión
   - Zero-downtime deployment

## Soporte

- Documentación Render.com: https://render.com/docs
- Ayuda en Discord: https://render.com/chat
