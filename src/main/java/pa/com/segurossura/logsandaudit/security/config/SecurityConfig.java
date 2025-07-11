package pa.com.segurossura.logsandaudit.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

// package ...
// imports ...

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    // Inyecta las TRES propiedades
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.audiences}")
    private String[] audiences;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers( // Endpoints públicos
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(jwtDecoder())
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                    )
                    .authenticationEntryPoint(customAuthenticationEntryPoint) // Para errores 401
                )
                // --- SECCIÓN COMPLETA DE MANEJO DE EXCEPCIONES ---
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(customAccessDeniedHandler)          // Para errores 403
                );
        return http.build();
    }


    // En tu SecurityConfig.java

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite peticiones desde el origen de tu frontend (ajusta si es necesario)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:4200"));
        // Permite los métodos HTTP más comunes
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // Permite todas las cabeceras comunes, incluyendo Authorization
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "x-auth-token"));
        // Permite que las credenciales (como cookies o tokens) sean enviadas
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta configuración a todas las rutas de tu API
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }


    @Bean
    public JwtDecoder jwtDecoder() {
        //return createNimbusDecoder();
        // Envolvemos el decodificador real con nuestro decodificador de logging
        return new LoggingJwtDecoder(createNimbusDecoder());
    }

    /**
     * Método privado que contiene la lógica para construir el decodificador Nimbus.
     */
    private JwtDecoder createNimbusDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(Arrays.asList(audiences));
        OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator();

        DelegatingOAuth2TokenValidator<Jwt> combinedValidators =
                new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator, timestampValidator);

        jwtDecoder.setJwtValidator(combinedValidators);
        return jwtDecoder;
    }

    // ... tus otros beans (jwtAuthenticationConverter, etc.) sin cambios ...
    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return jwtConverter;
    }

    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return jwt -> {
            final Collection<String> roles = jwt.getClaimAsStringList("roles");
            Stream<GrantedAuthority> roleAuthorities = (roles != null) ?
                    roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)) :
                    Stream.empty();

            final Collection<String> extensionRole = jwt.getClaimAsStringList("extension_role");
            Stream<GrantedAuthority> extensionRoleAuthorities = (extensionRole != null) ?
                    extensionRole.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)) :
                    Stream.empty();

            final Collection<String> extensionRoles = jwt.getClaimAsStringList("extension_roles");
            Stream<GrantedAuthority> extensionRolesAuthorities = (extensionRoles != null) ?
                    extensionRoles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)) :
                    Stream.empty();

            final String scopeClaim = jwt.getClaimAsString("scp");
            Stream<GrantedAuthority> scopeAuthorities = (scopeClaim != null) ?
                    Arrays.stream(scopeClaim.split(" ")).map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope)) :
                    Stream.empty();

            Stream<GrantedAuthority> concat = Stream.concat(roleAuthorities, scopeAuthorities);
            concat = Stream.concat(extensionRoleAuthorities, concat);
            concat = Stream.concat(extensionRolesAuthorities, concat);
            return concat.collect(Collectors.toList());
        };
    }


}
