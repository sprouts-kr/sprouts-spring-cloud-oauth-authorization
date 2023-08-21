package kr.sprouts.framework.oauth.authorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
public class AuthorizationApplication {
    @PostConstruct
    protected void initialize() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Seoul")));
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthorizationApplication.class, args);
    }
}
