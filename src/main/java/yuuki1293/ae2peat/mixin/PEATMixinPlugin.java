package yuuki1293.ae2peat.mixin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import yuuki1293.ae2peat.xmod.Addons;

/**
 * Controls load/unloading of Mixin classes.
 * When unloading and loading conflict, unloading takes priority.
 */
public class PEATMixinPlugin implements IMixinConfigPlugin {
    private static final String COMMON_MIXIN_PACKAGE = "yuuki1293.ae2peat.mixin.common";
    private static final Map<Addons, Set<String>> LOAD_WHEN_MOD_PRESENT = new HashMap<>();
    private static final Map<Addons, Set<String>> EXCLUDE_WHEN_MOD_PRESENT = new HashMap<>();

    static {
        LOAD_WHEN_MOD_PRESENT.put(
                Addons.POLYMORPHIC_ENERGISTICS,
                Set.of(
                        "yuuki1293.ae2peat.mixin.polyeng.PatternEncodingAccessTermMenuMixin",
                        "yuuki1293.ae2peat.mixin.polyeng.PatternEncodingAccessTermScreenMixin"));
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Default: only load common mixins
        boolean load = mixinClassName.startsWith(COMMON_MIXIN_PACKAGE);

        // Check if mixin should only load when specific mod is present
        if (!load) {
            for (Map.Entry<Addons, Set<String>> entry : LOAD_WHEN_MOD_PRESENT.entrySet()) {
                Addons requiredMod = entry.getKey();
                Set<String> mixinsForMod = entry.getValue();
                if (mixinsForMod.contains(mixinClassName)) {
                    if (requiredMod.isLoaded()) {
                        load = true;
                    }
                }
            }
        }

        // Check if mixin should be excluded when specific mod is present
        if (load) {
            for (Map.Entry<Addons, Set<String>> entry : EXCLUDE_WHEN_MOD_PRESENT.entrySet()) {
                Addons conflictingMod = entry.getKey();
                Set<String> mixinsToExclude = entry.getValue();
                if (mixinsToExclude.contains(mixinClassName)) {
                    if (conflictingMod.isLoaded()) {
                        load = false;
                    }
                }
            }
        }

        return load;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
