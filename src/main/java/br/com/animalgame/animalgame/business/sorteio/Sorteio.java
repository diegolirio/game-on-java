package br.com.animalgame.animalgame.business.sorteio;

import br.com.animalgame.animalgame.business.grupo.Grupo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Document("sorteios")
public class Sorteio {

    @Id
    private String id;
    private Integer amountGroups;
    private Integer amountNumbers;
    private Double price;
    private Double ratePercent;
    private LocalDate date;
    private String numbersType; // RANDOM or SEQUENTIAL
    private Double priceSubscription;
    private Double rateCurrency;

    private String code; // Codigo sorteado
    private Status status;
    private LocalDateTime dateTimeCreated;
    private String user;
    private List<Grupo> groups;
    private Grupo groupWinner;

    public Double calculatePrice() {
        return this.price * this.ratePercent / 100;
    }

    public enum Status {
        PENDING,
        AWAITING_AWARD,
        FINISHED
    }

}

