# Guía de Docker - Proyecto PDA

Esta guía explica cómo funciona Docker en este proyecto y cómo puedes usarlo para ejecutar tu aplicación de forma aislada.

## 1. ¿Qué es Docker en este proyecto?

Imagina que quieres que tu aplicación funcione exactamente igual en tu computadora, en la de un colega o en un servidor en la nube. Docker empaqueta tu aplicación con todo lo que necesita (Java, dependencias, configuración) en una **Imagen**.

- **Dockerfile**: Es la "receta" que dice cómo construir la imagen. En este proyecto, usa Maven para compilar el código Java y luego crea una imagen ligera con Java 21 para ejecutarlo.
- **Docker Compose**: Es el "director de orquesta". Permite definir y correr aplicaciones que pueden tener varios servicios (aunque aquí solo tenemos la aplicación `app`).

## 2. Cómo funciona la conexión a la base de datos

Tu aplicación sigue conectándose a **MongoDB Atlas (en la nube)**. Docker no cambia esto; simplemente permite que la aplicación corra dentro de un contenedor y se conecte a internet para llegar a tu base de datos actual.

## 3. Pasos para ejecutar con Docker

### Paso 1: Configurar variables de entorno
He creado un archivo llamado `.env.example`. 
1. Haz una copia de ese archivo y cámbiale el nombre a `.env`.
2. Asegúrate de que las credenciales en `.env` sean las correctas (especialmente el `SPRING_DATA_MONGODB_URI`).

### Paso 2: Construir y Levantar
Abre una terminal en la carpeta raíz del proyecto y ejecuta:

```bash
docker-compose up --build
```

Esto hará lo siguiente:
1. Leerá el `Dockerfile`.
2. Descargará Maven y compilará tu proyecto (`mvn clean package`).
3. Creará una imagen con el archivo `.jar`.
4. Levantará el contenedor en el puerto `8080`.

### Paso 3: Acceder a la aplicación
Una vez que veas el mensaje de que Spring Boot ha iniciado, puedes entrar en:
[http://localhost:8080](http://localhost:8080)

## 5. Solución de Problemas Comunes

### Error: "failed to connect to the docker API"
Si al ejecutar el comando recibes un error que dice que no se puede conectar al daemon de Docker, significa que **Docker Desktop no está abierto**.

**Solución:**
1. Busca "Docker Desktop" en tu menú de inicio de Windows y ábrelo.
2. Espera a que el icono de la ballena en la barra de tareas deje de moverse y se ponga en verde (estado "Running").
3. Una vez abierto, vuelve a intentar el comando `docker-compose up --build`.

### Error: ".env file not found"
Asegúrate de estar ejecutando el comando dentro de la carpeta `Prueba_PDA`. Si estás una carpeta arriba, Docker no encontrará el archivo.

---
> [!TIP]
> Usar Docker evita el clásico "en mi máquina sí funciona", ya que el entorno siempre será idéntico dentro del contenedor.
