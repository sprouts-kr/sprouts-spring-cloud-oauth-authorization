package kr.sprouts.framework.service.oauth.authorization.components.webflux;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import kr.sprouts.framework.service.oauth.authorization.components.webflux.utilities.ThrowingConsumer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.LoggingCodecSupport;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public class RemoteWebClient {
    public static WebClient.Builder builder() {
        ReactorClientHttpConnector reactorClientHttpConnector = new ReactorClientHttpConnector(
                HttpClient.create().secure(ThrowingConsumer.unchecked(sslContextSpec -> sslContextSpec
                        .sslContext(SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build())
                )));

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(1024 * 1024 * 50))
                .build();

        exchangeStrategies.messageWriters().stream()
                .filter(LoggingCodecSupport.class::isInstance)
                .forEach(httpMessageWriter -> ((LoggingCodecSupport) httpMessageWriter).setEnableLoggingRequestDetails(true));

        ExchangeFilterFunction requestProcessor = ExchangeFilterFunction.ofRequestProcessor(Mono::just);
        ExchangeFilterFunction responseProcessor = ExchangeFilterFunction.ofResponseProcessor(Mono::just);

        return WebClient.builder()
                .clientConnector(reactorClientHttpConnector)
                .exchangeStrategies(exchangeStrategies)
                .filter(requestProcessor)
                .filter(responseProcessor);
    }

    public static WebClient create(String baseUrl) {
        return RemoteWebClient.builder().baseUrl(baseUrl).build();
    }

    public static WebClient create(String baseUrl, DefaultHeader... defaultHeaders) {
        WebClient.Builder webClientBuilder = RemoteWebClient.builder();

        webClientBuilder.baseUrl(baseUrl);

        for (DefaultHeader header : defaultHeaders) {
            webClientBuilder.defaultHeader(header.getHeader(), header.getValues());
        }

        return webClientBuilder.build();
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DefaultHeader {
        private String header;
        private String[] values;

        public static DefaultHeader of(String header, String... values) {
            return new DefaultHeader(header, values);
        }
    }
}
