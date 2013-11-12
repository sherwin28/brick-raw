package net.isger.brick.raw.depot;

import net.isger.brick.raw.Depository;

public class DefaultHitch {

    public static void hitch(Object source) {
        if (!(source instanceof Depository)) {
            return;
        }
        Depository depos = (Depository) source;
        depos.add(new FileDepot());
        depos.add(new JarDepot());
    }

}
