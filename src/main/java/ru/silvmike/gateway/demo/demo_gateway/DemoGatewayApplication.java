package ru.silvmike.gateway.demo.demo_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.webflux.ProxyExchange;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import ru.silvmike.gateway.demo.config.WebClientConfiguration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@SpringBootApplication
@Import(WebClientConfiguration.class)
public class DemoGatewayApplication {

	private static final String URL_ONE = "https://gist.githubusercontent.com/Silvmike/2ac76a3d338e01ec947a3d02bf7bf65b/raw/4c9241a31c7619aac39806451c14b191465d3466/gistfile1.txt";
	private static final String URL_TWO = "https://gist.githubusercontent.com/Silvmike/04cf3975182e99352e8182fd070d72d7/raw/5301781aeea22a85b58bf6d8380104dc80de8fa4/gistfile1.txt";

	private final Scheduler routeScheduler = Schedulers.newParallel("route", 100);

	@GetMapping("/test")
	public Mono<ResponseEntity<String>> proxy(ProxyExchange<byte[]> proxy) throws Exception {
		proxy = proxy.sensitive("Host");
		return Flux.merge(
			Arrays.asList(
				proxy.uri(URL_ONE).get(),
				proxy.uri(URL_TWO).get()
			)
		)
		.bufferTimeout(2, Duration.of(5, ChronoUnit.SECONDS))
		.flatMap(Flux::fromIterable)
		.map(r -> new String(Objects.requireNonNull(r.getBody())))
		.collect(Collectors.joining(","))
		.map(concatenated ->
			ResponseEntity.status(200)
				.header("Content-Type", "application/json")
				.body(String.format("{ \"names\": [%s] }", concatenated))
		)
		.subscribeOn(routeScheduler);
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoGatewayApplication.class, args);
	}

}
