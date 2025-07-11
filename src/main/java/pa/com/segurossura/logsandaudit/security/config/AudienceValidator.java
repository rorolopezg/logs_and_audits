package pa.com.segurossura.logsandaudit.security.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;
import java.util.List;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final List<String> allowedAudiences;
    public AudienceValidator(List<String> allowedAudiences) { Assert.notEmpty(allowedAudiences, "allowedAudiences cannot be empty"); this.allowedAudiences = allowedAudiences; }
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<String> tokenAudiences = jwt.getAudience();
        if (tokenAudiences != null && tokenAudiences.stream().anyMatch(allowedAudiences::contains)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "The required audience is missing or invalid", null));
    }
}
