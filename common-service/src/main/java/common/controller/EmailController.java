package common.controller;
import common.dto.ApiResponseDto;
import common.dto.EmailRequestDto;
import common.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/common/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponseDto<Void>> send(@Valid @RequestBody EmailRequestDto request) {
        emailService.sendAsync(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponseDto<>(HttpStatus.ACCEPTED.value(), "Email queued for delivery", null, "none"));
    }
}
