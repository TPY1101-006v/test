# Monitoriza

**Monitor ambiental inteligente para aulas escolares.**

Monitoriza es una plataforma full-stack que recopila lecturas de sensores (temperatura, humedad, ruido, iluminación, CO₂ y TVOC), las visualiza en tiempo real, genera alertas cuando los valores salen del rango ideal y produce informes diarios con análisis de IA mediante **Google Gemini**.

---

## Inicio rápido

Antes de ejecutar los comandos, asegúrate de tener instalado lo siguiente.

### Instalaciones previas

| Dependencia | Versión mínima | ¿Para qué? | Cómo verificar |
|-------------|----------------|------------|----------------|
| **Java JDK** | 17 | Compilar y ejecutar el backend (Spring Boot) | `java -version` |
| **Node.js** | 18+ | Ejecutar el frontend (React + Vite) | `node -v` y `npm -v` |
| **MySQL Server** | 8.x | Base de datos del proyecto | Servicio MySQL activo en el puerto 3306 |
| **Cuenta Google** | — | Obtener API key de Gemini en AI Studio | [aistudio.google.com/apikey](https://aistudio.google.com/apikey) |
| **Maven** | 3.8+ *(opcional)* | Solo si **no** usas el Maven Wrapper incluido | `mvn -v` |

#### Enlaces de descarga

| Herramienta | Windows | macOS |
|-------------|---------|-------|
| Java 17 (JDK) | [Adoptium Temurin 17](https://adoptium.net/temurin/releases/?version=17) | [Adoptium Temurin 17](https://adoptium.net/temurin/releases/?version=17) |
| Node.js | [nodejs.org](https://nodejs.org/) | [nodejs.org](https://nodejs.org/) o `brew install node` |
| MySQL | [MySQL Installer](https://dev.mysql.com/downloads/installer/) | [MySQL DMG](https://dev.mysql.com/downloads/mysql/) o `brew install mysql` |
| Maven *(opcional)* | [Apache Maven](https://maven.apache.org/download.cgi) | `brew install maven` |

> **Nota sobre Maven:** el proyecto incluye **Maven Wrapper** en la carpeta `backend/` (`mvnw.cmd` en Windows y `mvnw` en macOS/Linux). Si no tienes Maven instalado globalmente, usa el wrapper — no necesitas instalar Maven por separado.

---

Necesitas **3 terminales** (o pestañas): una para MySQL, otra para el backend y otra para el frontend.

### 1. Base de datos (MySQL)

Asegúrate de que MySQL esté en ejecución y crea la base de datos:

```sql
CREATE DATABASE IF NOT EXISTS monitoriza_db;
```

> Ajusta usuario y contraseña en `backend/src/main/resources/application.properties` si no usas `root`.

---

### 2. Backend (Spring Boot — puerto 8080)

Elige los comandos según tu sistema operativo. Todos se ejecutan desde la carpeta `backend/`.

#### Windows — con Maven instalado

```powershell
cd backend
mvn clean install
$env:GEMINI_API_KEY="tu_api_key_aqui"
mvn spring-boot:run
```

#### Windows — sin Maven (usa Maven Wrapper)

Si al escribir `mvn` obtienes *"mvn no se reconoce como comando"*, usa el wrapper incluido en el proyecto:

```powershell
cd backend
.\mvnw.cmd clean install
$env:GEMINI_API_KEY="tu_api_key_aqui"
.\mvnw.cmd spring-boot:run
```

#### macOS / Linux — con Maven instalado

```bash
cd backend
mvn clean install
export GEMINI_API_KEY="tu_api_key_aqui"
mvn spring-boot:run
```

#### macOS / Linux — sin Maven (usa Maven Wrapper)

```bash
cd backend
chmod +x mvnw          # solo la primera vez, si el archivo no es ejecutable
./mvnw clean install
export GEMINI_API_KEY="tu_api_key_aqui"
./mvnw spring-boot:run
```

**Verificar:** abre [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

### 3. Frontend (React + Vite — puerto 5173)

Funciona igual en Windows, macOS y Linux (nueva terminal):

```bash
cd frontend
npm install      # solo la primera vez
npm run dev
```

**Verificar:** abre [http://localhost:5173](http://localhost:5173)

---

## API Key de Google AI Studio (obligatoria)

Monitoriza usa **Google Gemini** para dos funciones principales:

| Función | Descripción |
|---------|-------------|
| **Informes diarios** | Análisis automático del ambiente del aula con recomendaciones |
| **Chat asistente** | Bot en la interfaz que responde preguntas sobre sensores e informes |

Sin una API key válida, el backend arranca pero **fallarán los informes con IA y el chatbot**.

### Cómo obtener tu API key

1. Entra a **[Google AI Studio](https://aistudio.google.com/apikey)** con tu cuenta de Google.
2. Haz clic en **Create API key** (Crear clave de API).
3. Selecciona un proyecto de Google Cloud (o crea uno nuevo).
4. Copia la clave generada. **Guárdala en un lugar seguro; no la compartas ni la subas a Git.**

### Cómo configurarla en el proyecto

El backend lee la variable de entorno `GEMINI_API_KEY`. Elige **una** de estas opciones:

#### Opción A — Variable de entorno (recomendada)

**Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY="AIzaSy..."
mvn spring-boot:run          # o .\mvnw.cmd spring-boot:run si no tienes Maven
```

**Windows (CMD):**
```cmd
set GEMINI_API_KEY=AIzaSy...
mvn spring-boot:run
REM o: mvnw.cmd spring-boot:run
```

**macOS / Linux:**
```bash
export GEMINI_API_KEY="AIzaSy..."
mvn spring-boot:run          # o ./mvnw spring-boot:run si no tienes Maven
```

#### Opción B — Archivo `.env` local (referencia)

Copia el ejemplo incluido en el backend:

```powershell
cd backend
copy .env.example .env
```

Edita `.env` y pega tu clave. Luego **exporta la variable manualmente** antes de arrancar (Spring Boot no carga `.env` automáticamente):

```powershell
$env:GEMINI_API_KEY=(Get-Content .env | Where-Object { $_ -match '^GEMINI_API_KEY=' } | ForEach-Object { $_ -split '=', 2 } | Select-Object -Last 1)
.\mvnw.cmd spring-boot:run
```

#### Opción C — IDE (IntelliJ / VS Code)

Agrega `GEMINI_API_KEY=tu_clave` en la configuración de ejecución de **MonitorizaApplication**.

### Modelo y límites

| Propiedad | Valor por defecto | Archivo |
|-----------|-------------------|---------|
| Modelo Gemini | `gemini-2.5-flash` | `application.properties` |
| Mensajes de chat por día | 30 | `application.properties` |
| Mensajes de chat por minuto | 3 | `application.properties` |

El tier gratuito de Gemini tiene límites de solicitudes por minuto (RPM). El proyecto incluye pausas automáticas al generar informes históricos para evitar errores `429`.

> **Importante:** nunca subas la API key al repositorio. El archivo `.env` ya está en `.gitignore`.

---

## ¿Qué hace el proyecto?

```
┌─────────────┐     HTTP/REST      ┌──────────────────┐     JDBC      ┌────────┐
│  Frontend   │ ◄───────────────► │  Backend Spring  │ ◄───────────► │ MySQL  │
│  React/Vite │   localhost:5173   │  Boot :8080      │               │        │
└─────────────┘                    └────────┬─────────┘               └────────┘
                                            │
                                   ┌────────▼─────────┐
                                   │  Google Gemini   │
                                   │  (informes+chat) │
                                   └──────────────────┘
                                            ▲
                                   ┌────────┴─────────┐
                                   │  ESP8266 /       │
                                   │  Simulador       │
                                   └──────────────────┘
```

### Sensores monitoreados

| Sensor | Unidad | Rango ideal |
|--------|--------|-------------|
| Temperatura | °C | 20 – 22 |
| Humedad | % | 40 – 60 |
| Decibeles | dBA | ≤ 45 (referencia: ≤ 35 vacía) |
| Iluminación | lx | 300 – 500 |
| CO₂ | ppm | 400 – 800 |
| TVOC | ppb | 0 – 220 |

### Funcionalidades principales

- **Dashboard en tiempo real** — tarjetas y gráficos con las últimas 20 mediciones (actualización cada 5 minutos).
- **Alertas automáticas** — detección de valores fuera de rango con historial persistente.
- **Condiciones de sala** — configuración de m², estudiantes, ventanas, ventilación y aire acondicionado.
- **Informes PDF diarios** — generados con IA, descargables desde el panel lateral.
- **Exportación CSV** — descarga de todas las mediciones registradas.
- **Chat asistente IA** — consultas sobre sensores, mediciones del día e informes del mes.
- **Simulador de mediciones** — datos de prueba automáticos si no hay hardware conectado.
- **Documentación API** — Swagger UI integrado.

---

## Estructura del repositorio

```
test/
├── backend/                    # API REST — Spring Boot 4 + Java 17
│   ├── src/main/java/cl/duoc/monitoriza/
│   │   ├── config/             # Schedulers, Gemini, datos iniciales
│   │   ├── controller/         # Endpoints REST
│   │   ├── dto/                # Objetos de transferencia
│   │   ├── model/              # Entidades JPA
│   │   ├── repository/         # Acceso a datos
│   │   ├── service/            # Lógica de negocio e IA
│   │   └── util/               # Rangos ambientales, horarios escolares
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── templates/informe-pdf.html
│   └── .env.example            # Plantilla para la API key
│
└── frontend/                   # Interfaz web — React 18 + Vite 5
    └── src/
        ├── components/         # UI: sensores, gráficos, chat, sidebar
        ├── hooks/              # useSensors — polling de datos
        └── utils/              # API client y constantes
```

---

## Configuración del backend

Archivo principal: `backend/src/main/resources/application.properties`

| Propiedad | Descripción | Valor por defecto |
|-----------|-------------|-------------------|
| `spring.datasource.url` | Conexión MySQL | `jdbc:mysql://localhost:3306/monitoriza_db` |
| `spring.jpa.hibernate.ddl-auto` | Esquema de BD | `create` (recrea tablas al iniciar) |
| `app.simulador.mediciones.enabled` | Simulador cada 5 min | `true` |
| `app.seed.historico.enabled` | Datos históricos de prueba | `true` |
| `app.seed.informes.enabled` | Informes históricos al arrancar | `true` |
| `app.informe.diario.enabled` | Informe automático lun–vie 16:35 | `true` |
| `app.chat.enabled` | Chatbot IA | `true` |
| `gemini.api-key` | Clave de Gemini | `${GEMINI_API_KEY:}` |
| `gemini.model` | Modelo de IA | `gemini-2.5-flash` |

> **Producción:** cambia `ddl-auto` a `validate` o `update` y desactiva el simulador cuando conectes un ESP8266 real vía `POST /api/mediciones`.

### Activar o desactivar tareas programadas (schedulers)

El backend usa **Spring `@Scheduled`** para generar datos automáticamente. Cada tarea se controla con una propiedad en `application.properties`. Pon `true` para activarla o `false` para desactivarla; **reinicia el backend** después de cambiar el valor.

| Propiedad | Clase | Qué hace | Cuándo se ejecuta | Valor por defecto |
|-----------|-------|----------|-------------------|-------------------|
| `app.simulador.mediciones.enabled` | `MedicionSimuladorScheduler` | Inserta una medición ficticia en la BD | Cada **5 minutos** mientras el backend está en marcha | `true` |
| `app.informe.diario.enabled` | `InformeDiarioScheduler` | Genera el informe PDF del día con IA | **16:35**, lunes a viernes | `true` |

#### Ejemplo: desactivar simulador (ESP8266 real)

Edita `backend/src/main/resources/application.properties`:

```properties
app.simulador.mediciones.enabled=false
```

O pásalo al arrancar sin editar el archivo (variable de entorno, funciona en Windows y macOS/Linux):

```powershell
# Windows (PowerShell)
$env:APP_SIMULADOR_MEDICIONES_ENABLED="false"
.\mvnw.cmd spring-boot:run
```

```bash
# macOS / Linux
export APP_SIMULADOR_MEDICIONES_ENABLED=false
./mvnw spring-boot:run
```

#### Ejemplo: desactivar informe diario automático

```properties
app.informe.diario.enabled=false
```

Útil si solo quieres generar informes manualmente desde la API o al arrancar con el seed histórico.

#### Generación al arrancar (no es `@Scheduled`, pero relacionada)

Estas opciones corren **una vez** al iniciar la aplicación (`DataInitializer`), no en intervalos:

| Propiedad | Qué hace | Valor por defecto |
|-----------|----------|-------------------|
| `app.seed.historico.enabled` | Rellena ~1 mes de mediciones de prueba si la tabla está vacía | `true` |
| `app.seed.informes.enabled` | Genera informes históricos con IA para los días del seed | `true` |

Para arrancar con BD limpia sin datos ni informes de prueba:

```properties
app.seed.historico.enabled=false
app.seed.informes.enabled=false
```

#### Escenarios recomendados

| Escenario | Simulador | Informe diario | Seed histórico | Seed informes |
|-----------|-----------|----------------|----------------|---------------|
| Desarrollo / demo local | `true` | `true` | `true` | `true` |
| ESP8266 real en producción | `false` | `true` | `false` | `false` |
| Solo consultar API sin generar datos | `false` | `false` | `false` | `false` |

> **Nota:** aunque desactives una tarea, el scheduler sigue registrado en Spring; la propiedad en `false` hace que el método **salga sin ejecutar** la generación. En consola verás mensajes como `[Simulador] Medición guardada...` solo cuando el simulador está activo.

---

## Endpoints principales

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/mediciones/ultimas` | Últimas 20 mediciones |
| `POST` | `/api/mediciones` | Registrar medición (ESP8266) |
| `GET` | `/api/alertas` | Historial de alertas |
| `GET` | `/api/informes` | Listado de informes |
| `GET` | `/api/informes/{id}/pdf` | Descargar informe en PDF |
| `GET` | `/api/report` | Exportar mediciones CSV |
| `POST` | `/api/chat` | Enviar mensaje al asistente IA |
| `GET` | `/api/salas` | Condiciones del aula |
| `PUT` | `/api/salas/{id}` | Actualizar condiciones |

Documentación interactiva completa: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## Comandos útiles

```bash
# Backend — ejecutar tests
cd backend
mvn test                    # Windows sin Maven: .\mvnw.cmd test
                            # macOS/Linux sin Maven: ./mvnw test

# Backend — compilar JAR
mvn package -DskipTests     # o .\mvnw.cmd / ./mvnw package -DskipTests

# Frontend — build de producción
cd frontend
npm run build
npm run preview
```

---

## Solución de problemas

| Problema | Posible causa | Solución |
|----------|---------------|----------|
| Error de conexión MySQL | BD no creada o credenciales incorrectas | Verifica MySQL y `application.properties` |
| Chat responde con error | API key ausente o inválida | Configura `GEMINI_API_KEY` y reinicia el backend |
| Error 429 en informes | Límite de RPM de Gemini | Espera unos minutos; el seed ya incluye pausas |
| Frontend sin datos | Backend apagado | Asegúrate de que Spring Boot esté en `:8080` |
| CORS bloqueado | Frontend en otro puerto | El backend acepta `http://localhost:5173` por defecto |
| BD vacía al reiniciar | `ddl-auto=create` | Normal en desarrollo; usa `update` si quieres persistir esquema |
| `mvn` no reconocido | Maven no instalado | Usa `.\mvnw.cmd` (Windows) o `./mvnw` (macOS/Linux) desde `backend/` |
| `Permission denied` en Mac | `mvnw` sin permisos | Ejecuta `chmod +x mvnw` dentro de `backend/` |

---

## Stack tecnológico

**Backend:** Spring Boot 4 · Spring Data JPA · MySQL · Thymeleaf · OpenHTMLtoPDF · SpringDoc OpenAPI · Google Gemini API

**Frontend:** React 18 · Vite 5 · Recharts · CSS Modules

---

## Licencia

Proyecto académico — Duoc UC. Uso educativo y demostrativo.
