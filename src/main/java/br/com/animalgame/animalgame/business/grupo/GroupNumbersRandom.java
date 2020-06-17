package br.com.animalgame.animalgame.business.grupo;

import br.com.animalgame.animalgame.business.sorteio.Sorteio;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class GroupNumbersRandom implements GroupNumbers {

    @Override
    public List<Grupo> getGroupsNumbers(Sorteio sorteio) {
        List<Integer> numbersSorted = this.getNumbersSorted(1000);
        Collections.shuffle(numbersSorted);
        List<Grupo> groups = new ArrayList<>();
        for (int i = 0; i <= sorteio.getAmountGroups()-1; i++) {
            Grupo grupo = new Grupo();
            grupo.setCodigo(String.valueOf(i+1));
            grupo.setNumeros(numbersSorted.subList(i * sorteio.getAmountNumbers(), sorteio.getAmountNumbers() * (i+1) ));
            groups.add(grupo);
        }
        return groups;
    }

    private List<Integer> getNumbersSorted(int maxLength) {
        List<Integer> result = new ArrayList<>();
        for(int i = 0; i <= maxLength-1; i++) result.add(i);
        return result;
    }

}
