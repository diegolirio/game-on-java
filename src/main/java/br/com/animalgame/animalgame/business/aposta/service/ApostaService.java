package br.com.animalgame.animalgame.business.aposta.service;

import br.com.animalgame.animalgame.business.aposta.Aposta;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApostaService {

    Flux<Aposta> findAll();

    Mono<Aposta> findById(String id);

    Mono<Aposta> save(Aposta aposta);

    Flux<Aposta> findBySorteioId(String idSorteio);

    Aposta save2(Aposta aposta);
}

@Service
@RequiredArgsConstructor
class ApostaServiceImpl implements ApostaService {

    private final ApostaRepository apostaRepository;
    private final ApostaRepositoryImpl apostaRepositoryImpl;

    @Override
    public Flux<Aposta> findAll() {
        return this.apostaRepository.findAll();
    }

    @Override
    public Mono<Aposta> findById(String id) {
        return this.apostaRepository.findById(id);
    }

    @Override
    public Mono<Aposta> save(Aposta aposta) {
        return this.apostaRepository.save(aposta);
    }

    @Override
    public Flux<Aposta> findBySorteioId(String idSorteio) {
        return this.apostaRepository.findBySorteioId(idSorteio);
    }

    @Override
    public Aposta save2(Aposta aposta) {
        return this.apostaRepositoryImpl.save(aposta);
    }
}

@Repository
interface ApostaRepository extends ReactiveCrudRepository<Aposta, String> {

    Flux<Aposta> findBySorteioId(String idSorteio);
}

@Repository
interface ApostaRepositoryImpl extends CrudRepository<Aposta, String>{}