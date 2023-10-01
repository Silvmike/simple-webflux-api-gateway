### Basic api gateway using Spring WebFlux

It is amazingly simple API gateway demo:

- exposes URL /test
- runs 2 requests to downstream services in parallel
- aggregates results

#### Running

```bash
./gradlew bootRun --args='--server.port=8080'
```

#### IDEA http scratch file

[test.http](./test.http)
