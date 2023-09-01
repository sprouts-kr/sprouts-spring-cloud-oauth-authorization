package kr.sprouts.framework.service.oauth.authorization.components.resilience4j;

public final class Resilience4jInstance {
    private Resilience4jInstance() { }

    public static class Retry {
        private Retry() { }
        public static final String DEFAULT = "default";
    }

    public static class CircuitBreaker {
        private CircuitBreaker() { }
        public static final String DEFAULT = "default";
    }
}
