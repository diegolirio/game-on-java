package br.com.animalgame.animalgame.business.sorteio.service;

import br.com.animalgame.animalgame.business.aposta.Aposta;
import br.com.animalgame.animalgame.business.aposta.service.ApostaService;
import br.com.animalgame.animalgame.business.grupo.GroupNumbersRandom;
import br.com.animalgame.animalgame.business.grupo.Grupo;
import br.com.animalgame.animalgame.business.sorteio.Sorteio;
import br.com.animalgame.animalgame.config.exceptions.BadRequestException;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public interface SorteioService {
    Flux<Sorteio> findAll();

    Mono<Sorteio> findById(String id);

    Mono<Sorteio> save(Sorteio sorteio);

    Mono<Sorteio> update(Sorteio sorteio);

    //Mono<Sorteio> updateGrupoAposta(Sorteio sorteio, String grupoCodigo, Aposta aposta);
    Sorteio updateAposta(Aposta aposta, String sorteioId, String grupoCodigo);
    Sorteio desapostar(Aposta aposta, String sorteioId, String grupoCodigo);

    Mono<Sorteio> updateStatus(String id, Sorteio.Status status);

    Mono<Sorteio> finish(String id, String code);

    void delete(String id);

}

@Service
@RequiredArgsConstructor
class SorteioServiceImpl implements SorteioService {

    private final SorteioRepository sorteioRepository;
    private final SorteioRepositoryImpl sorteioRepositoryImpl;
    private final ApostaService apostaService;
    private final GroupNumbersRandom groupNumbersRandom;

    @Override
    public Flux<Sorteio> findAll() {
        return sorteioRepository.findAll();
    }

    @Override
    public Mono<Sorteio> findById(String id) {
        return sorteioRepository.findById(id);
    }

    @Override
    public Mono<Sorteio> save(Sorteio sorteio) {

        if (StringUtils.isNotEmpty(sorteio.getId())) {
            throw new RuntimeException("Sorteio já cadastrado!!!");
        }

        List<Grupo> groups;
        if ("RANDOM".equals(sorteio.getNumbersType())) {
            groups = this.groupNumbersRandom.getGroupsNumbers(sorteio);
        } else {
            groups = getNumbersByGroupSequential(sorteio);
        }
        sorteio.setGroups(groups);
        sorteio.setStatus(Sorteio.Status.PENDING);
        sorteio.setDateTimeCreated(LocalDateTime.now());
        sorteio.setRatePercent(sorteio.calculatePrice());
        return this.sorteioRepository.save(sorteio);
    }

    private List<Grupo> getNumbersByGroupSequential(Sorteio sorteio) {
        List<Integer> numerosEscolhidos = new ArrayList<>();
        List<Grupo> groups = new ArrayList<>();
        for (int i = 0; i <= sorteio.getAmountGroups() - 1; i++) {
            List<Integer> numerosDiferenteLista =
                    getNumerosDiferenteLista2(sorteio.getAmountNumbers(), i);
            numerosEscolhidos.addAll(numerosDiferenteLista);
            Grupo grupo =
                    Grupo.builder()
                            .codigo(String.valueOf(i + 1))
                            .numeros(numerosDiferenteLista)
                            .build();
            groups.add(grupo);
        }
        return groups;
    }

    @Override
    public Mono<Sorteio> update(Sorteio sorteio) {
        return this.sorteioRepository.save(sorteio);
    }

    @Override
    public Sorteio updateAposta(Aposta aposta, String sorteioId, String grupoCodigo) {
        Sorteio sorteio = this.sorteioRepositoryImpl.findById(sorteioId).get();
        if (!aposta.getSorteioId().equals(sorteioId)) {
            // Status BadRequest...
            throw new RuntimeException("Aposta não pertence a este sorteio!");
        }
        if (sorteio.getStatus() != Sorteio.Status.PENDING) {
            // Status code 412...
            throw new RuntimeException("Você não pode alterar mais suas apostas, sorteio aguardando o Resultado!");
        }
        Grupo grupoSelected =
                sorteio.getGroups().stream()
                        .filter(grupo -> grupo.getCodigo().equals(grupoCodigo))
                        .findFirst()
                        .orElseThrow();
        if (grupoSelected.getAposta() != null) {
            throw new RuntimeException("Cartela Indisponivel, selecione outra por favor!!!");
        }

        if(aposta.isNotAllowNewAposta()) {
            throw new BadRequestException("Não é permitido escolher novas Apostas. Você já possui "+aposta.getQuantidadeMaxima()+". Exclua uma escolhida!");
        }

        //sorteio.getGroups().stream()
        //        .filter(grupo -> aposta.equals(grupo.getAposta()))
        //        .forEach(grupo -> grupo.setAposta(null));

        sorteio.getGroups().stream()
                .filter(grupo -> grupo.getCodigo().equals(grupoCodigo))
                .forEach(g -> g.setAposta(aposta));

        // aposta.setGrupoCodigo(grupoSelected.getCodigo());

        aposta.addGrupoEscolhido(grupoSelected.getCodigo());

        this.apostaService.save2(aposta);
        return this.sorteioRepositoryImpl.save(sorteio);
    }

    @Override
    public Sorteio desapostar(Aposta aposta, String sorteioId, String grupoCodigo) {
        Sorteio sorteio = this.sorteioRepositoryImpl.findById(sorteioId).get();
        if (!aposta.getSorteioId().equals(sorteioId)) {
            throw new BadRequestException("Aposta não pertence a este sorteio!");
        }
        if (sorteio.getStatus() != Sorteio.Status.PENDING) {
            throw new BadRequestException("Você não pode alterar mais suas apostas, sorteio aguardando o Resultado!");
        }
        Grupo groupDeselect =
                sorteio.getGroups().stream()
                        .filter(grupo -> grupo.getCodigo().equals(grupoCodigo))
                        .findFirst()
                        .orElseThrow();
        if (groupDeselect.getAposta() == null) {
            throw new RuntimeException("Cartela já encontra-se disponivel!!!");
        }

        if(aposta.getGruposEscolhidos() == null || aposta.getGruposEscolhidos().size() <= 0) {
            throw new BadRequestException("Você não possui apostas!");
        }

        //sorteio.getGroups().stream()
        //        .filter(grupo -> aposta.equals(grupo.getAposta()))
        //        .forEach(grupo -> grupo.setAposta(null));

        sorteio.getGroups().stream()
                .filter(grupo -> grupo.getCodigo().equals(grupoCodigo))
                .forEach(g -> g.setAposta(null));

        aposta.removeGrupoEscolhido(groupDeselect.getCodigo());

        this.apostaService.save2(aposta);
        return this.sorteioRepositoryImpl.save(sorteio);
    }

    @Override
    public Mono<Sorteio> updateStatus(String id, Sorteio.Status status) {
        Sorteio sorteio = this.sorteioRepositoryImpl.findById(id).get();
        // TODO Chain Responsability | Bolao ---> Exclusivo para o AWAITING_AWARD
        if (sorteio.getStatus() == Sorteio.Status.PENDING && status == Sorteio.Status.AWAITING_AWARD) {
            sorteio.setStatus(status);
            return this.sorteioRepository.save(sorteio);
        }
        throw new RuntimeException("Status Atual do Sorteio é " + sorteio.getStatus() + ", você não pode muda-lo para " + status);
    }

    @Override
    public Mono<Sorteio> finish(String id, String code) {
        Sorteio sorteio = this.sorteioRepositoryImpl.findById(id).get();
        sorteio.setCode(code);
        if (sorteio.getStatus() == Sorteio.Status.AWAITING_AWARD) {
            sorteio.setStatus(Sorteio.Status.FINISHED);
            sorteio.setGroupWinner(this.getWinner(sorteio, code));
            return this.sorteioRepository.save(sorteio);
        }
        throw new RuntimeException("Status Atual do Sorteio é " + sorteio.getStatus() + ", você não pode muda-lo para " + Sorteio.Status.FINISHED);
    }

    @Override
    public void delete(String id) {
        this.sorteioRepositoryImpl.deleteById(id);
    }

    private Grupo getWinner(Sorteio sorteio, String code) {
        // TODO
//        Grupo grupo = sorteio.getGroups()
//                .stream()
//                .filter(g ->
//                        g.getCodigo().equals(
//                                g.getNumeros().stream()
//                                            .filter(n -> n == Integer.parseInt(code))
//                                            .findFirst()
//                                            .orElse(Integer.MIN_VALUE))
//                )
//                .findFirst()
//                .orElseThrow();
//        return grupo.getCodigo();
        for (Grupo g : sorteio.getGroups()) {
            for (Integer numero : g.getNumeros()) {
                if (numero == Integer.parseInt(code)) {
                    return g;
                }
            }
        }
        throw new RuntimeException("Número Sorteado nao esta entre os Grupos");
    }

//    @Override
//    public Mono<Sorteio> updateGrupoAposta(Sorteio sorteio, String grupoCodigo, Aposta aposta) {
//        sorteio.getGroups()
//                .stream()
//                .filter(g -> grupoCodigo.equals(g.getCodigo()))
//                .forEach(g -> {
//                    g.setAposta(aposta);
//                    aposta.setGrupoCodigo(g.getCodigo());
//                    aposta.setSorteioId(sorteio.getId());
//                    this.apostaService.save(aposta);
//                });
//        return this.sorteioRepository.save(sorteio);
//    }

    private List<Integer> getNumerosDiferenteLista2(int numeroMaximo, int index) {
        List<Integer> ns = new ArrayList<>();
        Integer min = index * numeroMaximo;
        Integer max = numeroMaximo * (index + 1);
        for (int i = min; i <= max - 1; i++) {
            ns.add(i);
        }
        return ns;
    }

    private List<Integer> getNumerosDiferenteLista(int numerosPorGrupos, Integer... numeros) {
        List<Integer> result = new ArrayList<>();
        for (int i = 1; i <= numerosPorGrupos; i++) {
            int i1 = -1;
            do {
                i1 = new Random().nextInt(999);
            } while (this.numeroEstaEntreOsEscolhidos(i1, numeros));
            result.add(i1);
        }
        return result;
    }

    private boolean numeroEstaEntreOsEscolhidos(Integer numero, Integer... numeros) {
        for (Integer n : numeros) {
            if (numero.equals(numero)) {
                return true;
            }
        }
        return false;
    }

}

@Primary
@Repository
interface SorteioRepository extends ReactiveCrudRepository<Sorteio, String> {
}


@Repository("SorteioRepositoryImpl")
interface SorteioRepositoryImpl extends CrudRepository<Sorteio, String> {
}