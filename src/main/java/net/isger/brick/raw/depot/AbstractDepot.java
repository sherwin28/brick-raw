package net.isger.brick.raw.depot;

import java.util.HashSet;
import java.util.Set;

import net.isger.brick.raw.Depot;

public abstract class AbstractDepot implements Depot {

    private Set<String> labs;

    protected AbstractDepot() {
        labs = new HashSet<String>();
    }

    protected void addLabel(String label) {
        if (label != null) {
            label = label.toLowerCase();
            if (!labs.contains(label)) {
                labs.add(label);
            }
        }
    }

    public boolean isSupport(String label) {
        return label != null && labs.contains(label.toLowerCase());
    }

}
