package employee.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String userIdHeader = request.getHeader("X-User-Id");
            String role = request.getHeader("X-User-Role");
            String username = request.getHeader("X-User-Name");
            log.info("[EMPLOYEE-JWT] {} {} -> X-User-Id={}, X-User-Role={}, X-User-Name={}", request.getMethod(), request.getRequestURI(), userIdHeader, role, username);
            if (userIdHeader != null && !userIdHeader.isBlank()) {
                CurrentUserContext.setUserId(Long.valueOf(userIdHeader));
            }
            if (role != null && !role.isBlank()) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("[EMPLOYEE-JWT] Authentication set with authority ROLE_{}", role);
            } else {
                log.warn("[EMPLOYEE-JWT] No X-User-Role header present — request will be treated as unauthenticated");
            }
            filterChain.doFilter(request, response);
        } finally {
            CurrentUserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}

