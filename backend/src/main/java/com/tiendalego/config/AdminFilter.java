package com.tiendalego.config;

import com.tiendalego.model.Usuario;
import com.tiendalego.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Component
public class AdminFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepo;

    private static final Set<String> ADMIN_PATHS = Set.of(
            "/api/v1/productos", "/api/v1/categorias", "/api/v1/imagenes"
    );

    public AdminFilter(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (requiereAdmin(request)) {
            String usuarioId = request.getHeader("X-Usuario-Id");
            if (usuarioId == null || usuarioId.isBlank()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Se requiere autenticacion de administrador\"}");
                return;
            }

            Optional<Usuario> usuario = usuarioRepo.findById(usuarioId);
            if (usuario.isEmpty() || !"ADMIN".equals(usuario.get().getRol())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"No tiene permisos de administrador\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiereAdmin(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.equals("/api/v1/pedidos") && "GET".equals(method)) {
            return true;
        }

        for (String adminPath : ADMIN_PATHS) {
            if (path.startsWith(adminPath) && Set.of("POST", "PUT", "DELETE").contains(method)) {
                return true;
            }
        }

        return false;
    }
}
