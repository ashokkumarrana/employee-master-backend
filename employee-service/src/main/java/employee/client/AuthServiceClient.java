package employee.client;
import employee.config.FeignConfig;
import employee.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", url = "${AUTH_SERVICE_URL:http://localhost:8082}", configuration = FeignConfig.class)
public interface AuthServiceClient {

    @GetMapping("/auth/user/id/{id}")
    UserResponseDto getUserById(@PathVariable Long id);
}