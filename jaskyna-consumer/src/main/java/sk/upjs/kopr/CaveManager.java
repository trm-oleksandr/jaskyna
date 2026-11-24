package sk.upjs.kopr;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * Generátor udalostí 3 - správca jaskyne. Každú celú hodinu od 11:00 do 16:00 vypíše tabuľku.
 * O 16:00 vypíše aj odpracované minúty sprievodcov a pošle výsledok dňa.
 */
@Component
public class CaveManager {

    private final Cave cave;
    private final RabbitTemplate rabbitTemplate;
    private final int numberOfGuides;
    private final String entryMode;

    CaveManager(Cave cave, RabbitTemplate rabbitTemplate,
                @Value("${jaskyna.sprievodcovia}") int numberOfGuides,
                @Value("${jaskyna.vstupy}") String entryMode) {
        this.cave = cave;
        this.rabbitTemplate = rabbitTemplate;
        this.numberOfGuides = numberOfGuides;
        this.entryMode = entryMode;
    }

    @RabbitListener(bindings =
            @QueueBinding(
                    exchange = @Exchange(value = "hodiny", type = ExchangeTypes.FANOUT),
                    value = @Queue
            )
    )
    public void handleTick(Tick tick) {
        int time = tick.getTime();
        if (time % 60 != 0 || time <= Tick.OPENING) {
            return;
        }
        // tabuľku poskladáme celú a vypíšeme naraz, aby sa do nej nevpletali výpisy iných vlákien
        StringBuilder table = new StringBuilder();
        table.append("============ Správca jaskyne ").append(tick).append(" ============\n");
        table.append("Čakajúci pred jaskyňou:   ").append(cave.getWaiting(time)).append("\n");
        // stav na začiatku celej hodiny: skupina, ktorá práve vychádza alebo vchádza, sa nepočíta
        table.append("Skupiny v jaskyni:        ");
        List<Tour> toursInside = cave.getToursInside(time);
        if (toursInside.isEmpty()) {
            table.append("žiadne");
        }
        for (Tour tour : toursInside) {
            table.append(tour).append("; ");
        }
        table.append("\n");
        table.append("Vyšlo za poslednú hodinu: ").append(cave.getExited(time - 60, time));
        System.out.println(table);
        if (time == Tick.CLOSING) {
            printEndOfDay();
        }
    }

    private void printEndOfDay() {
        StringBuilder summary = new StringBuilder("============ Koniec dňa ============\n");
        for (Guide guide : cave.getGuides()) {
            summary.append("Sprievodca ").append(guide.getId()).append(" odpracoval ")
                   .append(guide.getWorkedMinutes()).append(" minút\n");
        }
        System.out.print(summary);
        Result result = new Result();
        result.setGuides(numberOfGuides);
        result.setEntryMode(entryMode);
        result.setArrived(cave.getArrived());
        result.setServed(cave.getServed());
        System.out.println(result);
        rabbitTemplate.convertAndSend("jaskyna", "vysledok", result);
    }
}
