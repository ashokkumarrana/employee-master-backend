package dropdown.controller;
import dropdown.dto.ApiResponseDto;
import dropdown.dto.DropdownRequestDto;
import dropdown.dto.DropdownResponseDto;
import dropdown.service.DropdownService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dropdown")
public class DropdownController {

    private final DropdownService dropdownService;
    public DropdownController(DropdownService dropdownService) {
        this.dropdownService = dropdownService;
    }

    @GetMapping("/department/list")
    public ResponseEntity<ApiResponseDto<List<DropdownResponseDto>>> getDepartments() {
        return ResponseEntity.ok(dropdownService.getDepartments());
    }

    @PostMapping("/department/save")
    public ResponseEntity<ApiResponseDto<DropdownResponseDto>> saveDepartment(@Valid @RequestBody DropdownRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dropdownService.saveDepartment(requestDto));
    }

    @GetMapping("/designation/list")
    public ResponseEntity<ApiResponseDto<List<DropdownResponseDto>>> getDesignations() {
        return ResponseEntity.ok(dropdownService.getDesignations());
    }

    @PostMapping("/designation/save")
    public ResponseEntity<ApiResponseDto<DropdownResponseDto>> saveDesignation(@Valid @RequestBody DropdownRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dropdownService.saveDesignation(requestDto));
    }

    @GetMapping("/state/list")
    public ResponseEntity<ApiResponseDto<List<DropdownResponseDto>>> getStates() {
        return ResponseEntity.ok(dropdownService.getStates());
    }

    @PostMapping("/state/save")
    public ResponseEntity<ApiResponseDto<DropdownResponseDto>> saveState(@Valid @RequestBody DropdownRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dropdownService.saveState(requestDto));
    }

    @GetMapping("/country/list")
    public ResponseEntity<ApiResponseDto<List<DropdownResponseDto>>> getCountries() {
        return ResponseEntity.ok(dropdownService.getCountries());
    }

    @PostMapping("/country/save")
    public ResponseEntity<ApiResponseDto<DropdownResponseDto>> saveCountry(@Valid @RequestBody DropdownRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dropdownService.saveCountry(requestDto));
    }

    @PostMapping("/city/save")
    public ResponseEntity<ApiResponseDto<DropdownResponseDto>> saveCity(@Valid @RequestBody DropdownRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dropdownService.saveCity(requestDto));
    }
    @GetMapping("/city/list")
    public ResponseEntity<ApiResponseDto<List<DropdownResponseDto>>> getCities() {
        return ResponseEntity.ok(dropdownService.getCities());
    }
}