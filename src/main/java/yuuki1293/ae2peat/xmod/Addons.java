package yuuki1293.ae2peat.xmod;

import net.pedroksl.ae2addonlib.util.AddonEnum;

public enum Addons implements AddonEnum {
    AE2WTLIB("ae2wtlib"),
    AE2_JEI_INTEGRATION("ae2jeiintegration"),
    JEI("jei"),
    REI("rei"),
    EMI("emi");

    private final String modId;

    Addons(String modId) {
        this.modId = modId;
    }

    public String getModId() {
        return modId;
    }

    public String getModName() {
        return this.modId;
    }
}
