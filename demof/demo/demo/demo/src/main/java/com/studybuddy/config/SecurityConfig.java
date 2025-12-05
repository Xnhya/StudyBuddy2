package com.studybuddy.config;

import com.studybuddy.security.CustomUserDetailsService;
import com.studybuddy.security.CustomLoginSuccessHandler;
import com.studybuddy.security.JwtAuthenticationFilter;
import com.studybuddy.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize en controladores
public class SecurityConfig {
    
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, 
                         JwtTokenProvider jwtTokenProvider) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService);
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomLoginSuccessHandler customLoginSuccessHandler) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/static/**").permitAll()
                // Páginas públicas
                .requestMatchers("/", "/home", "/about", "/contact", "/register", "/login").permitAll()
                .requestMatchers("/auth/**").permitAll() // Rutas de login/registro web
                // Endpoints de API públicas
                .requestMatchers("/api/auth/**", "/api/public/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // APIs externas (requieren autenticación pero cualquier rol)
                .requestMatchers("/api/external/**").authenticated()
                // Rutas de Admin (solo ADMIN)
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                // Rutas de Moderador (MODERATOR o ADMIN)
                .requestMatchers("/moderator/**", "/api/moderator/**").hasAnyRole("MODERATOR", "ADMIN")
                // Rutas de usuario autenticado
                .requestMatchers("/user/**", "/api/users/**").authenticated()
                // Rutas de grupos (autenticados)
                .requestMatchers("/groups/**", "/api/groups/**").authenticated()
                // Resto requiere autenticación
                .anyRequest().authenticated()
            )
            // Configuración Login WEB (Formulario HTML)
            .formLogin(form -> form
                // CORRECCIÓN: Cambiado de "/auth/login" a "/login" para coincidir con ViewController
                .loginPage("/login") 
                .loginProcessingUrl("/auth/login") // Esto se mantiene igual porque el formulario hace POST aquí
                .successHandler(customLoginSuccessHandler) // Usar CustomLoginSuccessHandler para manejar 2FA
                // CORRECCIÓN: Cambiado de "/auth/login..." a "/login..."
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .permitAll()
            );
            
        // Añadir filtro JWT para las peticiones API
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}