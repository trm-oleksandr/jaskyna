package sk.upjs.kopr;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/*
 * Prijíma návštevníkov od Generátora udalostí 1 (alebo Generátora záznamu)
 * a zapisuje ich do databázy ako čakajúcich pred jaskyňou.
 */
@Component
public class VisitorConsumer {

    private final Cave cave;

    VisitorConsumer(Cave cave) {
        this.cave = cave;
    }

    @RabbitListener(bindings =
            @QueueBinding(
                    exchange = @Exchange(value = "jaskyna", type = ExchangeTypes.TOPIC),
                    value = @Queue,
                    key = "navstevnici.prichod"
            )
    )
    public void handleArrival(Arrival arrival) {
        cave.addArrival(arrival);
    }
}
