package pa.com.segurossura.logsandaudit.security.utils;

//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SecurityUtils {
    // Patrón de regex para encontrar un GUID/UUID en una URL.
    private static final Pattern GUID_PATTERN = Pattern.compile(
            "([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})"
    );

    private SecurityUtils() {}
    /*
    public static String getCurrentUserLogin() {

        var securityContext = SecurityContextHolder.getContext();
        var authentication = securityContext.getAuthentication();

        if (authentication != null && authentication.getPrincipal() != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            }
            if (principal instanceof String) {
                return (String) principal;
            }
        }

        return "anonymous"; // Un usuario por defecto si no hay contexto de seguridad
    }
    */

    /**
     * Extrae el primer GUID que encuentra en una cadena de texto.
     * @param text El texto del cual extraer el GUID (por ejemplo, una URL).
     * @return Un Optional con el GUID o un Optional vacío si no se encuentra.
     */
    private static Optional<String> extractGuid(final String text) {
        if (text == null || text.isEmpty()) {
            return Optional.empty();
        }
        final Matcher matcher = GUID_PATTERN.matcher(text);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    /**
     * Obtiene el objeto Jwt del contexto de seguridad actual.
     * @return Un Optional que contiene el Jwt si el usuario está autenticado, o vacío en caso contrario.
     */
    private static Optional<Jwt> getCurrentJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            return Optional.of((Jwt) authentication.getPrincipal());
        }
        return Optional.empty();
    }

    public static String getCurrentUserLogin() {
        String username = getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("upn") != null ? jwt.getClaimAsString("upn") : jwt.getClaimAsStringList("emails").getFirst() )
                .orElse("UNKNOWN");
        return username;
    }

    public static String getCurrentUserObjectId() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("oid")).orElse("UNKNOWN");
    }

    /**
     * Obtiene el nombre de pila del usuario.
     * @return El 'given_name' del token o un Optional vacío.
     */
    public static Optional<String> getCurrentUserGivenName() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("given_name"));
    }

    public static List<String> getRolesFromJwt() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsStringList("role")).orElse(null);
    }

    public static List<String> getExtensionRoleFromJwt() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsStringList("extension_role")).orElse(null);
    }

    public static List<String> getScopesFromJwt() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsStringList("scp")).orElse(null);
    }

    public static List<String> getRolesFromAuthentication() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .toList();
        }

        return null;
    }

    /**
     * Obtiene el Tenant ID del directorio de Azure AD B2C desde el claim 'iss' (issuer).
     * @return Un Optional que contiene el Tenant ID de B2C o un Optional vacío si no se encuentra.
     */
    public static String getB2cTenantId() {
        // El claim 'iss' es una URL que contiene el Tenant ID del tenant B2C.
        return getCurrentJwt()
                .map(jwt -> jwt.getIssuer().toString())
                .flatMap(SecurityUtils::extractGuid).orElse("UNKNOWN");
    }

    /**
     * Obtiene el Tenant ID del proveedor de identidad (IdP) desde el claim 'idp'.
     * Esto es útil cuando se usa federación (por ejemplo, login con una cuenta de otro tenant de Azure).
     * @return Un Optional que contiene el Tenant ID del IdP o un Optional vacío si no se encuentra.
     */
    public static String getIdpTenantId() {
        // El claim 'idp' es una URL que contiene el Tenant ID del proveedor de identidad.
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("idp"))
                .flatMap(SecurityUtils::extractGuid).orElse("UNKNOWN");
    }
}