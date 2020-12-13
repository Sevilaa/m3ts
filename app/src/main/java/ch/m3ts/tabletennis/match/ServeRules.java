package ch.m3ts.tabletennis.match;

public enum ServeRules {
    S2(2),
    S5(5);

    public final int amountOfServes;

    ServeRules(int amountOfServes) {
        this.amountOfServes = amountOfServes;
    }
}
