const ApiClient = (() => {
  const cfg = () => window.APP_CONFIG || {};
  const base = () => (cfg().apiBaseUrl || "").replace(/\/$/, "");
  const tokenKey = () => (cfg().storage && cfg().storage.tokenKey) || "finanzapp.token";
  const userKey = () => (cfg().storage && cfg().storage.userKey) || "finanzapp.user";

  async function request(path, { method = "GET", body, auth = false } = {}) {
    const headers = { "Content-Type": "application/json" };
    if (auth) {
      const t = localStorage.getItem(tokenKey());
      if (t) headers["Authorization"] = `Bearer ${t}`;
    }
    
    console.log("🚀 API Request:", {
      url: `${base()}${path}`,
      method,
      body,
      headers
    });
    
    const res = await fetch(`${base()}${path}`, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
      credentials: "omit"
    });

    const isJson = res.headers.get("content-type")?.includes("application/json");
    const data = isJson ? await res.json().catch(() => undefined) : await res.text();

    console.log("📥 API Response:", {
      status: res.status,
      ok: res.ok,
      data
    });

    if (!res.ok) {
      const msg = (data && (data.message || data.error || data.msg)) || `Error ${res.status}`;
      console.error("❌ API Error:", {
        status: res.status,
        statusText: res.statusText,
        message: msg,
        fullData: data,
        url: `${base()}${path}`
      });
      
      // Si es error 401 o 403, probablemente el token expiró
      if (res.status === 401 || res.status === 403) {
        console.error("🚫 Token inválido o expirado. Redirigiendo al login...");
        // Limpiar datos y redirigir
        localStorage.removeItem(tokenKey());
        localStorage.removeItem(userKey());
        
        // Mostrar alerta antes de redirigir
        setTimeout(() => {
          alert("Tu sesión ha expirado. Por favor, inicia sesión nuevamente.");
          window.location.href = "index.html";
        }, 100);
      }
      
      throw new Error(msg);
    }
    return isJson ? data : { message: data };
  }

  // Auth
  const auth = {
    async login(email, contrasena) {
      const data = await request("/api/auth/login", {
        method: "POST",
        body: { email, contrasena }
      });
      
      console.log("📥 Respuesta del login:", data);
      
      if (data?.token) {
        localStorage.setItem(tokenKey(), data.token);
        
        // Intentar obtener el ID del usuario de varias formas
        let userId = data.id || data.usuarioId || data.userId || data.user?.id;
        
        // Si no viene en el login, intentar obtenerlo del endpoint /me
        if (!userId) {
          console.log("⚠️ ID no encontrado en login, consultando /api/usuarios/me...");
          try {
            const meData = await request("/api/usuarios/me", { method: "GET", auth: true });
            console.log("📥 Respuesta de /api/usuarios/me:", meData);
            userId = meData.id || meData.usuarioId || meData.usuario?.id;
          } catch (err) {
            console.error("❌ No se pudo obtener el ID del usuario:", err);
          }
        }
        
        const userData = {
          id: userId,
          nombre: data.nombre,
          email: data.email
        };
        
        console.log("💾 Guardando usuario con ID:", userData);
        localStorage.setItem(userKey(), JSON.stringify(userData));
      }
      return data;
    },

    async register(nombre, email, contrasena, divisaPref) {
      return await request("/api/auth/register", {
        method: "POST",
        body: { nombre, email, contrasena, divisaPref }
      });
    },

    logout() {
      console.log("🚪 Limpiando datos de sesión...");
      localStorage.removeItem(tokenKey());
      localStorage.removeItem(userKey());
      console.log("✅ Datos de sesión eliminados");
      
      // Opcional: Limpiar todo el localStorage
      // localStorage.clear();
    },

    getStoredUser() {
      try { return JSON.parse(localStorage.getItem(userKey()) || "null"); } catch { return null; }
    },

    getStoredToken() {
      return localStorage.getItem(tokenKey());
    }
  };

  // Usuarios
  const usuarios = {
    async me() {
      return await request("/api/usuarios/me", { method: "GET", auth: true });
    },

    async crear(data) {
      return await request("/api/usuarios", { method: "POST", body: data, auth: true });
    },

    async listar() {
      return await request("/api/usuarios", { method: "GET", auth: true });
    },

    async obtener(id) {
      return await request(`/api/usuarios/${id}`, { method: "GET", auth: true });
    },

    async actualizar(id, data) {
      return await request(`/api/usuarios/${id}`, { method: "PUT", body: data, auth: true });
    },

    async eliminar(id) {
      return await request(`/api/usuarios/${id}`, { method: "DELETE", auth: true });
    }
  };

  // Bolsillos
  const bolsillos = {
    async crear(data) {
      return await request("/api/bolsillos", { method: "POST", body: data, auth: true });
    },

    async listar() {
      return await request("/api/bolsillos", { method: "GET", auth: true });
    },

    async obtener(id) {
      return await request(`/api/bolsillos/${id}`, { method: "GET", auth: true });
    },

    async actualizar(id, data) {
      return await request(`/api/bolsillos/${id}`, { method: "PUT", body: data, auth: true });
    },

    async eliminar(id) {
      return await request(`/api/bolsillos/${id}`, { method: "DELETE", auth: true });
    }
  };

  // Categorías
  const categorias = {
    async crear(data) {
      return await request("/api/categorias", { method: "POST", body: data, auth: true });
    },

    async listar() {
      return await request("/api/categorias", { method: "GET", auth: true });
    },

    async obtener(id) {
      return await request(`/api/categorias/${id}`, { method: "GET", auth: true });
    },

    async actualizar(id, data) {
      return await request(`/api/categorias/${id}`, { method: "PUT", body: data, auth: true });
    },

    async eliminar(id) {
      return await request(`/api/categorias/${id}`, { method: "DELETE", auth: true });
    }
  };

  // Ingresos
  const ingresos = {
    async crear(data) {
      return await request("/api/ingresos", { method: "POST", body: data, auth: true });
    },

    async listar() {
      return await request("/api/ingresos", { method: "GET", auth: true });
    },

    async obtener(id) {
      return await request(`/api/ingresos/${id}`, { method: "GET", auth: true });
    },

    async actualizar(id, data) {
      return await request(`/api/ingresos/${id}`, { method: "PUT", body: data, auth: true });
    },

    async eliminar(id) {
      return await request(`/api/ingresos/${id}`, { method: "DELETE", auth: true });
    }
  };

  // Egresos
  const egresos = {
    async crear(data) {
      return await request("/api/egresos", { method: "POST", body: data, auth: true });
    },

    async listar() {
      return await request("/api/egresos", { method: "GET", auth: true });
    },

    async obtener(id) {
      return await request(`/api/egresos/${id}`, { method: "GET", auth: true });
    },

    async actualizar(id, data) {
      return await request(`/api/egresos/${id}`, { method: "PUT", body: data, auth: true });
    },

    async eliminar(id) {
      return await request(`/api/egresos/${id}`, { method: "DELETE", auth: true });
    }
  };

  // Grupos
  const grupos = {
    async crear(data) {
      return await request("/api/grupos", { method: "POST", body: data, auth: true });
    },

    async listar() {
      return await request("/api/grupos", { method: "GET", auth: true });
    },

    async obtener(id) {
      return await request(`/api/grupos/${id}`, { method: "GET", auth: true });
    },

    async actualizar(id, data) {
      return await request(`/api/grupos/${id}`, { method: "PUT", body: data, auth: true });
    },

    async eliminar(id) {
      return await request(`/api/grupos/${id}`, { method: "DELETE", auth: true });
    }
  };

  // Usuario-Grupo
  const usuarioGrupo = {
    async agregar(data) {
      return await request("/api/usuario-grupo", { method: "POST", body: data, auth: true });
    },

    async listarGruposDeUsuario(usuarioId) {
      return await request(`/api/usuario-grupo/usuario/${usuarioId}`, { method: "GET", auth: true });
    },

    async listarUsuariosDeGrupo(grupoId) {
      return await request(`/api/usuario-grupo/grupo/${grupoId}`, { method: "GET", auth: true });
    },

    async obtenerRelacion(usuarioId, grupoId) {
      return await request(`/api/usuario-grupo/${usuarioId}/${grupoId}`, { method: "GET", auth: true });
    },

    async eliminarRelacion(usuarioId, grupoId) {
      return await request(`/api/usuario-grupo/${usuarioId}/${grupoId}`, { method: "DELETE", auth: true });
    }
  };

  return {
    ...auth,
    usuarios,
    bolsillos, 
    categorias,
    ingresos,
    egresos,
    grupos,
    usuarioGrupo
  };
})();
