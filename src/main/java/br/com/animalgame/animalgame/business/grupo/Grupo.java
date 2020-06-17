package br.com.animalgame.animalgame.business.grupo;

import br.com.animalgame.animalgame.business.aposta.Aposta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Grupo {

    private String codigo;
    private List<Integer> numeros;
    private Aposta aposta;
}
