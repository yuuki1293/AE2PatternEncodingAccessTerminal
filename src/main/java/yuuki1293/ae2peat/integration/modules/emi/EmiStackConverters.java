package yuuki1293.ae2peat.integration.modules.emi;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Register your {@link EmiStackConverter} instances for JEI here.
 */
public class EmiStackConverters {
    private static List<EmiStackConverter> converters = ImmutableList.of();

    private EmiStackConverters() {
    }

    /**
     * @return The currently registered converters.
     */
    public static synchronized List<EmiStackConverter> getConverters() {
        return converters;
    }
}
