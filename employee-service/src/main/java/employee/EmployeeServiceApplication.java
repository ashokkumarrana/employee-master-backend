package employee;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
@EnableCaching
@EnableAsync
@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {"employee", "common.service", "common.listener", "common.redis"})
public class EmployeeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeeServiceApplication.class, args);
		System.out.println("Employee Service Application Started Successfully on Port 8086....");
	}
}
