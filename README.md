# Backend Gestor Finanzas

Proyecto Spring Boot (Puerto 3030) para gestionar usuarios, grupos, bolsillos, categorías, ingresos y egresos.

## Requisitos
- Java 17
- MySQL 8+
- Maven (opcional si usas `mvnw.cmd`)

## Configuración
Edita `src/main/resources/application.properties` con tus credenciales reales:
```
spring.datasource.url=jdbc:mysql://localhost:3306/finanzas?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=Password123!
```

`spring.jpa.hibernate.ddl-auto=update` creará/alterará tablas automáticamente según las entidades.

## Ejecutar
Windows PowerShell:
```
./mvnw.cmd spring-boot:run
```
La API quedará en: `http://localhost:3030`

## Endpoints Principales
| Recurso | Endpoint Base | Ejemplos |
|---------|--------------|----------|
| Usuarios | `/api/usuarios` | GET /api/usuarios, POST, GET /api/usuarios/{id}, PUT, DELETE |
| Grupos | `/api/grupos` | ... |
| Bolsillos | `/api/bolsillos` | ... |
| Categorías | `/api/categorias` | ... |
| Ingresos | `/api/ingresos` | ... |
| Egresos | `/api/egresos` | ... |
| UsuarioGrupo | `/api/usuario-grupo` | GET /api/usuario-grupo/usuario/{usuarioId} |

## Modelo Relacional Simplificado
Ver diagrama provisto.

## Próximos Pasos Sugeridos
- Añadir validaciones (Bean Validation) y DTOs.
- Añadir capa de seguridad (Spring Security + JWT) para login.
- Servicios de reportes / estadísticas.
- Manejo de transacciones al mover dinero entre bolsillos.

---
Generado automáticamente como base inicial.
