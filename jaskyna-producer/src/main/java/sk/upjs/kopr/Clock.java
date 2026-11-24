package sk.upjs.kopr;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/*
 * Generátor udalostí 4 - hodiny. Každú sekundu posunie čas simulácie o 5 minút
 * a rozpošle ho cez fanout exchange "hodiny" všetkým ostatným generátorom.
 */
@Component
public class Clock {

    private final RabbitTemplate rabbitTemplate;
    private int time = Tick.OPENING;

    Clock(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedRate = 1000, initialDelay = 1000)
    public void tick() {
        if (time > Tick.CLOSING) {
            return;
        }
        Tick tick = new Tick();
        tick.setTime(time);
        rabbitTemplate.convertAndSend("hodiny", "", tick);
        if (time == Tick.CLOSING) {
            System.out.println(tick + " koniec simulácie");
        }
        time += Tick.MINUTES_PER_TICK;
    }
}
