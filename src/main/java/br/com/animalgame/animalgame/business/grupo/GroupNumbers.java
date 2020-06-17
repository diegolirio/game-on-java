package br.com.animalgame.animalgame.business.grupo;

import br.com.animalgame.animalgame.business.sorteio.Sorteio;

import java.util.List;

public interface GroupNumbers {

    List<Grupo> getGroupsNumbers(Sorteio sorteio);
}
