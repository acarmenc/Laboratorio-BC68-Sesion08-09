
# Laboratorio BC68 - Sesión 08 – 09

## **Objetivo**

Construir un microservicio “transactions-service” que:
1. exponga endpoints reactivos (WebFlux) para crear y listar movimientos de
cuenta,
2. persista cuentas y movimientos en MongoDB reactivo,
3. consulte un módulo legado (JPA + H2 en memoria) para reglas de riesgo
(bloqueante) y lo encapsule reactivamente con
Schedulers.boundedElastic(),
4. incluya manejo de errores consistente y un stream SSE de movimientos.

## **Endpoints**
| Método | Endpoint            | Descripción |
|--------|---------------------|-------------|
| POST    | `/api/transactions`  | Permite realizar transacciones. |
| GET    | `/api/transactions?accountNumber`         | Permite obtener las transacsciones por numero de cuenta |
| GET    | `/api/stream/transactions`  | Permite obtener el stream de transacciones |

## **Configuración y Ejecución**

### **1. Clonar el repositorio**
```
git clone https://github.com/acarmenc/Laboratorio-BC68-Sesion08-09.git
```
### **2. Iniciar instancia de MongoDb**

### **3. Iniciar aplicacion**
```
mvn spring-boot:run
```

## **Herramientas utilizadas**
- **Java 17**
- **Maven 3.9.11**
- **MongoDb local**
- **IntelliJ IDEA**
- **Postman**
- **Git**