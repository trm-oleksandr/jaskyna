package sk.upjs.kopr;

import java.io.Serializable;

public class Arrival implements Serializable {
    private static final long serialVersionUID = 100L;
    private int time;  // kedy návštevníci prišli (minúty od polnoci)
    private int count; // koľko návštevníkov prišlo

    public int getTime() {
        return time;
    }
    public int getCount() {
        return count;
    }
    public void setTime(int time) {
        this.time = time;
    }
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return Tick.format(time) + " prišlo " + count + " návštevníkov";
    }
}
