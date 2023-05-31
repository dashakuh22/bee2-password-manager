package by.bsu.bee2.bee2passwordmanager;

import by.bsu.bee2.bee2passwordmanager.entity.User;
import by.bsu.bee2.bee2passwordmanager.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@ComponentScan(basePackages = "by.bsu.bee2.bee2passwordmanager.*")
public class Bee2PasswordManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(Bee2PasswordManagerApplication.class, args);
    }

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void initAfterAll() {
        userRepository.save(new User("user",
			"$2a$10$dw1pDU16gqSBC9FHF5HbaONKw3RCHQYEIaMrE5a7gEXl9n9020.yi",
            "f0b8c03c-e291-11ed-b5ea-0242ac120002"
            ));
    }

}
