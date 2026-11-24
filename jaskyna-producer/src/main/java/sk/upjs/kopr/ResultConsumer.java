package sk.upjs.kopr;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/*
 * Zbiera výsledky dňa od všetkých spustených jaskýň (variantov) a porovná ich
 * s variantom 3 sprievodcovia a dynamické vstupy.
 */
@Component
public class ResultConsumer {

    private final List<Result> results = new ArrayList<>();

    @RabbitListener(bindings =
            @QueueBinding(
                    exchange = @Exchange(value = "jaskyna", type = ExchangeTypes.TOPIC),
                    value = @Queue,
                    key = "vysledok"
            )
    )
    public void handleResult(Result result) {
        results.add(result);
        Result base = null;
        for (Result r : results) {
            if (r.getGuides() == 3 && r.getEntryMode().equals("dynamicky")) {
                base = r;
            }
        }
        System.out.println("========== Porovnanie variantov ==========");
        for (Result r : results) {
            String difference = "";
            if (base != null && r != base) {
                int diff = r.getServed() - base.getServed();
                difference = diff >= 0 ? " (o " + diff + " viac)" : " (o " + (-diff) + " menej)";
            }
            System.out.println(r + difference);
        }
    }
}
