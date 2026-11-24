package sk.upjs.kopr;

/*
 * Prehliadka v databáze jaskyne. Po vytvorení sa už nemení.
 */
public class Tour {
    private final int guide; // číslo sprievodcu
    private final int start; // čas vstupu do jaskyne
    private final int size;  // veľkosť skupiny

    public Tour(int guide, int start, int size) {
        this.guide = guide;
        this.start = start;
        this.size = size;
    }

    public int getGuide() {
        return guide;
    }
    public int getStart() {
        return start;
    }
    public int getSize() {
        return size;
    }
    public int getEnd() {
        return start + Cave.TOUR_LENGTH;
    }

    @Override
    public String toString() {
        return "skupina " + size + " so sprievodcom " + guide;
    }
}
