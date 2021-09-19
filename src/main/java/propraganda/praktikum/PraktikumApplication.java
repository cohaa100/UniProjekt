package propraganda.praktikum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import propraganda.praktikum.logic.util.RoleInjector;

@SpringBootApplication
@SuppressWarnings("PMD")
@Slf4j
public class PraktikumApplication {

    final
    RoleInjector roleInjector;

    public PraktikumApplication(RoleInjector roleInjector) {
        this.roleInjector = roleInjector;
    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(PraktikumApplication.class, args);

    }

}
