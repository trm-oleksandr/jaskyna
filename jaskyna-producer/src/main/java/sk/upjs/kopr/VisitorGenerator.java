package sk.upjs.kopr;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/*
 * Generátor udalostí 1 - každú sekundu simulácie príde k jaskyni náhodne 0 až 5 návštevníkov.
 * Každý príchod sa zapíše do súboru, aby ho Generátor záznamu vedel zopakovať.
 */
@Component
@ConditionalOnProperty(name = "jaskyna.generator", havingValue = "nahodny", matchIfMissing = true)
public class VisitorGenerator {

    public static final String RECORD_FILE = "zaznam.txt";
    private final RabbitTemplate rabbitTemplate;
    private PrintWriter record;

    VisitorGenerator(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(bindings =
            @QueueBinding(
                    exchange = @Exchange(value = "hodiny", type = ExchangeTypes.FANOUT),
                    value = @Queue
            )
    )
    public void handleTick(Tick tick) throws IOException {
        if (tick.getTime() == Tick.OPENING) {
            record = new PrintWriter(new FileWriter(RECORD_FILE));
        }
        if (record == null || tick.getTime() > Tick.LAST_ENTRY) {
            return;
        }
        Arrival arrival = new Arrival();
        arrival.setTime(tick.getTime());
        arrival.setCount((int) (Math.random() * 6));
        record.println(arrival.getTime() + " " + arrival.getCount());
        record.flush();
        if (tick.getTime() == Tick.LAST_ENTRY) {
            record.close();
        }
        System.out.println(arrival);
        rabbitTemplate.convertAndSend("jaskyna", "navstevnici.prichod", arrival);
    }
}
