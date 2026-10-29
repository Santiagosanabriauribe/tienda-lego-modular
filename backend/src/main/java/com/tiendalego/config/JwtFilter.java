package com.tiendalego.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    public JwtFilter(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if (c.getName().equals("token")) {
                    try {
                        Claims datos = jwt.leer(c.getValue());
                        var auth = new UsernamePasswordAuthenticationToken(
                                datos.getSubject(), null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + datos.get("rol", String.class))));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } catch (JwtException e) {
                        // token invalido o vencido: se sigue sin autenticar
                    }
                }
            }
        }
        chain.doFilter(req, res);
    }
}
