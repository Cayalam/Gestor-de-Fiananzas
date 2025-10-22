const app = (() => {
    // Cache DOM elements
    const $ = (selector) => document.querySelector(selector);
    const $$ = (selector) => document.querySelectorAll(selector);
    
    // State
    let currentUser = null;
    let currentView = "dashboard";
    let bolsillos = [];
    let categorias = [];
    let ingresos = [];
    let egresos = [];
    let grupos = [];
    let tempGroupMembers = []; // Miembros temporales del grupo en creación

    // Modal Management
    const modalHandlers = {
        showModal: (id) => {
            console.log(`🎭 Abriendo modal: ${id}`);
            const modal = $(`#${id}`);
            if (modal) {
                // Si es el modal de transacción, poblar los selectores
                if (id === "transaccionModal") {
                    console.log("   → Es modal de transacción, poblando selectores...");
                    modalHandlers.populateTransaccionSelects();
                }
                modal.classList.add("active");
                console.log(`   ✅ Modal ${id} abierto`);
            } else {
                console.error(`   ❌ No se encontró el modal #${id}`);
            }
        },
        
        hideModal: (id) => {
            const modal = $(`#${id}`);
            if (modal) {
                modal.classList.remove("active");
                
                // Limpiar campos ocultos y restaurar títulos
                if (id === "categoriaModal") {
                    $("#categoriaId").value = "";
                    const modalTitle = document.querySelector("#categoriaModal .modal__title");
                    if (modalTitle) modalTitle.textContent = "Nueva Categoría";
                } else if (id === "bolsilloModal") {
                    $("#bolsilloId").value = "";
                    const modalTitle = document.querySelector("#bolsilloModal .modal__title");
                    if (modalTitle) modalTitle.textContent = "Nuevo Bolsillo";
                } else if (id === "transaccionModal") {
                    $("#transaccionId").value = "";
                    $("#transaccionTipoOriginal").value = "";
                    const modalTitle = document.querySelector("#transaccionModal .modal__title");
                    if (modalTitle) modalTitle.textContent = "Nueva Transacción";
                }
            }
        },

        closeOnBackdrop: (e) => {
            if (e.target === e.currentTarget) {
                e.target.classList.remove("active");
            }
        },

        populateTransaccionSelects: () => {
            console.log("🔧 populateTransaccionSelects() llamado");
            console.log("   - Categorías disponibles:", categorias.length, categorias);
            console.log("   - Bolsillos disponibles:", bolsillos.length, bolsillos);
            
            // Poblar select de categorías
            const categoriaSelect = $("#categoriaId");
            console.log("   - Elemento categoriaSelect encontrado:", !!categoriaSelect);
            if (categoriaSelect) {
                categoriaSelect.innerHTML = '<option value="">Selecciona una categoría</option>' +
                    categorias.map(c => `<option value="${c.id}">${c.nombre} (${c.tipo === 'ing' ? 'Ingreso' : 'Gasto'})</option>`).join("");
                
                console.log(`   ✅ ${categorias.length} categorías cargadas en el selector`);
            } else {
                console.error("   ❌ No se encontró el elemento #categoriaId");
            }

            // Poblar select de bolsillos
            const bolsilloSelect = $("#bolsilloId");
            console.log("   - Elemento bolsilloSelect encontrado:", !!bolsilloSelect);
            if (bolsilloSelect) {
                bolsilloSelect.innerHTML = '<option value="">Selecciona un bolsillo</option>' +
                    bolsillos.map(b => `<option value="${b.id}">${b.nombre} - ${b.saldo.toFixed(2)} €</option>`).join("");
                
                console.log(`   ✅ ${bolsillos.length} bolsillos cargados en el selector`);
            } else {
                console.error("   ❌ No se encontró el elemento #bolsilloId");
            }
        },

        attachModalListeners: () => {
            $$(".modal").forEach(modal => {
                modal.addEventListener("click", modalHandlers.closeOnBackdrop);
                const closeBtn = modal.querySelector("[data-close-modal]");
                if (closeBtn) {
                    closeBtn.addEventListener("click", () => modal.classList.remove("active"));
                }
            });
        },

        // Gestión de miembros del grupo
        renderGroupMembers: () => {
            const membersList = $("#membersList");
            if (!membersList) return;

            if (tempGroupMembers.length === 0) {
                membersList.innerHTML = '<p class="empty-message-small">No hay miembros añadidos. Añade usuarios por su email.</p>';
                return;
            }

            membersList.innerHTML = tempGroupMembers.map((member, index) => `
                <div class="member-item">
                    <div class="member-info">
                        <div class="member-avatar">${member.email.substring(0, 2)}</div>
                        <div class="member-details">
                            <span class="member-name">${member.nombre || 'Usuario'}</span>
                            <span class="member-email">${member.email}</span>
                        </div>
                    </div>
                    <button type="button" class="member-remove" onclick="app.removeMemberFromGroup(${index})" title="Eliminar miembro">
                        ✕
                    </button>
                </div>
            `).join('');
        },

        addMemberToGroup: async () => {
            const emailInput = $("#emailMiembro");
            const email = emailInput?.value?.trim();

            if (!email) {
                alert("Por favor, ingresa un email válido");
                return;
            }

            // Validar formato de email
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(email)) {
                alert("Por favor, ingresa un email válido");
                return;
            }

            // Verificar si ya está en la lista
            if (tempGroupMembers.some(m => m.email === email)) {
                alert("Este usuario ya está en la lista de miembros");
                emailInput.value = "";
                return;
            }

            console.log(`🔍 Buscando usuario con email: ${email}`);

            try {
                // Aquí deberías tener un endpoint en tu API para buscar usuarios por email
                // Por ahora, simularemos que encontramos al usuario
                const member = {
                    email: email,
                    nombre: email.split('@')[0], // Usamos la parte antes del @ como nombre temporal
                    // En producción, aquí vendría el ID del usuario encontrado
                };

                tempGroupMembers.push(member);
                modalHandlers.renderGroupMembers();
                emailInput.value = "";
                console.log("✅ Miembro añadido:", member);
            } catch (err) {
                console.error("❌ Error al buscar usuario:", err);
                alert("No se encontró ningún usuario con ese email");
            }
        },

        removeMemberFromGroup: (index) => {
            console.log(`🗑️ Eliminando miembro en posición ${index}`);
            tempGroupMembers.splice(index, 1);
            modalHandlers.renderGroupMembers();
        },

        clearGroupMembers: () => {
            tempGroupMembers = [];
            modalHandlers.renderGroupMembers();
        },

        // Nuevas funciones para editar grupo
        addEditMember: () => {
            const emailInput = $("#nuevoMiembroEmail");
            const email = emailInput?.value.trim();

            if (!email) {
                alert("Por favor, ingresa un email");
                return;
            }

            if (!email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
                alert("Por favor, ingresa un email válido");
                return;
            }

            console.log(`➕ Añadiendo miembro: ${email}`);

            // Verificar si ya existe en la lista
            const editMembersList = $("#editMiembrosList");
            const existingEmails = Array.from(editMembersList.querySelectorAll('.edit-member-email'))
                .map(el => el.textContent);
            
            if (existingEmails.includes(email)) {
                alert("Este usuario ya está en el grupo");
                return;
            }

            // Añadir nuevo miembro a la lista visual (SIN data-existing para que se detecte como nuevo)
            const memberHtml = `
                <div class="edit-member-item" data-email="${email}" data-new="true">
                    <div class="member-avatar">${email.charAt(0).toUpperCase()}</div>
                    <div class="member-details">
                        <div class="member-name">${email.split('@')[0]}</div>
                        <div class="edit-member-email">${email}</div>
                    </div>
                    <span class="member-badge new-badge">Nuevo</span>
                    <button type="button" class="member-remove" onclick="app.removeEditMember('${email}')">
                        ✕
                    </button>
                </div>
            `;
            
            editMembersList.insertAdjacentHTML('beforeend', memberHtml);
            emailInput.value = "";
            console.log("✅ Miembro añadido a la lista");
        },

        removeEditMember: (email) => {
            const memberItem = $(`.edit-member-item[data-email="${email}"]`);
            if (memberItem) {
                memberItem.remove();
                console.log(`🗑️ Miembro eliminado: ${email}`);
            }
        }
    };

    // View Management
    const viewHandlers = {
        switchView: (viewName) => {
            $$(".view").forEach(v => v.classList.add("hidden"));
            $$(".nav__link").forEach(l => l.classList.remove("active"));

            $(`#${viewName}View`)?.classList.remove("hidden");
            $(`.nav__link[data-view="${viewName}"]`)?.classList.add("active");
            
            currentView = viewName;
            window.location.hash = viewName;

            // Renderizar la vista correspondiente
            switch(viewName) {
                case 'categorias':
                    viewRenderers.renderCategorias();
                    break;
                case 'bolsillos':
                    viewRenderers.renderBolsillos();
                    break;
                case 'transacciones':
                    viewRenderers.renderTransacciones();
                    break;
                case 'grupos':
                    viewRenderers.renderGrupos();
                    break;
            }
        },

        attachViewListeners: () => {
            $$(".nav__link[data-view]").forEach(link => {
                link.addEventListener("click", (e) => {
                    e.preventDefault();
                    const view = e.currentTarget.dataset.view;
                    viewHandlers.switchView(view);
                });
            });
        },

        initFromHash: () => {
            const hash = window.location.hash.slice(1);
            if (hash) viewHandlers.switchView(hash);
        }
    };

    // View Renderers
    const viewRenderers = {
        renderCategorias: () => {
            const grid = $("#categoriasGrid");
            if (!grid) return;

            if (categorias.length === 0) {
                grid.innerHTML = '<p class="empty-message">No hay categorías creadas. Crea tu primera categoría.</p>';
                return;
            }

            grid.innerHTML = categorias.map(cat => `
                <div class="categoria-card">
                    <div class="categoria-card__header">
                        <h3>${cat.nombre}</h3>
                        <span class="categoria-card__tipo ${cat.tipo === 'ing' ? 'tipo-ingreso' : 'tipo-gasto'}">
                            ${cat.tipo === 'ing' ? 'Ingreso' : 'Gasto'}
                        </span>
                    </div>
                    <div class="categoria-card__actions">
                        <button class="btn-icon" onclick="app.editarCategoria(${cat.id})" title="Editar">✏️</button>
                        <button class="btn-icon" onclick="app.eliminarCategoria(${cat.id})" title="Eliminar">🗑️</button>
                    </div>
                </div>
            `).join("");
        },

        renderBolsillos: () => {
            const grid = $("#bolsillosGrid");
            if (!grid) return;

            if (bolsillos.length === 0) {
                grid.innerHTML = '<p class="empty-message">No hay bolsillos creados. Crea tu primer bolsillo.</p>';
                return;
            }

            grid.innerHTML = bolsillos.map(bol => `
                <div class="bolsillo-card">
                    <div class="bolsillo-card__header">
                        <h3>${bol.nombre}</h3>
                        ${bol.grupo ? `<span class="bolsillo-card__grupo">Grupo: ${bol.grupo}</span>` : ''}
                    </div>
                    <div class="bolsillo-card__saldo">
                        <span class="saldo-label">Saldo:</span>
                        <span class="saldo-valor ${bol.saldo >= 0 ? 'positivo' : 'negativo'}">
                            ${bol.saldo.toFixed(2)} €
                        </span>
                    </div>
                    <div class="bolsillo-card__actions">
                        <button class="btn-icon" onclick="app.editarBolsillo(${bol.id})" title="Editar">✏️</button>
                        <button class="btn-icon" onclick="app.eliminarBolsillo(${bol.id})" title="Eliminar">🗑️</button>
                    </div>
                </div>
            `).join("");
        },

        renderTransacciones: () => {
            const grid = $("#transaccionesGrid");
            if (!grid) return;

            const allTransacciones = [
                ...ingresos.map(i => ({ ...i, tipo: 'ingreso' })),
                ...egresos.map(e => ({ ...e, tipo: 'egreso' }))
            ].sort((a, b) => new Date(b.fecha) - new Date(a.fecha));

            if (allTransacciones.length === 0) {
                grid.innerHTML = '<p class="empty-message">No hay transacciones registradas. Crea tu primera transacción.</p>';
                return;
            }

            grid.innerHTML = allTransacciones.map(t => {
                const categoria = categorias.find(c => c.id === t.categoriaId);
                const bolsillo = bolsillos.find(b => b.id === t.bolsilloId);
                const fecha = new Date(t.fecha).toLocaleDateString('es-ES');

                return `
                    <div class="transaccion-card">
                        <div class="transaccion-card__fecha">${fecha}</div>
                        <div class="transaccion-card__info">
                            <h4>${t.descripcion}</h4>
                            <p>
                                <span class="transaccion-categoria">${categoria?.nombre || 'Sin categoría'}</span>
                                •
                                <span class="transaccion-bolsillo">${bolsillo?.nombre || 'Sin bolsillo'}</span>
                            </p>
                        </div>
                        <div class="transaccion-card__monto ${t.tipo === 'ingreso' ? 'monto-ingreso' : 'monto-egreso'}">
                            ${t.tipo === 'ingreso' ? '+' : '-'}${t.monto.toFixed(2)} €
                        </div>
                        <div class="transaccion-card__actions">
                            <button class="btn-icon" onclick="app.edit${t.tipo === 'ingreso' ? 'Ingreso' : 'Egreso'}(${t.id})" title="Editar">✏️</button>
                            <button class="btn-icon" onclick="app.delete${t.tipo === 'ingreso' ? 'Ingreso' : 'Egreso'}(${t.id})" title="Eliminar">🗑️</button>
                        </div>
                    </div>
                `;
            }).join("");
        },

        renderGrupos: () => {
            const grid = $("#gruposGrid");
            console.log("🎨 Renderizando grupos...");
            console.log("   📊 Total de grupos:", grupos.length);
            console.log("   📦 Datos de grupos:", grupos);
            
            if (!grid) {
                console.error("   ❌ No se encontró el elemento #gruposGrid");
                return;
            }

            if (grupos.length === 0) {
                grid.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-state__icon">👥</div>
                        <h3 class="empty-state__title">No tienes grupos</h3>
                        <p class="empty-state__text">Los grupos te permiten compartir gastos con amigos, familia o compañeros.</p>
                        <button class="btn btn-primary" onclick="document.querySelector('#nuevoGrupoBtn2').click()">
                            ➕ Crear mi primer grupo
                        </button>
                    </div>
                `;
                console.log("   ℹ️ No hay grupos para mostrar");
                return;
            }

            grid.innerHTML = grupos.map(grupo => {
                // Generar la lista de miembros
                const miembrosHtml = grupo.miembros && grupo.miembros.length > 0
                    ? grupo.miembros.map(m => `
                        <div class="member-badge" title="${m.email} - ${m.rol}">
                            <span class="member-avatar">${m.nombre.charAt(0).toUpperCase()}</span>
                            <span class="member-name">${m.nombre}</span>
                            ${m.rol === 'ADMIN' ? '<span class="admin-badge">👑</span>' : ''}
                        </div>
                    `).join('')
                    : '<p class="no-members">Sin miembros</p>';

                return `
                    <div class="grupo-card">
                        <div class="grupo-card__header">
                            <h3>${grupo.nombre}</h3>
                            <span class="grupo-badge">👥 ${grupo.cantidadMiembros || 0} miembros</span>
                        </div>
                        <div class="grupo-card__info">
                            <p class="grupo-descripcion">${grupo.descripcion || 'Sin descripción'}</p>
                            <div class="grupo-miembros">
                                ${miembrosHtml}
                            </div>
                        </div>
                        <div class="grupo-card__actions">
                            <button class="btn-icon" onclick="app.verDetallesGrupo(${grupo.id})" title="Ver detalles">👁️</button>
                            <button class="btn-icon" onclick="app.salirGrupo(${grupo.id})" title="Salir del grupo">🚪</button>
                        </div>
                    </div>
                `;
            }).join("");
            
            console.log("   ✅ Grupos renderizados exitosamente");
        }
    };

    // Dashboard 
    const dashboardHandlers = {
        updateStats: async () => {
            const total = bolsillos.reduce((sum, b) => sum + b.saldo, 0);
            
            // Combinar ingresos y egresos del mes actual
            const currentMonth = new Date().getMonth();
            const currentYear = new Date().getFullYear();
            
            const ingresosDelMes = ingresos
                .filter(i => {
                    const fecha = new Date(i.fecha);
                    return fecha.getMonth() === currentMonth && fecha.getFullYear() === currentYear;
                })
                .reduce((sum, i) => sum + i.monto, 0);
                
            const gastosDelMes = egresos
                .filter(e => {
                    const fecha = new Date(e.fecha);
                    return fecha.getMonth() === currentMonth && fecha.getFullYear() === currentYear;
                })
                .reduce((sum, e) => sum + e.monto, 0);
            
            $("#balanceTotal").textContent = `${total.toFixed(2)} €`;
            $("#ingresosMes").textContent = `${ingresosDelMes.toFixed(2)} €`;
            $("#gastosMes").textContent = `${gastosDelMes.toFixed(2)} €`;
            $("#balanceNeto").textContent = `${(ingresosDelMes - gastosDelMes).toFixed(2)} €`;
        },

        updateBolsillosList: () => {
            const container = $("#bolsillosList");
            if (!container) return;

            if (bolsillos.length === 0) {
                container.innerHTML = '<p style="text-align: center; color: #666; padding: 20px;">No hay bolsillos creados</p>';
                return;
            }

            container.innerHTML = bolsillos
                .map(b => `
                    <div class="bolsillo-item">
                        <strong>${b.nombre}</strong>
                        <span>${b.saldo.toFixed(2)} €</span>
                    </div>
                `).join("");
        },

        updateCategoriasList: () => {
            const container = $("#categoriasList");
            if (!container) return;

            if (categorias.length === 0) {
                container.innerHTML = '<p style="text-align: center; color: #666; padding: 20px;">No hay categorías creadas</p>';
                return;
            }

            container.innerHTML = categorias
                .slice(0, 5)
                .map(c => `
                    <div class="categoria-item">
                        <strong>${c.nombre}</strong>
                        <span class="categoria-tipo ${c.tipo === 'ing' ? 'tipo-ingreso' : 'tipo-gasto'}">
                            ${c.tipo === 'ing' ? '💰 Ingreso' : '💸 Gasto'}
                        </span>
                    </div>
                `).join("");
        },

        updateCategoriaGastos: () => {
            const container = $("#categoriaGastos");
            if (!container) return;

            const gastosPorCategoria = categorias
                .filter(c => c.tipo === "gasto")
                .map(c => {
                    const total = transacciones
                        .filter(t => t.categoriaId === c.id && t.tipo === "egreso")
                        .reduce((sum, t) => sum + t.monto, 0);
                    return { ...c, total };
                })
                .sort((a, b) => b.total - a.total);

            container.innerHTML = gastosPorCategoria
                .map(c => `
                    <div class="categoria-gasto">
                        <div class="categoria-gasto__info">
                            <span>${c.nombre}</span>
                            <strong>${c.total.toFixed(2)} €</strong>
                        </div>
                        <div class="categoria-gasto__bar">
                            <div style="width: ${(c.total / Math.max(...gastosPorCategoria.map(g => g.total)) * 100)}%"></div>
                        </div>
                    </div>
                `).join("");
        },

        updateTransaccionesList: () => {
            const container = $("#transaccionesList");
            if (!container) return;

            // Combinar ingresos y egresos con su tipo
            const allTransacciones = [
                ...ingresos.map(i => ({ ...i, tipo: 'ingreso' })),
                ...egresos.map(e => ({ ...e, tipo: 'egreso' }))
            ];

            // Filtrar solo transacciones que tengan categoría y bolsillo válidos
            const transaccionesValidas = allTransacciones.filter(t => {
                const categoria = categorias.find(c => c.id === t.categoriaId);
                const bolsillo = bolsillos.find(b => b.id === t.bolsilloId);
                return categoria && bolsillo; // Solo mostrar si ambos existen
            });

            if (transaccionesValidas.length === 0) {
                container.innerHTML = '<p style="text-align: center; color: #666; padding: 20px;">No hay transacciones para mostrar</p>';
                return;
            }

            container.innerHTML = transaccionesValidas
                .sort((a, b) => new Date(b.fecha) - new Date(a.fecha))
                .slice(0, 5)
                .map(t => {
                    const categoria = categorias.find(c => c.id === t.categoriaId);
                    const bolsillo = bolsillos.find(b => b.id === t.bolsilloId);
                    return `
                        <div class="transaccion-item ${t.tipo}">
                            <div class="transaccion-item__info">
                                <strong>${t.descripcion}</strong>
                                <small>${categoria.nombre} • ${bolsillo.nombre}</small>
                            </div>
                            <span class="transaccion-item__monto">
                                ${t.tipo === "ingreso" ? "+" : "-"}${t.monto.toFixed(2)} €
                            </span>
                        </div>
                    `;
                }).join("");
        },

        updateGruposList: () => {
            const container = $("#gruposList");
            if (!container) return;

            if (grupos.length === 0) {
                container.innerHTML = '<p style="text-align: center; color: #666; padding: 20px;">No hay grupos creados</p>';
                return;
            }

            container.innerHTML = grupos
                .slice(0, 5)
                .map(g => {
                    const miembrosCount = g.miembros ? g.miembros.length : 0;
                    return `
                        <div class="grupo-item">
                            <strong>${g.nombre}</strong>
                            <span>👥 ${miembrosCount} miembro${miembrosCount !== 1 ? 's' : ''}</span>
                        </div>
                    `;
                }).join("");
        },

        refreshDashboard: () => {
            dashboardHandlers.updateStats();
            dashboardHandlers.updateBolsillosList();
            dashboardHandlers.updateCategoriasList();
            dashboardHandlers.updateCategoriaGastos();
            dashboardHandlers.updateTransaccionesList();
            dashboardHandlers.updateGruposList();
        }
    };

    // Form Submissions
    const formHandlers = {
        submitLogin: async (e) => {
            e.preventDefault();
            const form = e.target;
            const email = form.email.value;
            const contrasena = form.contrasena.value;

            try {
                await ApiClient.login(email, contrasena);
                window.location.href = "dashboard.html";
            } catch (err) {
                $("#loginError").textContent = err.message;
            }
        },

        submitRegister: async (e) => {
            e.preventDefault();
            const form = e.target;
            const data = {
                nombre: form.nombre.value,
                email: form.email.value,
                contrasena: form.contrasena.value,
                divisaPref: form.divisaPref.value
            };

            try {
                await ApiClient.register(data.nombre, data.email, data.contrasena, data.divisaPref);
                window.location.href = "index.html";
            } catch (err) {
                $("#registerError").textContent = err.message;
            }
        },

        submitTransaccion: async (e) => {
            e.preventDefault();
            const form = e.target;
            
            const user = ApiClient.getStoredUser();
            const transaccionId = form.id.value;
            const tipoOriginal = form.tipoOriginal.value;
            const isEdit = transaccionId && transaccionId !== "";
            
            const data = {
                tipo: form.tipo.value,
                descripcion: form.descripcion.value.trim(),
                monto: parseFloat(form.monto.value),
                categoriaId: parseInt(form.categoriaId.value),
                bolsilloId: parseInt(form.bolsilloId.value)
            };
            
            // Agregar usuarioId si existe
            if (user?.id) {
                data.usuarioId = user.id;
            }
            
            console.log(`📤 Datos de transacción a ${isEdit ? 'actualizar' : 'crear'}:`, JSON.stringify(data, null, 2));

            try {
                if (isEdit) {
                    // Si cambiaron el tipo, eliminar del tipo anterior y crear en el nuevo
                    if (tipoOriginal !== data.tipo) {
                        if (tipoOriginal === "ingreso") {
                            await ApiClient.ingresos.eliminar(transaccionId);
                        } else {
                            await ApiClient.egresos.eliminar(transaccionId);
                        }
                        // Crear como nuevo del otro tipo
                        if (data.tipo === "ingreso") {
                            await ApiClient.ingresos.crear(data);
                        } else {
                            await ApiClient.egresos.crear(data);
                        }
                        console.log("✅ Transacción convertida exitosamente");
                    } else {
                        // Actualizar del mismo tipo
                        if (data.tipo === "ingreso") {
                            await ApiClient.ingresos.actualizar(transaccionId, data);
                        } else {
                            await ApiClient.egresos.actualizar(transaccionId, data);
                        }
                        console.log("✅ Transacción actualizada exitosamente");
                    }
                } else {
                    // Crear nueva
                    if (data.tipo === "ingreso") {
                        await ApiClient.ingresos.crear(data);
                        console.log("✅ Ingreso creado exitosamente");
                    } else {
                        await ApiClient.egresos.crear(data);
                        console.log("✅ Egreso creado exitosamente");
                    }
                }
                modalHandlers.hideModal("transaccionModal");
                form.reset();
                await loadData();
                dashboardHandlers.refreshDashboard();
                viewRenderers.renderTransacciones();
            } catch (err) {
                console.error(`❌ Error al ${isEdit ? 'actualizar' : 'crear'} transacción:`, err);
                if (!err.message.includes("403") && !err.message.includes("401")) {
                    alert(`Error al ${isEdit ? 'actualizar' : 'crear'} transacción: ${err.message}`);
                }
            }
        },

        submitCategoria: async (e) => {
            e.preventDefault();
            const form = e.target;
            
            const user = ApiClient.getStoredUser();
            const categoriaId = form.id.value;
            const isEdit = categoriaId && categoriaId !== "";
            
            // Convertir tipo de formulario a BD: "ingreso" -> "ing", "gasto" -> "eg"
            const tipoFormulario = form.tipo.value;
            const tipoBD = tipoFormulario === 'ingreso' ? 'ing' : 'eg';
            
            const data = {
                nombre: form.nombre.value.trim(),
                tipo: tipoBD
            };
            
            // Agregar usuarioId si existe
            if (user?.id) {
                data.usuarioId = user.id;
            }
            
            console.log(`📤 Datos de categoría a ${isEdit ? 'actualizar' : 'crear'}:`, JSON.stringify(data, null, 2));
            console.log(`🔑 ID de categoría: ${categoriaId}, isEdit: ${isEdit}`);

            try {
                if (isEdit) {
                    await ApiClient.categorias.actualizar(categoriaId, data);
                    console.log("✅ Categoría actualizada exitosamente");
                } else {
                    await ApiClient.categorias.crear(data);
                    console.log("✅ Categoría creada exitosamente");
                }
                modalHandlers.hideModal("categoriaModal");
                form.reset();
                document.getElementById('categoriaId').value = ''; // Limpiar ID
                await loadData();
                dashboardHandlers.refreshDashboard();
                viewRenderers.renderCategorias();
            } catch (err) {
                console.error(`❌ Error al ${isEdit ? 'actualizar' : 'crear'} categoría:`, err);
                if (!err.message.includes("403") && !err.message.includes("401")) {
                    alert(`Error al ${isEdit ? 'actualizar' : 'crear'} categoría: ${err.message}`);
                }
            }
        },

        submitBolsillo: async (e) => {
            e.preventDefault();
            const form = e.target;
            const bolsilloId = form.bolsilloId?.value || "";
            const isEdit = bolsilloId && bolsilloId !== "";
            const nombreValue = form.nombre.value;
            const saldoValue = form.saldo.value;
            
            // Verificar token
            const token = localStorage.getItem("finanzapp.token");
            console.log("🔑 Token presente:", token ? "✅ Sí" : "❌ No", token?.substring(0, 20) + "...");
            
            console.log("📝 Valores del formulario RAW:", {
                bolsilloId: bolsilloId,
                isEdit: isEdit,
                nombre: nombreValue,
                saldo: saldoValue,
                nombreTrimmed: nombreValue.trim(),
                saldoFloat: parseFloat(saldoValue)
            });
            
            // Validar nombre
            if (!nombreValue || nombreValue.trim() === "") {
                alert("Por favor, ingresa un nombre para el bolsillo");
                return;
            }
            
            const saldo = parseFloat(saldoValue);
            
            // Validar que el saldo sea un número válido
            if (isNaN(saldo) || saldo < 0) {
                alert("Por favor, ingresa un saldo inicial válido (mínimo 0)");
                return;
            }
            
            const user = ApiClient.getStoredUser();
            
            const data = {
                nombre: nombreValue.trim(),
                saldo: saldo
            };
            
            // Agregar usuarioId si existe
            if (user?.id) {
                data.usuarioId = user.id;
            }

            console.log(`✅ Datos de bolsillo a ${isEdit ? 'actualizar' : 'crear'}:`, JSON.stringify(data, null, 2));

            try {
                if (isEdit) {
                    await ApiClient.bolsillos.actualizar(bolsilloId, data);
                    console.log("✅ Bolsillo actualizado exitosamente");
                } else {
                    await ApiClient.bolsillos.crear(data);
                    console.log("✅ Bolsillo creado exitosamente");
                }
                modalHandlers.hideModal("bolsilloModal");
                form.reset();
                await loadData();
                dashboardHandlers.refreshDashboard();
                viewRenderers.renderBolsillos();
            } catch (err) {
                console.error(`❌ Error al ${isEdit ? 'actualizar' : 'crear'} bolsillo:`, err);
                if (!err.message.includes("403") && !err.message.includes("401")) {
                    alert(`Error al ${isEdit ? 'actualizar' : 'crear'} bolsillo: ${err.message}`);
                }
            }
        },

        submitGrupo: async (e) => {
            e.preventDefault();
            const form = e.target;
            
            const user = ApiClient.getStoredUser();
            
            const data = {
                nombre: form.nombre.value.trim(),
                descripcion: form.descripcion.value.trim(),
                miembros: tempGroupMembers.map(m => m.email) // Enviar los emails de los miembros
            };
            
            // Agregar usuarioId si existe
            if (user?.id) {
                data.usuarioId = user.id;
            }
            
            console.log("📤 Datos de grupo a enviar:", JSON.stringify(data, null, 2));
            console.log(`   👥 Miembros añadidos: ${data.miembros.length}`);

            try {
                await ApiClient.grupos.crear(data);
                console.log("✅ Grupo creado exitosamente");
                modalHandlers.hideModal("grupoModal");
                form.reset();
                modalHandlers.clearGroupMembers(); // Limpiar lista de miembros
                await loadData();
                viewRenderers.renderGrupos();
            } catch (err) {
                console.error("❌ Error al crear grupo:", err);
                if (!err.message.includes("403") && !err.message.includes("401")) {
                    alert(`Error al crear grupo: ${err.message}`);
                }
            }
        },

        submitEditarGrupo: async (e) => {
            e.preventDefault();
            const form = e.target;
            
            const grupoId = $("#editGrupoId").value;
            const data = {
                nombre: $("#editNombreGrupo").value.trim(),
                descripcion: $("#editDescripcionGrupo").value.trim()
            };
            
            // Obtener emails de SOLO los nuevos miembros (los que tienen data-new="true")
            const nuevosMiembros = Array.from($$('.edit-member-item[data-new="true"]'))
                .map(item => item.getAttribute('data-email'))
                .filter(email => email);
            
            if (nuevosMiembros.length > 0) {
                data.nuevosMiembros = nuevosMiembros;
            }
            
            console.log("📤 Actualizando grupo ID:", grupoId);
            console.log("   Datos:", data);
            console.log("   Nuevos miembros a añadir:", nuevosMiembros);
            console.log("   Total de miembros en lista:", $$('.edit-member-item').length);

            try {
                await ApiClient.grupos.actualizar(grupoId, data);
                console.log("✅ Grupo actualizado exitosamente");
                modalHandlers.hideModal("editarGrupoModal");
                form.reset();
                await loadData();
                dashboardHandlers.refreshDashboard();
                viewRenderers.renderGrupos();
                alert("✅ Grupo actualizado exitosamente");
            } catch (err) {
                console.error("❌ Error al actualizar grupo:", err);
                alert(`Error al actualizar grupo: ${err.message}`);
            }
        }
    };

    // Data Loading
    const loadData = async () => {
        try {
            const user = ApiClient.getStoredUser();
            console.log("👤 Usuario actual:", user);
            
            const [bolsillosData, categoriasData, ingresosData, egresosData, gruposData] = await Promise.all([
                ApiClient.bolsillos.listar(),
                ApiClient.categorias.listar(),
                ApiClient.ingresos.listar(),
                ApiClient.egresos.listar(),
                ApiClient.grupos.listar()
            ]);

            console.log("📦 Datos recibidos del backend:");
            console.log("  - Bolsillos:", bolsillosData);
            console.log("  - Categorías:", categoriasData);
            console.log("  - Ingresos:", ingresosData);
            console.log("  - Egresos:", egresosData);
            console.log("  - Grupos:", gruposData);
            console.log("🔄 VERSIÓN DEL CÓDIGO: 2025-10-22 v2.0 - Grupos sin filtro frontend");

            // Filtrar los datos por usuario actual (solución temporal mientras se arregla el backend)
            if (user?.id) {
                // Contar datos de otros usuarios
                const bolsillosOtros = bolsillosData.filter(b => b.usuarioId !== user.id).length;
                const categoriasOtras = categoriasData.filter(c => c.usuarioId !== user.id).length;
                const ingresosOtros = ingresosData.filter(i => i.usuarioId !== user.id).length;
                const egresosOtros = egresosData.filter(e => e.usuarioId !== user.id).length;
                // Los grupos ya vienen filtrados por el backend (no tienen usuarioId directo)
                
                if (bolsillosOtros > 0 || categoriasOtras > 0 || ingresosOtros > 0 || egresosOtros > 0) {
                    console.warn("⚠️ ADVERTENCIA: El backend está devolviendo datos de otros usuarios!");
                    console.warn(`   - ${bolsillosOtros} bolsillos de otros usuarios`);
                    console.warn(`   - ${categoriasOtras} categorías de otros usuarios`);
                    console.warn(`   - ${ingresosOtros} ingresos de otros usuarios`);
                    console.warn(`   - ${egresosOtros} egresos de otros usuarios`);
                    console.warn("   ⚠️ Esto debería ser arreglado en el BACKEND");
                }
                
                bolsillos = bolsillosData.filter(b => b.usuarioId === user.id);
                categorias = categoriasData.filter(c => c.usuarioId === user.id);
                ingresos = ingresosData.filter(i => i.usuarioId === user.id);
                egresos = egresosData.filter(e => e.usuarioId === user.id);
                // Los grupos ya vienen filtrados por el backend, no filtrar aquí
                grupos = gruposData;
                
                console.log("✅ Datos filtrados por usuario (ID: " + user.id + "):");
                console.log("  - Bolsillos filtrados:", bolsillos.length);
                console.log("  - Categorías filtradas:", categorias.length);
                console.log("  - Ingresos filtrados:", ingresos.length);
                console.log("  - Egresos filtrados:", egresos.length);
                console.log("  - Grupos (del backend):", grupos.length);
            } else {
                console.warn("⚠️ No se pudo obtener el ID del usuario, mostrando todos los datos");
                bolsillos = bolsillosData;
                categorias = categoriasData;
                ingresos = ingresosData;
                egresos = egresosData;
                grupos = gruposData;
            }

            dashboardHandlers.refreshDashboard();
        } catch (err) {
            console.error("❌ Error cargando datos:", err);
            if (err.message.includes("401")) {
                window.location.href = "index.html";
            }
        }
    };

    // Password Toggle
    const togglePassword = (e) => {
        const btn = e.currentTarget;
        const input = document.getElementById(btn.getAttribute("aria-controls"));
        const type = input.type === "password" ? "text" : "password";
        input.type = type;
        btn.textContent = type === "password" ? "👁️" : "🔒";
    };

    // Initialization
    const init = () => {
        // Attach event listeners
        $("#loginForm")?.addEventListener("submit", formHandlers.submitLogin);
        $("#registerForm")?.addEventListener("submit", formHandlers.submitRegister);
        $("#transaccionForm")?.addEventListener("submit", formHandlers.submitTransaccion);
        $("#categoriaForm")?.addEventListener("submit", formHandlers.submitCategoria);
        $("#bolsilloForm")?.addEventListener("submit", formHandlers.submitBolsillo);
        $("#grupoForm")?.addEventListener("submit", formHandlers.submitGrupo);

        $$("[data-toggle='password']").forEach(btn => {
            btn.addEventListener("click", togglePassword);
        });

        // Setup modals
        modalHandlers.attachModalListeners();
        $("#nuevaTransaccionBtn")?.addEventListener("click", () => modalHandlers.showModal("transaccionModal"));
        $("#nuevaTransaccionBtn2")?.addEventListener("click", () => modalHandlers.showModal("transaccionModal"));
        // Resetear formularios al abrir modal para creación
        $("#nuevaCategoriaBtn")?.addEventListener("click", () => {
            const modalTitle = document.querySelector('#categoriaModal .modal__title');
            if (modalTitle) modalTitle.textContent = 'Nueva Categoría';
            $('#categoriaForm').reset();
            $('#categoriaId').value = '';
            modalHandlers.showModal("categoriaModal");
        });
        $("#nuevaCategoriaBtn2")?.addEventListener("click", () => {
            const modalTitle = document.querySelector('#categoriaModal .modal__title');
            if (modalTitle) modalTitle.textContent = 'Nueva Categoría';
            $('#categoriaForm').reset();
            $('#categoriaId').value = '';
            modalHandlers.showModal("categoriaModal");
        });
        $("#nuevoBolsilloBtn")?.addEventListener("click", () => {
            const modalTitle = document.querySelector('#bolsilloModal .modal__title');
            if (modalTitle) modalTitle.textContent = 'Nuevo Bolsillo';
            $('#bolsilloForm').reset();
            $('#bolsilloId').value = '';
            $('#saldoBolsillo').disabled = false;
            modalHandlers.showModal("bolsilloModal");
        });
        $("#nuevoBolsilloBtn2")?.addEventListener("click", () => {
            const modalTitle = document.querySelector('#bolsilloModal .modal__title');
            if (modalTitle) modalTitle.textContent = 'Nuevo Bolsillo';
            $('#bolsilloForm').reset();
            $('#bolsilloId').value = '';
            $('#saldoBolsillo').disabled = false;
            modalHandlers.showModal("bolsilloModal");
        });
        $("#nuevoGrupoBtn")?.addEventListener("click", () => {
            modalHandlers.clearGroupMembers(); // Limpiar miembros al abrir modal
            modalHandlers.showModal("grupoModal");
        });
        $("#nuevoGrupoBtn2")?.addEventListener("click", () => {
            modalHandlers.clearGroupMembers(); // Limpiar miembros al abrir modal
            modalHandlers.showModal("grupoModal");
        });

        // Botón para añadir miembros al grupo
        $("#addMemberBtn")?.addEventListener("click", () => modalHandlers.addMemberToGroup());
        
        // Permitir añadir con Enter en el campo de email
        $("#emailMiembro")?.addEventListener("keypress", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                modalHandlers.addMemberToGroup();
            }
        });

        // Botón editar grupo en detalles
        const btnEditarGrupo = document.getElementById('btnEditarGrupo');
        if (btnEditarGrupo) {
            btnEditarGrupo.addEventListener('click', () => {
                const grupoId = document.getElementById('detallesGrupoId').value;
                if (grupoId) {
                    crudOperations.editarGrupo(parseInt(grupoId, 10));
                }
            });
        }

        // Manejador para cerrar modales
        const closeButtons = document.querySelectorAll('.btn-close-modal, .modal-backdrop');
        closeButtons.forEach(btn => {
            btn.addEventListener('click', (e) => {
                // Si se hizo clic en el backdrop, asegúrate de que no sea en el contenido del modal
                if (e.target.classList.contains('modal-backdrop') || e.target.closest('.btn-close-modal')) {
                    const modal = btn.closest('.modal-container');
                    if(modal) {
                        modalHandlers.hideModal(modal.id);
                    } else if (e.target.classList.contains('modal-container')) {
                        // Caso especial para cuando el backdrop es el elemento clickeado
                        modalHandlers.hideModal(e.target.id);
                    }
                }
            });
        });
        
        const btnCerrarDetalles = document.getElementById('btnCerrarDetalles');
        if(btnCerrarDetalles) {
            btnCerrarDetalles.addEventListener('click', () => modalHandlers.hideModal('detallesGrupoModal'));
        }
        
        const btnCerrarEditar = document.getElementById('btnCerrarEditar');
        if(btnCerrarEditar) {
            btnCerrarEditar.addEventListener('click', () => modalHandlers.hideModal('editarGrupoModal'));
        }

        // Botón añadir miembro en editar grupo
        $("#btnAnadirMiembro")?.addEventListener("click", () => {
            modalHandlers.addEditMember();
        });

        // Form editar grupo
        $("#editarGrupoForm")?.addEventListener("submit", formHandlers.submitEditarGrupo);

        // Setup navigation
        viewHandlers.attachViewListeners();
        viewHandlers.initFromHash();

        // Load user info
        currentUser = ApiClient.getStoredUser();
        if (currentUser) {
            $(".user__name").textContent = currentUser.nombre;
            $(".user__email").textContent = currentUser.email;
        }

        // Setup logout
        $("#logoutBtn")?.addEventListener("click", async () => {
            const confirmar = confirm("¿Estás seguro de que deseas cerrar sesión?");
            if (confirmar) {
                console.log("🚪 Cerrando sesión...");
                
                // Agregar clase de carga al botón
                const logoutBtn = $("#logoutBtn");
                if (logoutBtn) {
                    logoutBtn.textContent = "⏳";
                    logoutBtn.disabled = true;
                }
                
                // Simular un pequeño delay para efecto visual
                await new Promise(resolve => setTimeout(resolve, 500));
                
                // Limpiar datos de sesión
                ApiClient.logout();
                console.log("✅ Sesión cerrada. Redirigiendo al login...");
                
                // Redirigir al login
                window.location.href = "index.html";
            }
        });

        // Initial data load for dashboard
        if (window.location.pathname.includes("dashboard.html")) {
            loadData();
        }
    };

    // CRUD Functions
    const crudOperations = {
        // Categorías
        editCategoria: async (id) => {
            const categoria = categorias.find(c => c.id === id);
            if (!categoria) {
                alert("Categoría no encontrada");
                return;
            }
            
            // Pre-poblar el formulario
            $("#categoriaId").value = categoria.id;
            $("#nombreCategoria").value = categoria.nombre;
            $("#tipoCategoria").value = categoria.tipo;
            
            // Cambiar título del modal
            const modalTitle = document.querySelector("#categoriaModal .modal__title");
            if (modalTitle) modalTitle.textContent = "Editar Categoría";
            
            // Mostrar modal
            modalHandlers.showModal("categoriaModal");
        },
        
        deleteCategoria: async (id) => {
            if (!confirm("¿Estás seguro de que deseas eliminar esta categoría?")) return;
            
            try {
                await ApiClient.categorias.eliminar(id);
                await loadData();
                dashboardHandlers.refreshDashboard();
                viewRenderers.renderCategorias();
            } catch (err) {
                alert(`Error al eliminar categoría: ${err.message}`);
            }
        },

        // Bolsillos
        editBolsillo: async (id) => {
            const bolsillo = bolsillos.find(b => b.id === id);
            if (!bolsillo) {
                alert("Bolsillo no encontrado");
                return;
            }
            
            // Pre-poblar el formulario
            $("#bolsilloId").value = bolsillo.id;
            $("#nombreBolsillo").value = bolsillo.nombre;
            $("#saldoBolsillo").value = bolsillo.saldo;
            
            // Cambiar título del modal
            const modalTitle = document.querySelector("#bolsilloModal .modal__title");
            if (modalTitle) modalTitle.textContent = "Editar Bolsillo";
            
            // Mostrar modal
            modalHandlers.showModal("bolsilloModal");
        },
        
        deleteBolsillo: async (id) => {
            if (!confirm("¿Estás seguro de que deseas eliminar este bolsillo?")) return;
            
            try {
                await ApiClient.bolsillos.eliminar(id);
                await loadData();
                dashboardHandlers.refreshDashboard();
                viewRenderers.renderBolsillos();
            } catch (err) {
                alert(`Error al eliminar bolsillo: ${err.message}`);
            }
        },

        // Ingresos
        editIngreso: async (id) => {
            const ingreso = ingresos.find(i => i.id === id);
            if (!ingreso) {
                alert("Ingreso no encontrado");
                return;
            }
            
            // Pre-poblar el formulario
            $("#transaccionId").value = ingreso.id;
            $("#transaccionTipoOriginal").value = "ingreso";
            $("#tipo").value = "ingreso";
            $("#descripcion").value = ingreso.descripcion;
            $("#monto").value = ingreso.monto;
            $("#categoriaId").value = ingreso.categoriaId;
            $("#bolsilloId").value = ingreso.bolsilloId;
            
            // Cambiar título del modal
            const modalTitle = document.querySelector("#transaccionModal .modal__title");
            if (modalTitle) modalTitle.textContent = "Editar Ingreso";
            
            // Poblar selectores
            modalHandlers.populateTransaccionSelects();
            
            // Volver a setear los valores después de poblar
            $("#categoriaId").value = ingreso.categoriaId;
            $("#bolsilloId").value = ingreso.bolsilloId;
            
            // Mostrar modal
            modalHandlers.showModal("transaccionModal");
        },
        
        deleteIngreso: async (id) => {
            if (!confirm("¿Estás seguro de que deseas eliminar este ingreso?")) return;
            
            try {
                await ApiClient.ingresos.eliminar(id);
                await loadData();
                dashboardHandlers.refreshDashboard();
                viewRenderers.renderTransacciones();
            } catch (err) {
                alert(`Error al eliminar ingreso: ${err.message}`);
            }
        },

        // Egresos
        editEgreso: async (id) => {
            const egreso = egresos.find(e => e.id === id);
            if (!egreso) {
                alert("Egreso no encontrado");
                return;
            }
            
            // Pre-poblar el formulario
            $("#transaccionId").value = egreso.id;
            $("#transaccionTipoOriginal").value = "egreso";
            $("#tipo").value = "egreso";
            $("#descripcion").value = egreso.descripcion;
            $("#monto").value = egreso.monto;
            $("#categoriaId").value = egreso.categoriaId;
            $("#bolsilloId").value = egreso.bolsilloId;
            
            // Cambiar título del modal
            const modalTitle = document.querySelector("#transaccionModal .modal__title");
            if (modalTitle) modalTitle.textContent = "Editar Gasto";
            
            // Poblar selectores
            modalHandlers.populateTransaccionSelects();
            
            // Volver a setear los valores después de poblar
            $("#categoriaId").value = egreso.categoriaId;
            $("#bolsilloId").value = egreso.bolsilloId;
            
            // Mostrar modal
            modalHandlers.showModal("transaccionModal");
        },
        
        deleteEgreso: async (id) => {
            if (!confirm("¿Estás seguro de que deseas eliminar este egreso?")) return;
            
            try {
                await ApiClient.egresos.eliminar(id);
                await loadData();
                dashboardHandlers.refreshDashboard();
                viewRenderers.renderTransacciones();
            } catch (err) {
                alert(`Error al eliminar egreso: ${err.message}`);
            }
        },

        // Grupos
        verDetallesGrupo: async (id) => {
            try {
                console.log("🔍 Obteniendo detalles del grupo ID:", id);
                
                // Buscar el grupo en el array local
                const grupo = grupos.find(g => g.id === id);
                
                if (!grupo) {
                    console.error("❌ Grupo no encontrado:", id);
                    alert("Grupo no encontrado");
                    return;
                }
                
                console.log("📊 Datos del grupo:", grupo);
                
                // Guardar el ID del grupo en el campo oculto
                $("#detallesGrupoId").value = grupo.id;
                
                // Llenar información del grupo
                $("#detallesGrupoTitulo").textContent = grupo.nombre;
                $("#detallesNombre").textContent = grupo.nombre;
                $("#detallesDescripcion").textContent = grupo.descripcion || "Sin descripción";
                $("#detallesFecha").textContent = new Date(grupo.fechaCreacion).toLocaleDateString('es-ES', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                });
                $("#detallesCantidad").textContent = `${grupo.cantidadMiembros || 0} miembros`;
                
                // Renderizar lista de miembros
                const miembrosList = $("#detallesMiembrosList");
                
                if (!grupo.miembros || grupo.miembros.length === 0) {
                    miembrosList.innerHTML = `
                        <div class="empty-state-small">
                            <p>👥 No hay miembros en este grupo</p>
                        </div>
                    `;
                } else {
                    miembrosList.innerHTML = grupo.miembros.map(miembro => `
                        <div class="miembro-detalle-item">
                            <div class="miembro-avatar-large" style="background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);">
                                ${miembro.nombre.charAt(0).toUpperCase()}
                            </div>
                            <div class="miembro-info-detalle">
                                <div class="miembro-nombre-detalle">
                                    ${miembro.nombre}
                                    ${miembro.rol === 'ADMIN' ? '<span class="admin-badge">👑 Admin</span>' : ''}
                                </div>
                                <div class="miembro-email-detalle">${miembro.email}</div>
                                <div class="miembro-rol-detalle">
                                    <span class="rol-badge ${miembro.rol.toLowerCase()}">${miembro.rol}</span>
                                </div>
                            </div>
                        </div>
                    `).join('');
                }
                
                // Mostrar modal
                modalHandlers.showModal("detallesGrupoModal");
                
            } catch (err) {
                console.error("❌ Error al ver detalles del grupo:", err);
                alert(`Error al cargar detalles del grupo: ${err.message}`);
            }
        },
        
        salirGrupo: async (id) => {
            if (!confirm("¿Estás seguro de que deseas salir de este grupo?")) return;
            
            try {
                const user = ApiClient.getStoredUser();
                if (!user || !user.id) {
                    alert("Error: No se pudo obtener el usuario actual");
                    return;
                }

                console.log(`🚪 Saliendo del grupo ${id}...`);
                
                // Eliminar la relación usuario-grupo
                await ApiClient.usuarioGrupo.eliminarRelacion(user.id, id);
                
                console.log("✅ Saliste del grupo exitosamente");
                
                // Recargar datos y actualizar vistas
                await loadData();
                dashboardHandlers.refreshDashboard();
                viewRenderers.renderGrupos();
                
                alert("Has salido del grupo exitosamente");
            } catch (err) {
                console.error("❌ Error al salir del grupo:", err);
                alert(`Error al salir del grupo: ${err.message}`);
            }
        },

        editarGrupo: (id) => {
            try {
                // Si no se pasa ID, intentar obtenerlo del campo oculto del modal de detalles
                if (!id) {
                    id = parseInt($("#detallesGrupoId").value, 10);
                }
                
                if (!id) {
                    alert("No se pudo cargar la información del grupo");
                    return;
                }
                
                // Buscar el grupo por ID
                const grupo = grupos.find(g => g.id === id);
                
                if (!grupo) {
                    alert("No se pudo cargar la información del grupo");
                    return;
                }
                
                console.log("✏️ Editando grupo:", grupo);
                
                // Llenar formulario de edición
                $("#editGrupoId").value = grupo.id;
                $("#editNombreGrupo").value = grupo.nombre;
                $("#editDescripcionGrupo").value = grupo.descripcion || "";
                
                // Renderizar miembros actuales
                const editMembersList = $("#editMiembrosList");
                editMembersList.innerHTML = grupo.miembros && grupo.miembros.length > 0
                    ? grupo.miembros.map(miembro => `
                        <div class="edit-member-item" data-email="${miembro.email}" data-existing="true">
                            <div class="member-avatar">${miembro.nombre.charAt(0).toUpperCase()}</div>
                            <div class="member-details">
                                <div class="member-name">${miembro.nombre}</div>
                                <div class="edit-member-email">${miembro.email}</div>
                            </div>
                            <span class="member-badge ${miembro.rol.toLowerCase()}-badge">${miembro.rol}</span>
                        </div>
                    `).join('')
                    : '<p class="empty-message-small">Sin miembros</p>';
                
                // Cerrar modal de detalles y abrir modal de edición
                modalHandlers.hideModal("detallesGrupoModal");
                modalHandlers.showModal("editarGrupoModal");
                
            } catch (err) {
                console.error("❌ Error al preparar edición:", err);
                alert("Error al cargar los datos del grupo");
            }
        },

        // Categorías
        editarCategoria: (id) => {
            const categoria = categorias.find(c => c.id === id);
            if (!categoria) {
                alert('Categoría no encontrada');
                return;
            }
            
            console.log("✏️ Editando categoría:", categoria);
            
            // Convertir tipo de BD a formulario: "ing" -> "ingreso", "eg" -> "gasto"
            const tipoFormulario = categoria.tipo === 'ing' ? 'ingreso' : 'gasto';
            
            // Llenar el formulario
            $('#categoriaId').value = categoria.id;
            $('#nombreCategoria').value = categoria.nombre;
            $('#tipoCategoria').value = tipoFormulario;
            
            console.log("📝 Valores cargados en formulario:", {
                id: $('#categoriaId').value,
                nombre: $('#nombreCategoria').value,
                tipo: $('#tipoCategoria').value
            });
            
            // Cambiar título del modal
            const modalTitle = document.querySelector('#categoriaModal .modal__title');
            if (modalTitle) modalTitle.textContent = 'Editar Categoría';
            
            // Mostrar modal
            modalHandlers.showModal('categoriaModal');
        },

        eliminarCategoria: async (id) => {
            if (!confirm('¿Estás seguro de que quieres eliminar esta categoría?')) return;
            
            try {
                await ApiClient.categorias.eliminar(id);
                await loadData();
                dashboardHandlers.refreshDashboard();
                viewRenderers.renderCategorias();
            } catch (err) {
                alert(`Error al eliminar categoría: ${err.message}`);
            }
        },

        // Bolsillos
        editarBolsillo: (id) => {
            const bolsillo = bolsillos.find(b => b.id === id);
            if (!bolsillo) {
                alert('Bolsillo no encontrado');
                return;
            }
            
            console.log("✏️ Editando bolsillo:", bolsillo);
            
            // Llenar el formulario
            $('#bolsilloId').value = bolsillo.id;
            $('#nombreBolsillo').value = bolsillo.nombre;
            $('#saldoBolsillo').value = bolsillo.saldo;
            $('#saldoBolsillo').disabled = true; // El saldo inicial no se puede editar
            
            // Cambiar título del modal
            const modalTitle = document.querySelector('#bolsilloModal .modal__title');
            if (modalTitle) modalTitle.textContent = 'Editar Bolsillo';
            
            // Mostrar modal
            modalHandlers.showModal('bolsilloModal');
        },

        eliminarBolsillo: async (id) => {
            if (!confirm('¿Estás seguro de que quieres eliminar este bolsillo?')) return;
            
            try {
                console.log('🗑️ Eliminando bolsillo ID:', id);
                await ApiClient.bolsillos.eliminar(id);
                console.log('✅ Bolsillo eliminado exitosamente');
                await loadData();
                dashboardHandlers.refreshDashboard();
                viewRenderers.renderBolsillos();
                alert('✅ Bolsillo eliminado exitosamente');
            } catch (err) {
                console.error('❌ Error al eliminar bolsillo:', err);
                // Si es error 409 (Conflict), el bolsillo tiene transacciones
                if (err.message.includes("409")) {
                    alert('❌ No se puede eliminar este bolsillo porque tiene transacciones asociadas.\n\nPrimero debes eliminar todas las transacciones de este bolsillo.');
                }
                // No mostrar alerta si es error de autenticación (el API ya redirige al login)
                else if (!err.message.includes("403") && !err.message.includes("401")) {
                    alert(`Error al eliminar bolsillo: ${err.message}`);
                }
            }
        }
    };

    // Public API
    return { 
        init,
        ...crudOperations,
        // Exponer funciones de gestión de miembros
        removeMemberFromGroup: (index) => modalHandlers.removeMemberFromGroup(index),
        removeEditMember: (email) => modalHandlers.removeEditMember(email)
    };
})();

// Initialize app
document.addEventListener("DOMContentLoaded", () => {
    app.init();
    
    // Cerrar modales al hacer clic en el backdrop
    document.addEventListener("click", (e) => {
        if (e.target.classList.contains("modal") && e.target.classList.contains("active")) {
            const modalId = e.target.id;
            console.log(`🎭 Cerrando modal por clic en backdrop: ${modalId}`);
            e.target.classList.remove("active");
            
            // Limpiar formulario
            const form = e.target.querySelector("form");
            if (form) form.reset();
            
            // Limpiar campos ocultos
            const hiddenInputs = e.target.querySelectorAll('input[type="hidden"]');
            hiddenInputs.forEach(input => input.value = "");
        }
    });
    
    // Cerrar modales con tecla ESC
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            const activeModal = document.querySelector(".modal.active");
            if (activeModal) {
                console.log(`🎭 Cerrando modal por tecla ESC: ${activeModal.id}`);
                activeModal.classList.remove("active");
                
                // Limpiar formulario
                const form = activeModal.querySelector("form");
                if (form) form.reset();
                
                // Limpiar campos ocultos
                const hiddenInputs = activeModal.querySelectorAll('input[type="hidden"]');
                hiddenInputs.forEach(input => input.value = "");
            }
        }
    });
});
