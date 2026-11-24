package sk.upjs.kopr;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
 * Generátor udalostí 2 - každú sekundu simulácie, ak je pripravený sprievodca a skupina môže vojsť,
 * zoberie čo najväčšiu skupinu do 20 ľudí na prehliadku.
 * Pri jaskyna.vstupy=polhodina sa vstupuje iba každú pol hodinu s 1 až 20 návštevníkmi.
 */
@Component
public class GuideGenerator {

    private final Cave cave;
    private final String entryMode;

    GuideGenerator(Cave cave, @Value("${jaskyna.vstupy}") String entryMode) {
        this.cave = cave;
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
        for (Tour tour : cave.getToursEndingAt(time)) {
            System.out.println(tick + " z jaskyne vyšla " + tour);
        }
        if (time > Tick.LAST_ENTRY) {
            return;
        }
        Tour tour = null;
        if (entryMode.equals("polhodina")) {
            if (time % 30 == 0) {
                tour = cave.startTour(time, 1);
            }
        } else {
            tour = cave.startTour(time, Cave.MIN_GROUP);
        }
        if (tour != null) {
            System.out.println(tick + " do jaskyne vošla " + tour);
        }
    }
}
