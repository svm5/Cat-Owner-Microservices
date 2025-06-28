package labs.externalmicroservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "Cat Owner API",
                description = "API of service with owners and their pets",
                version = "1.0.0",
                contact = @Contact(
                        name = "Smirnova Svetlana"
                )
        )
)
@SecurityScheme(
        type = SecuritySchemeType.HTTP,
        name = "basicAuth",
        scheme = "basic")
public class OpenApiConfig {
}
