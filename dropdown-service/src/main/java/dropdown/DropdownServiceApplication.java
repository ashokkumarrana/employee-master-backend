package dropdown;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@EnableCaching
@SpringBootApplication
@ComponentScan(basePackages = {"dropdown", "common.redis"})
public class DropdownServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DropdownServiceApplication.class, args);
		System.out.println("Dropdown Service Application Started Successfully on Port 8084.....");
	}
}
