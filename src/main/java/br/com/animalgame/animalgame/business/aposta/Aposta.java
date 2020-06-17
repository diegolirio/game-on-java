package br.com.animalgame.animalgame.business.aposta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document("apostas")
@EqualsAndHashCode(of = "id")
public class Aposta {

    @Id
    private String id;
    private String name;
    private Double price;
    private StatusEnum status = StatusEnum.PENDENTE;
    private String sorteioId;
    //private String grupoCodigo;
    private String celular;

    private List<String> gruposEscolhidos;
    private int quantidadeMaxima;

    @JsonIgnoreProperties
    public boolean isNotAllowNewAposta() {
        if(quantidadeMaxima <= 0) {
            return false;
        }
        return gruposEscolhidos != null && gruposEscolhidos.size() >= quantidadeMaxima;
    }

    public void addGrupoEscolhido(String grupoCodigo) {
        if(gruposEscolhidos == null) {
            gruposEscolhidos = new ArrayList<>();
        }
         gruposEscolhidos.add(grupoCodigo);
    }

    public void removeGrupoEscolhido(String grupoCodigo) {
        gruposEscolhidos.remove(grupoCodigo);
    }

    public enum StatusEnum {
        PAGO,
        PENDENTE
    }
}
