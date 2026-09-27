package employee.client;
import employee.config.FeignConfig;
import employee.dto.ApiResponseDto;
import employee.dto.DropdownResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
        name = "dropdown-service",
        url = "http://localhost:8084",
        configuration = FeignConfig.class
)
public interface DropdownServiceClient {

    @GetMapping("/dropdown/department/list")
    ApiResponseDto<List<DropdownResponseDto>> getDepartments();

    @GetMapping("/dropdown/designation/list")
    ApiResponseDto<List<DropdownResponseDto>> getDesignations();

    @GetMapping("/dropdown/state/list")
    ApiResponseDto<List<DropdownResponseDto>> getStates();

    @GetMapping("/dropdown/country/list")
    ApiResponseDto<List<DropdownResponseDto>> getCountries();
}
