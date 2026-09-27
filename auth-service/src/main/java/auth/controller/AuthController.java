package auth.controller;
import auth.dto.LoginRequestDto;
import auth.dto.LoginResponseDto;
import auth.dto.UserResponseDto;
import auth.entity.User;
import auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto request) {
        return userService.login(request);
    }

    @GetMapping("/user/{username}")
    public UserResponseDto getUser(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @GetMapping("/user/id/{id}")
    public UserResponseDto getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}
