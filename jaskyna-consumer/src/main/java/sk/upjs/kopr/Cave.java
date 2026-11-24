package sk.upjs.kopr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/*
 * Databáza návštevníkov, prehliadok a sprievodcov - zdieľaná pamäťová štruktúra.
 * Pristupujú k nej naraz viaceré vlákna (každý @RabbitListener má svoje vlákno),
 * preto sú všetky metódy synchronized.
 */
@Component
public class Cave {

    public static final int TOUR_LENGTH = 30;        // prehliadka trvá 30 minút
    public static final int GUIDE_REST = 15;         // sprievodca si po prehliadke oddýchne 15 minút
    public static final int GAP_BETWEEN_GROUPS = 10; // rozostup medzi skupinami aspoň 10 minút
    public static final int MIN_GROUP = 10;
    public static final int MAX_GROUP = 20;

    private final List<Guide> guides = new ArrayList<>();
    private final List<Tour> tours = new ArrayList<>();
    private final List<Arrival> arrivals = new ArrayList<>(); // kedy a koľko návštevníkov prišlo
    private int lastTourStart = Tick.OPENING - GAP_BETWEEN_GROUPS;

    Cave(@Value("${jaskyna.sprievodcovia}") int numberOfGuides) {
        for (int i = 1; i <= numberOfGuides; i++) {
            guides.add(new Guide(i));
        }
    }

    public synchronized void addArrival(Arrival arrival) {
        arrivals.add(arrival);
    }

    /*
     * Počet návštevníkov čakajúcich pred jaskyňou na začiatku času time: prišli pred časom time
     * a ešte nevošli do jaskyne. Návštevníci, ktorí prišli práve v čase time, sa rátajú až od
     * ďalšieho času, preto výsledok nezávisí od toho, či ich vlákno spracovalo skôr alebo neskôr.
     */
    public synchronized int getWaiting(int time) {
        int waiting = 0;
        for (Arrival arrival : arrivals) {
            if (arrival.getTime() < time) {
                waiting += arrival.getCount();
            }
        }
        for (Tour tour : tours) {
            if (tour.getStart() < time) {
                waiting -= tour.getSize();
            }
        }
        return waiting;
    }

    /*
     * Ak sú splnené všetky podmienky, skupina vojde do jaskyne. Kontrola aj vstup sú jedna
     * atomická operácia, aby si medzi nimi nikto iný nezmenil stav databázy.
     * Vráti novú prehliadku, alebo null, ak skupina vojsť nemôže.
     */
    public synchronized Tour startTour(int time, int minGroupSize) {
        int waiting = getWaiting(time);
        if (waiting < minGroupSize || time - lastTourStart < GAP_BETWEEN_GROUPS) {
            return null;
        }
        Guide guide = null; // oddýchnutý sprievodca, ktorý čaká najdlhšie
        for (Guide g : guides) {
            if (g.getReadyFrom() <= time && (guide == null || g.getReadyFrom() < guide.getReadyFrom())) {
                guide = g;
            }
        }
        if (guide == null) {
            return null;
        }
        int size = Math.min(MAX_GROUP, waiting);
        guide.startTour(time);
        lastTourStart = time;
        Tour tour = new Tour(guide.getId(), time, size);
        tours.add(tour);
        return tour;
    }

    public synchronized List<Tour> getToursEndingAt(int time) {
        List<Tour> result = new ArrayList<>();
        for (Tour tour : tours) {
            if (tour.getEnd() == time) {
                result.add(tour);
            }
        }
        return result;
    }

    // skupiny, ktoré vošli pred časom time a ešte nevyšli
    public synchronized List<Tour> getToursInside(int time) {
        List<Tour> result = new ArrayList<>();
        for (Tour tour : tours) {
            if (tour.getStart() < time && tour.getEnd() > time) {
                result.add(tour);
            }
        }
        return result;
    }

    // počet návštevníkov, ktorí vyšli z jaskyne v intervale (from, to>
    public synchronized int getExited(int from, int to) {
        int exited = 0;
        for (Tour tour : tours) {
            if (tour.getEnd() > from && tour.getEnd() <= to) {
                exited += tour.getSize();
            }
        }
        return exited;
    }

    public synchronized int getServed() {
        int served = 0;
        for (Tour tour : tours) {
            served += tour.getSize();
        }
        return served;
    }

    public synchronized int getArrived() {
        int arrived = 0;
        for (Arrival arrival : arrivals) {
            arrived += arrival.getCount();
        }
        return arrived;
    }

    public synchronized List<Guide> getGuides() {
        return new ArrayList<>(guides);
    }
}
