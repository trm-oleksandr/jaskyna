package sk.upjs.kopr;

import java.io.Serializable;

public class Result implements Serializable {
    private static final long serialVersionUID = 100L;
    private int guides;       // počet sprievodcov
    private String entryMode; // "dynamicky" alebo "polhodina"
    private int arrived;      // koľko návštevníkov prišlo
    private int served;       // koľko návštevníkov absolvovalo prehliadku

    public int getGuides() {
        return guides;
    }
    public String getEntryMode() {
        return entryMode;
    }
    public int getArrived() {
        return arrived;
    }
    public int getServed() {
        return served;
    }
    public void setGuides(int guides) {
        this.guides = guides;
    }
    public void setEntryMode(String entryMode) {
        this.entryMode = entryMode;
    }
    public void setArrived(int arrived) {
        this.arrived = arrived;
    }
    public void setServed(int served) {
        this.served = served;
    }

    @Override
    public String toString() {
        return guides + " sprievodcovia, vstupy " + entryMode + ": obslúžených " + served + " z " + arrived + " návštevníkov";
    }
}
