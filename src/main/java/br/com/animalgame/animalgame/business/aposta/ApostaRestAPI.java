package br.com.animalgame.animalgame.business.aposta;

import br.com.animalgame.animalgame.business.aposta.service.ApostaService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class ApostaRestAPI {

    private static final String _URL = "/v1/apostas";
    private static final String _URL_BY_SORTEIO = "/v1/apostas/sorteio/{idSorteio}";
    private static final String _URL_ID = "/v1/apostas/{id}";

    @Bean("apostaRoute")
    public RouterFunction<ServerResponse> route(ApostaHandler handler) {
        return RouterFunctions
                .route(GET(_URL).and(accept(APPLICATION_JSON)), handler::getAll)
                .andRoute(GET(_URL_ID).and(accept(APPLICATION_JSON)), handler::getById)
                .andRoute(GET(_URL_BY_SORTEIO).and(accept(APPLICATION_JSON)), handler::getBySoreteio)
                .andRoute(POST(_URL).and(accept(APPLICATION_JSON)), handler::save);
    }
}

@Component
@RequiredArgsConstructor
class ApostaHandler {

    private final ApostaService service;

    public Mono<ServerResponse> getAll(ServerRequest serverRequest) {
        Flux<Aposta> Apostas = service.findAll();
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(Apostas, Aposta.class);
    }

    public Mono<ServerResponse> getById(ServerRequest serverRequest) {
        Mono<Aposta> Aposta = service.findById(serverRequest.pathVariable("id"));
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(Aposta, Aposta.class);
    }

    public Mono<ServerResponse> save(ServerRequest serverRequest) {
        Mono<Aposta> _mono = serverRequest.bodyToMono(Aposta.class);
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(_mono.flatMap(service::save), Aposta.class));
    }

    public Mono<ServerResponse> getBySoreteio(ServerRequest serverRequest) {
        Flux<Aposta> apostas = service.findBySorteioId(serverRequest.pathVariable("idSorteio"));
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(apostas, Aposta.class);
    }
}