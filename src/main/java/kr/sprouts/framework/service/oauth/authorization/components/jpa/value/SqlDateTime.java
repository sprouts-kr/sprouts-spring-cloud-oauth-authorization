package kr.sprouts.framework.service.oauth.authorization.components.jpa.value;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.Month;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SqlDateTime {
    public static final LocalDateTime MIN = LocalDateTime.of(1000, Month.JANUARY, 1, 0, 0);
    public static final LocalDateTime MAX = LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 59, 59, 999_999_000);
}
