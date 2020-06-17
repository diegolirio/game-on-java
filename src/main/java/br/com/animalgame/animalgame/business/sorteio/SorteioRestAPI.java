package br.com.animalgame.animalgame.business.sorteio;

import br.com.animalgame.animalgame.business.aposta.Aposta;
import br.com.animalgame.animalgame.business.sorteio.service.SorteioService;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
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
public class SorteioRestAPI {

    private static final String SORTEIO_URL = "/v1/sorteios";
    private static final String SORTEIO_URL_ID = "/v1/sorteios/{id}";
    private static final String SORTEIO_URL_ID_UPDATE_STATUS = "/v1/sorteios/{id}/update-status/{status}";
    private static final String SORTEIO_URL_ID_FINISH = "/v1/sorteios/{id}/finish/{code}";
    private static final String _URL_ID_GRUPO_CODIGO = "/v1/sorteios/{id}/grupos/{codigo}";
    private static final String SORTEIO_URL_ID_DELETE = "/v1/sorteios/{id}/delete";

    @Bean
    public RouterFunction<ServerResponse> route(SorteioHandler handler) {
        return RouterFunctions
                .route(GET(SORTEIO_URL).and(accept(APPLICATION_JSON)), handler::getAll)
                .andRoute(GET(SORTEIO_URL_ID).and(accept(APPLICATION_JSON)), handler::getById)
                //.andRoute(PATCH(_URL_ID_GRUPO_CODIGO).and(accept(APPLICATION_JSON)), handler::updateApostaGrupo)
                .andRoute(POST(_URL_ID_GRUPO_CODIGO).and(accept(APPLICATION_JSON)), handler::updateAposta)
                .andRoute(PUT(_URL_ID_GRUPO_CODIGO).and(accept(APPLICATION_JSON)), handler::desapostar)
                .andRoute(POST(SORTEIO_URL).and(accept(APPLICATION_JSON)), handler::save)
                .andRoute(POST(SORTEIO_URL_ID_UPDATE_STATUS).and(accept(APPLICATION_JSON)), handler::updateStatus)
                .andRoute(POST(SORTEIO_URL_ID_FINISH).and(accept(APPLICATION_JSON)), handler::finish)
                .andRoute(POST(SORTEIO_URL_ID_DELETE).and(accept(APPLICATION_JSON)), handler::delete)
                .andRoute(POST(SORTEIO_URL_ID).and(accept(APPLICATION_JSON)), handler::update);
    }

}

@Component
@RequiredArgsConstructor
class SorteioHandler {

    private final SorteioService service;

    public Mono<ServerResponse> getAll(ServerRequest serverRequest) {
        Flux<Sorteio> sorteios = service.findAll();
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(sorteios, Sorteio.class);
    }

    public Mono<ServerResponse> getById(ServerRequest serverRequest) {
        Mono<Sorteio> sorteio = service.findById(serverRequest.pathVariable("id"));
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(sorteio, Sorteio.class);
    }

    public Mono<ServerResponse> save(ServerRequest serverRequest) {
        Mono<Sorteio> _mono = serverRequest.bodyToMono(Sorteio.class);
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(_mono.flatMap(service::save), Sorteio.class));
    }

//    public Mono<ServerResponse> updateApostaGrupo(ServerRequest serverRequest) {
//        Mono<Sorteio> _monoSorteio = service.findById(serverRequest.pathVariable("id"));
//        String grupoCodigo = serverRequest.pathVariable("codigo");
//        Mono<Aposta> _monoAposta = serverRequest.bodyToMono(Aposta.class);
//        AtomicReference<Sorteio> sorteio = new AtomicReference<>();
//        AtomicReference<Aposta> aposta = new AtomicReference<>();
//
//        //Mono<Sorteio> sorteioMono = _monoSorteio.flatMap(service::save);
//
//        //_monoSorteio.subscribe(s -> { sorteio.set(s); });
//        //_monoAposta.subscribe(a -> { aposta.set(a); });
//
//        _monoSorteio.flatMap(value -> {
//            sorteio.set(value);
//            return Mono.just(value);
//        });
//
//
//        service.updateGrupoAposta(sorteio.get(), grupoCodigo, aposta.get());
//        return ServerResponse.ok().contentType(APPLICATION_JSON)
//                .body(_monoSorteio, Sorteio.class);
//    }
    
    public Mono<ServerResponse> updateAposta(ServerRequest serverRequest) {
        Mono<Aposta> aposta = serverRequest.bodyToMono(Aposta.class);
        String sorteioId = serverRequest.pathVariable("id");
        String grupoCodigo = serverRequest.pathVariable("codigo");
        Publisher<Sorteio> pub = aposta.flatMap(a -> Mono.just(service.updateAposta(a, sorteioId, grupoCodigo)));
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(pub, Sorteio.class));
    }

    public Mono<ServerResponse> desapostar(ServerRequest serverRequest) {
        Mono<Aposta> aposta = serverRequest.bodyToMono(Aposta.class);
        String sorteioId = serverRequest.pathVariable("id");
        String grupoCodigo = serverRequest.pathVariable("codigo");
        Publisher<Sorteio> pub = aposta.flatMap(a -> Mono.just(service.desapostar(a, sorteioId, grupoCodigo)));
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(pub, Sorteio.class));
    }


    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        Mono<Sorteio> _mono = serverRequest.bodyToMono(Sorteio.class);
        Publisher<Sorteio> _m = _mono.flatMap(service::update);
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(_m, Sorteio.class));
    }

    public Mono<ServerResponse> updateStatus(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        String status = serverRequest.pathVariable("status");
        Mono<Sorteio> sorteio = this.service.updateStatus(id, Sorteio.Status.valueOf(status));
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(sorteio, Sorteio.class));
    }

    public Mono<ServerResponse> finish(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        String code = serverRequest.pathVariable("code");
        Mono<Sorteio> sorteio = this.service.finish(id, code);
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(sorteio, Sorteio.class));
    }

    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        //Mono<Void> voidMono = this.service.delete(id);
        this.service.delete(id);
        return ServerResponse.noContent().build();
    }
}