package dropdown.service;
import dropdown.dto.ApiResponseDto;
import dropdown.entity.Department;
import dropdown.entity.City;
import dropdown.entity.Country;
import dropdown.entity.Designation;
import dropdown.entity.State;
import dropdown.repository.*;
import dropdown.dto.DropdownRequestDto;
import dropdown.dto.DropdownResponseDto;
import dropdown.exception.DropdownException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DropdownService {
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final StateRepository stateRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    @CacheEvict(value = "departments", allEntries = true)
    public ApiResponseDto<DropdownResponseDto> saveDepartment(DropdownRequestDto requestDto) {
        if (Objects.isNull(requestDto)) {
            throw new DropdownException(400, "Request cannot be null", HttpStatus.BAD_REQUEST);
        }
        Department department = new Department();
        department.setDepartmentName(requestDto.getName());
        department.setDeleted(false);
        Department savedDepartment = departmentRepository.save(department);
        DropdownResponseDto data = toResponse(savedDepartment.getId(), savedDepartment.getDepartmentName());
        return new ApiResponseDto<>(HttpStatus.CREATED.value(), "Department saved successfully", data, "none");
    }
    @Cacheable(value = "departments")
    public ApiResponseDto<List<DropdownResponseDto>> getDepartments() {
        List<DropdownResponseDto> data = departmentRepository.findByDeletedFalse().stream().map(department -> toResponse(department.getId(), department.getDepartmentName())).toList();
        return new ApiResponseDto<>(HttpStatus.OK.value(), "Department dropdown fetched successfully", data, "none");
    }

    @CacheEvict(value = "designations", allEntries = true)
    public ApiResponseDto<DropdownResponseDto> saveDesignation(DropdownRequestDto requestDto) {
        if (Objects.isNull(requestDto)) {
            throw new DropdownException(400, "Request cannot be null", HttpStatus.BAD_REQUEST);
        }
        Designation designation = new Designation();
        designation.setDesignationName(requestDto.getName());
        designation.setDeleted(false);
        Designation savedDesignation = designationRepository.save(designation);
        DropdownResponseDto data = toResponse(savedDesignation.getId(), savedDesignation.getDesignationName());
        return new ApiResponseDto<>(HttpStatus.CREATED.value(), "Designation saved successfully", data, "none");
    }

    @Cacheable(value = "designations")
    public ApiResponseDto<List<DropdownResponseDto>> getDesignations() {
        List<DropdownResponseDto> data = designationRepository.findByDeletedFalse().stream().map(designation -> toResponse(designation.getId(), designation.getDesignationName())).toList();
        return new ApiResponseDto<>(HttpStatus.OK.value(), "Designation dropdown fetched successfully", data, "none");
    }

    @CacheEvict(value = "states", allEntries = true)
    public ApiResponseDto<DropdownResponseDto> saveState(DropdownRequestDto requestDto) {
        if (Objects.isNull(requestDto)) {
            throw new DropdownException(400, "Request cannot be null", HttpStatus.BAD_REQUEST);
        }
        State state = new State();
        state.setStateName(requestDto.getName());
        state.setDeleted(false);
        State savedState = stateRepository.save(state);
        DropdownResponseDto data = toResponse(savedState.getId(), savedState.getStateName());
        return new ApiResponseDto<>(HttpStatus.CREATED.value(), "State saved successfully", data, "none");
    }

    @Cacheable(value = "states")
    public ApiResponseDto<List<DropdownResponseDto>> getStates() {
        List<DropdownResponseDto> data = stateRepository.findByDeletedFalse().stream().map(state -> toResponse(state.getId(), state.getStateName())).toList();
        return new ApiResponseDto<>(HttpStatus.OK.value(), "State dropdown fetched successfully", data, "none");
    }

    @CacheEvict(value = "countries", allEntries = true)
    public ApiResponseDto<DropdownResponseDto> saveCountry(DropdownRequestDto requestDto) {
        if (Objects.isNull(requestDto)) {
            throw new DropdownException(400, "Request cannot be null", HttpStatus.BAD_REQUEST);
        }
        Country country = new Country();
        country.setCountryName(requestDto.getName());
        country.setDeleted(false);
        Country savedCountry = countryRepository.save(country);
        DropdownResponseDto data = toResponse(savedCountry.getId(), savedCountry.getCountryName());
        return new ApiResponseDto<>(HttpStatus.CREATED.value(), "Country saved successfully", data, "none");
    }

    @Cacheable(value = "countries")
    public ApiResponseDto<List<DropdownResponseDto>> getCountries() {
        List<DropdownResponseDto> data = countryRepository.findByDeletedFalse().stream().map(country -> toResponse(country.getId(), country.getCountryName())).toList();
        return new ApiResponseDto<>(HttpStatus.OK.value(), "Country dropdown fetched successfully", data, "none");
    }

    @CacheEvict(value = "cities", allEntries = true)
    public ApiResponseDto<DropdownResponseDto> saveCity(DropdownRequestDto requestDto) {
        if (Objects.isNull(requestDto)) {
            throw new DropdownException(400, "Request cannot be null", HttpStatus.BAD_REQUEST);
        }
        City city = new City();
        city.setCityName(requestDto.getName());
        city.setDeleted(false);
        City savedCity = cityRepository.save(city);
        DropdownResponseDto data = toResponse(savedCity.getId(), savedCity.getCityName());
        return new ApiResponseDto<>(HttpStatus.CREATED.value(), "City saved successfully", data, "none");
    }

    @Cacheable(value = "cities")
    public ApiResponseDto<List<DropdownResponseDto>> getCities() {
        List<DropdownResponseDto> data = cityRepository.findByDeletedFalse().stream().map(city -> toResponse(city.getId(), city.getCityName())).toList();
        return new ApiResponseDto<>(HttpStatus.OK.value(), "City dropdown fetched successfully", data, "none");
    }

    private DropdownResponseDto toResponse(Long id, String name) {
        DropdownResponseDto dto = new DropdownResponseDto();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }
}