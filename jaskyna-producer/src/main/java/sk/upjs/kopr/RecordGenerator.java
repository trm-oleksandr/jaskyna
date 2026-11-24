package sk.upjs.kopr;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/*
 * Generátor záznamu - náhrada Generátora udalostí 1. Zopakuje presne rovnaké časy a počty
 * návštevníkov, ako naposledy vyrobil Generátor udalostí 1 (súbor zaznam.txt).
 * Zapína sa parametrom --jaskyna.generator=zaznam
 */
@Component
@ConditionalOnProperty(name = "jaskyna.generator", havingValue = "zaznam")
public class RecordGenerator {

    private final RabbitTemplate rabbitTemplate;
    private final Map<Integer, Integer> record = new HashMap<>(); // čas -> počet návštevníkov

    RecordGenerator(RabbitTemplate rabbitTemplate) throws FileNotFoundException {
        this.rabbitTemplate = rabbitTemplate;
        try (Scanner scanner = new Scanner(new File(VisitorGenerator.RECORD_FILE))) {
            while (scanner.hasNextInt()) {
                int time = scanner.nextInt();
                int count = scanner.nextInt();
                record.put(time, count);
            }
        }
    }

    @RabbitListener(bindings =
            @QueueBinding(
                    exchange = @Exchange(value = "hodiny", type = ExchangeTypes.FANOUT),
                    value = @Queue
            )
    )
    public void handleTick(Tick tick) {
        Integer count = record.get(tick.getTime());
        if (count == null) {
            return;
        }
        Arrival arrival = new Arrival();
        arrival.setTime(tick.getTime());
        arrival.setCount(count);
        System.out.println("Záznam: " + arrival);
        rabbitTemplate.convertAndSend("jaskyna", "navstevnici.prichod", arrival);
    }
}
