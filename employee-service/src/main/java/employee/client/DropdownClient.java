package employee.client;
import employee.dto.ApiResponseDto;
import employee.dto.DropdownResponseDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DropdownClient {

    private final DropdownServiceClient dropdownServiceClient;

    public List<DepartmentDto> getDepartments() {
        ApiResponseDto<List<DropdownResponseDto>> response = dropdownServiceClient.getDepartments();
        if (response == null || response.getData() == null) {
            return List.of();
        }
        return response.getData().stream().map(dto -> {
            DepartmentDto department = new DepartmentDto();
            department.setId(dto.getId());
            department.setName(dto.getName());
            return department;
        }).toList();
    }

    public Map<Long, String> getDepartmentMap() {
        return toMap(dropdownServiceClient.getDepartments());
    }

    public Map<Long, String> getDesignationMap() {
        return toMap(dropdownServiceClient.getDesignations());
    }

    public Map<Long, String> getStateMap() {
        return toMap(dropdownServiceClient.getStates());
    }

    public Map<Long, String> getCountryMap() {
        return toMap(dropdownServiceClient.getCountries());
    }

    private Map<Long, String> toMap(ApiResponseDto<List<DropdownResponseDto>> response) {
        if (response == null || response.getData() == null) {
            return Map.of();
        }
        return response.getData().stream().collect(Collectors.toMap(DropdownResponseDto::getId, DropdownResponseDto::getName));
    }

    @Data
    public static class DepartmentDto {
        private Long id;
        private String name;
    }
}
