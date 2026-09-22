# Acta de Constitución del Proyecto

### Nombre del proyecto: FitTrack Identificador del proyecto: FITTRACK-APP-2026

### Fecha elaboración: 02/09/2026

##### Identificador del proyecto: FITTRACK-APP-2026

##### Versión 1.0

## Contenido

<u>Información del Proyecto</u>

<u>1.- Propósito y Justificación del Proyecto</u>
<u>2.- Descripción del Proyecto</u>
<u>3.- Riesgos principales</u>
<u>4.- Objetivos</u>
<u>5.- Listado de hitos</u>
<u>6.- Presupuesto estimado</u>
<u>7.- Niveles de autoridad del Director del Proyecto</u>
<u>8.- Criterios de aprobación</u>
#### Información del Proyecto

|Empresa/Organización|Proyecto Academico-Uniempresarial|
|---|---|
|Nombre del proyecto|FitTrack|
|Fecha de elaboración|02/09/2026|
|Cliente|Usuarios que realizan entrenamientos en gimnasio|
|Patrocinador principal|N/A|
|Director del proyecto|Diego Rojas-Andres Chiquiza-Brian Alba|

#### 1.- Propósito y Justificación del Proyecto

*El proyecto FitTrack tiene como propósito desarrollar una aplicación móvil que permita a los usuarios* *gestionar y realizar seguimiento de sus entrenamientos de manera organizada, facilitando el registro* *de rutinas, ejercicios, series, repeticiones y progreso físico.*

*El proyecto surge como respuesta a la necesidad de contar con una herramienta sencilla, accesible y* *sobre todo fácil de utilizar que permita centralizar la información relacionada con los* *entrenamientos, evitando depender de registros manuales o de diferentes aplicaciones para llevar el* *control de la actividad física.*

*FitTrack*

##### Identificador del proyecto: FITTRACK-APP-2026

*La realización de FitTrack permitirá aplicar conocimientos de desarrollo de aplicaciones móviles en* *Android Studio utilizando Java, integración de bases de datos locales y remotas, desarrollo y consumo* *de APIs REST, autenticación de usuarios y utilización de recursos del dispositivo móvil. Se espera* *obtener una aplicación funcional, segura y fácil de utilizar, capaz de almacenar y consultar* *información de los entrenamientos tanto localmente como mediante un servidor.*

*Además, el proyecto representa una oportunidad para integrar diferentes tecnologías de desarrollo de* *software en una solución práctica, demostrando el manejo de una arquitectura cliente-servidor y de* *operaciones CRUD sobre una base de datos PostgreSQL.*

#### 2.- Descripción del Proyecto

*FitTrack es una aplicación móvil desarrollada en Android Studio utilizando Java, cuyo objetivo es* *permitir a los usuarios gestionar y realizar seguimiento de sus entrenamientos físicos de manera organizada y sencilla.*

*La aplicación contará con un sistema de registro e inicio de sesión, permitiendo gestionar el acceso de* *los usuarios de forma segura. Una vez autenticado, el usuario podrá administrar sus rutinas y* *ejercicios, registrar sus entrenamientos y consultar su progreso. Entre las principales funcionalidades del proyecto se encuentran:*

- ***Registro e inicio de sesión de usuarios**, con mecanismos de autenticación y protección de contraseñas.*
- ***Gestión de rutinas de entrenamiento**, permitiendo crear, consultar, actualizar y eliminar rutinas.*
- ***Gestión de ejercicios**, incluyendo información como nombre, grupo muscular, series, repeticiones y peso.*
- ***Registro del progreso**, permitiendo almacenar información de los entrenamientos realizados.*
- ***Almacenamiento local** mediante SQLite/Room para conservar determinados datos, como las* *rutinas creadas y permitir el acceso a información sin conexión. ● **Almacenamiento de preferencias** mediante SharedPreferences o DataStore, como* *configuraciones y datos relacionados con la sesión.*
- ***Uso de recursos del dispositivo móvil**, como la cámara para registrar fotografías de progreso* *y el almacenamiento de archivos para conservar contenido generado por el usuario.*
- ***Comunicación con una API REST**, desarrollada mediante Spring Boot, encargada de gestionar* *la información almacenada en una base de datos PostgreSQL.*
- ***Operaciones CRUD** sobre diferentes entidades de la aplicación, como usuarios, ejercicios, rutinas y registros de entrenamiento.*
- ***Sincronización de información** entre la aplicación móvil y el servidor cuando exista conexión a Internet.*
*FitTrack*

##### Identificador del proyecto: FITTRACK-APP-2026

*El proyecto estará orientado a ofrecer una interfaz sencilla e intuitiva, facilitando la navegación y el registro de información.*

*La arquitectura estará basada en un modelo **cliente-servidor**, donde la aplicación Android funcionará* *como cliente y consumirá los servicios proporcionados por la API desarrollada en Spring Boot, mientras* *que PostgreSQL será utilizado como sistema de gestión de la base de datos del servidor.*

#### 3.- Riesgos principales

Durante el desarrollo e implementación del proyecto FitTrack pueden presentarse diferentes eventos o condiciones que afecten el cumplimiento de los objetivos establecidos. Los principales riesgos identificados son:

- **Problemas de integración:** Errores en la comunicación entre la aplicación Android, la API de Spring Boot y la base de datos PostgreSQL.
- **Pérdida de datos:** Fallos en el almacenamiento local o remoto que puedan provocar la pérdida de información de usuarios, rutinas o entrenamientos. ● **Problemas de seguridad:** Errores en la autenticación o almacenamiento de contraseñas que puedan comprometer la información de los usuarios.
- **Falta de tiempo:** Retrasos en el desarrollo debido a la cantidad de funcionalidades y tecnologías requeridas.
- **Problemas de conexión:** Dificultades para enviar o consultar información del servidor cuando el dispositivo no tenga acceso a Internet.
- **Errores en los CRUD:** Fallos en las operaciones de creación, consulta, actualización o eliminación de los datos.
- **Incompatibilidad con dispositivos:** Diferencias entre versiones de Android o dispositivos que puedan afectar funcionalidades como la cámara o el almacenamiento.
*FitTrack*

##### Identificador del proyecto: FITTRACK-APP-2026

#### 4.- Objetivos

##### Objetivo General:

Desarrollar una aplicación móvil para dispositivos Android que facilite la gestión y seguimiento de rutinas, ejercicios y entrenamientos, con el fin de permitir a los usuarios organizar sus actividades y controlar su progreso, mediante una arquitectura cliente-servidor con Java, Spring Boot, PostgreSQL y API REST, durante un periodo de 8 semanas

##### Objetivos Específicos:

- Implementar un sistema de autenticación de usuarios que permita el registro e inicio de sesión de forma segura.
- Desarrollar al menos 3 módulos CRUD funcionales para la gestión de información relacionada con usuarios, ejercicios y rutinas de entrenamiento.
- Implementar una base de datos local mediante SQLite/Room para almacenar información de manera persistente en el dispositivo.
- Desarrollar una API REST con Spring Boot y PostgreSQL que permita gestionar y consultar la información utilizada por la aplicación móvil.
- Integrar al menos 2 recursos del dispositivo móvil, como la cámara y el almacenamiento de archivos, aplicándolos a funcionalidades relacionadas con el seguimiento del entrenamiento.
- Implementar mecanismos de almacenamiento de preferencias mediante SharedPreferences o DataStore para conservar configuraciones y datos necesarios de la aplicación.
#### 5.- Listado de hitos-Diagrama gantt

|Actividad / Hito|S1|S2|S3|S4|S5|S6|S7|S8|
|---|---|---|---|---|---|---|---|---|
|Análisis de requerimientos|X|X|||||||
|Diseño de la aplicación||X|X||||||
|Diseño de BD y API|||X|X|||||
|Desarrollo Android||||X|X|X|||
|Desarrollo API + PostgreSQL Integración Android + API Pruebas y correcciones||||X|X|X X|X X|X|
|Entrega final||||||||X|

*FitTrack*

##### Identificador del proyecto: FITTRACK-APP-2026

#### 6.- Presupuesto estimado

El presupuesto del proyecto FitTrack se establece teniendo en cuenta que tendrá una duración de 8 semanas, equivalentes aproximadamente a dos meses. Durante este periodo se realizarán las actividades de análisis, diseño, desarrollo, integración, pruebas y entrega final.

El equipo estará conformado por tres desarrolladores Pre-Junior. El costo estimado por cada desarrollador durante las 8 semanas del proyecto es de **$2.800.000** COP, para un total de **$8.400.000** COP.

Adicionalmente, se contempla una única suscripción compartida a una herramienta de inteligencia artificial durante los dos meses de desarrollo, con un costo estimado de $200.000 COP.

|Concepto|Cantidad|Costo|
|---|---|---|
|Desarrollador Pre-Junior|3 desarrolladores durante 2 meses|$8.400.000 COP|
|Suscripción de IA|1 suscripción durante 2 meses|$200.000 COP|
|Herramientas de desarrollo|Herramientas gratuitas|$0 COP|
|Costo total estimado|-|$8.600.000 COP|

El costo de **$2.800.000** COP corresponde al valor total estimado por cada desarrollador durante los dos *meses del proyecto, no a un valor mensual. El desarrollo se realizará localmente, por lo que no se* *contemplan gastos de hosting, dominio ni servicios de producción.*

#### 7- Aprobaciones

#### ______________________________ ______________________________ Firma del Director del Proyecto Firma del Patrocinador

*FitTrack*

##### Identificador del proyecto: FITTRACK-APP-2026

#### ______________________________ ______________________________ Nombre Director del Proyecto Nombre del Patrocinador

*FitTrack*
