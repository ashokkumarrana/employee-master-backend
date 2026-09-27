package dropdown.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DropdownRequestDto {
    private Long id;
    @NotBlank(message = "Name is required")
    private String name;
}