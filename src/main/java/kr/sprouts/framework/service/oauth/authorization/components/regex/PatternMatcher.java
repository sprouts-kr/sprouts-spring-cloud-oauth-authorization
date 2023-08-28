package kr.sprouts.framework.service.oauth.authorization.components.regex;

import java.util.regex.Pattern;

public class PatternMatcher {
    private PatternMatcher() {
        throw new IllegalStateException("Utility class");
    }

    private static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$";

    public static Boolean uuid(String source) {
        return Pattern.compile(UUID_REGEX).matcher(source).find();
    }
}
