package yuuki1293.ae2peat.xmod;

import net.pedroksl.ae2addonlib.util.AddonEnum;

public enum Addons implements AddonEnum {
    AE2WTLIB("AE2 Wireless Terminals Lib");

    private final String modName;

    Addons(String modName) {
        this.modName = modName;
    }

    public String getModId() {
        return name().toLowerCase();
    }

    public String getModName() {
        return this.modName;
    }
}
