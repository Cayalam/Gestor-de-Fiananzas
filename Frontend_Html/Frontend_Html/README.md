# FinanzApp (Frontend demo)

Interfaz HTML/CSS/JS para autenticación local (registro e inicio de sesión) sin backend. Los usuarios se guardan en `localStorage` y la sesión en `sessionStorage`.

## Estructura
- `index.html`: Inicio de sesión
- `register.html`: Registro de usuario
- `dashboard.html`: Panel tras autenticación
- `css/styles.css`: Estilos
- `js/app.js`: Lógica de validación, almacenamiento y sesión
- `assets/logo.svg`: Logo

## Ejecutar
Abre `index.html` haciendo doble clic o con un servidor estático.

### Windows PowerShell (opcional)
Si tienes Python instalado:

```powershell
$dir = "c:\\Users\\Caaya\\OneDrive - Universidad Industrial de Santander\\Desktop\\Proyecto_entornos\\Frontend_Html"; cd $dir; python -m http.server 5500
```

Luego visita http://localhost:5500/

## Notas de seguridad
- Este proyecto es solo para demostración. No guarda contraseñas reales de forma segura ni reemplaza un backend.
- Para producción, usa un servidor con autenticación real (hash con sal y algoritmo lento tipo Argon2/bcrypt, tokens, HTTPS, etc.).
