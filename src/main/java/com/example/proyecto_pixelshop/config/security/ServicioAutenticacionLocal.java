package com.example.proyecto_pixelshop.config.security;

import com.example.proyecto_pixelshop.model.Usuario;
import com.example.proyecto_pixelshop.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// Servicio de autenticación local (email y contraseña)
// Carga los usuarios desde la base de datos para el login tradicional, verifica que estén activos y asigna roles
@Service
public class ServicioAutenticacionLocal implements UserDetailsService {
    
    @Autowired private UsuarioRepository usuarioRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Buscar solo usuarios activos
        Usuario usuario = usuarioRepository.findByEmailAndActivo(email, true)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado o inactivo: " + email));
        
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
        
        return new User(
            usuario.getEmail(),
            usuario.getPassword(),
            authorities
        );
    }
}
