
# Laboratorio BC68 - Sesion 10 – 11

## **Objetivo**

Sobre el microservicio ya construido (WebFlux + Mongo reactivo + módulo legado
JPA/H2):
1. Activar y endurecer: Log4j2 (JSON + MDC), Jacoco con umbrales,
   Checkstyle, SonarQube.
2. Patrones: añadir Circuit Breaker + Retry + TimeLimiter (Resilience4j)
   hacia un cliente remoto de riesgo con fallback al módulo legado JPA
   envuelto en boundedElastic().
3. Validar con Postman (nada de curl).

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

### **4. Iniciar Sonarqube**
```
docker run -d --name sonar -p 9001:9000 sonarqube:-community
```

### **5. Generar reporte jacoco y checkstule**
```
mvn clean install
```

### **6. Generar reporte sonarqube**
```
mvn clean verify sonar:sonar
```

## **Herramientas utilizadas**
- **Java 17**
- **Maven 3.9.11**
- **Sonarqube community (Docker image)**
- **Jacoco**
- **Checkstyle**
- **MongoDb local**
- **IntelliJ IDEA**
- **Postman**
- **Git**