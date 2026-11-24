package sk.upjs.kopr;

/*
 * Sprievodca v databáze jaskyne. Mení sa iba vo vnútri synchronizovaných metód triedy Cave.
 */
public class Guide {
    private final int id;
    private int readyFrom = Tick.OPENING; // od kedy je oddýchnutý a môže viesť ďalšiu skupinu
    private int workedMinutes = 0;        // minúty strávené na prehliadkach

    public Guide(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
    public int getReadyFrom() {
        return readyFrom;
    }
    public int getWorkedMinutes() {
        return workedMinutes;
    }

    public void startTour(int time) {
        readyFrom = time + Cave.TOUR_LENGTH + Cave.GUIDE_REST;
        workedMinutes += Cave.TOUR_LENGTH;
    }
}
