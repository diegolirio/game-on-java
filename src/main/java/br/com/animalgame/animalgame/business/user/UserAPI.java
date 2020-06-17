package br.com.animalgame.animalgame.business.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class UserAPI {

    //private static final String URL = "/v1/users";
    private static final String URL_LOGIN = "/v1/users/login";

    @Bean("UserRouter")
    public RouterFunction<ServerResponse> route(UserHandler handler) {
        return RouterFunctions
                .route(POST(URL_LOGIN).and(accept(APPLICATION_JSON)), handler::login);
    }

}

@Component
@RequiredArgsConstructor
class UserHandler {

    private final UserService service;

    public Mono<ServerResponse> login(ServerRequest serverRequest) {
        Mono<User> _mono = serverRequest.bodyToMono(User.class);
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(_mono.flatMap(service::login), User.class));
    }

}

@Service
class UserService {
    public Mono<User> login(User user) {
        if (
                        ("admin".equals(user.getName()) && "vegas2020".equals(user.getPassword())) ||
                        ("lili".equals(user.getName()) && "20lili20".equals(user.getPassword())) ||
                        ("thabata".equals(user.getName()) && "thabata3158".equals(user.getPassword())) ||
                        ("paty".equals(user.getName()) && "paty7586".equals(user.getPassword())) ||
                        ("paulo".equals(user.getName()) && "paulo4825".equals(user.getPassword())) ||
                        ("diego".equals(user.getName()) && "lirio1969".equals(user.getPassword()))
        ) {
            user.setPassword("*************************");
            return Mono.just(user);
        }
        throw new RuntimeException("Login ou senha inválidos");
    }
}