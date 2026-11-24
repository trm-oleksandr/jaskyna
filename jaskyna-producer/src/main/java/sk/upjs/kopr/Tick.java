package sk.upjs.kopr;

import java.io.Serializable;

public class Tick implements Serializable {
    private static final long serialVersionUID = 100L;
    public static final int OPENING = 10 * 60;      // 10:00 - začiatok vstupných hodín
    public static final int LAST_ENTRY = 15 * 60;   // 15:00 - koniec vstupných hodín
    public static final int CLOSING = 16 * 60;      // 16:00 - posledný výpis správcu
    public static final int MINUTES_PER_TICK = 5;   // 1 sekunda simulácie = 5 minút
    private int time; // počet minút od polnoci

    public int getTime() {
        return time;
    }
    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return format(time);
    }

    public static String format(int time) {
        return String.format("%02d:%02d", time / 60, time % 60);
    }
}
