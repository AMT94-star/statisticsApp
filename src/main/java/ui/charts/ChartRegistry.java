package ui.charts;

import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import model.PokemonEntry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class ChartRegistry {

    public record ChartDef(String id, String displayName, Function<FilteredList<PokemonEntry>, Node> builder) {
    }

    private static final LinkedHashMap<String, ChartDef> REGISTRY = new LinkedHashMap<>();

    static {
        register("dow_pie", "Day of Week", ChartBuilders::dayOfWeekPie);
        register("catch_rate", "Catch Rate", ChartBuilders::catchRateBar);
        register("shiny_pie", "Shiny Rate", ChartBuilders::shinyPie);
        register("weather_pie", "Weather Conditions", ChartBuilders::weatherPie);
        register("park_location", "Park & Location", ChartBuilders::parkLocationStacked);
        register("incense_line", "Incense Duration", ChartBuilders::incenseLine);
        register("avg_cp_line", "Avg CP over Time", ChartBuilders::avgCpLine);
        register("pokemon_bar", "Top Pokémon", ChartBuilders::topPokemonBar);
        register("encounters", "Encounters per Day", ChartBuilders::encountersLine);
        register("tag_pie", "Tag Distribution", ChartBuilders::tagPie);
    }

    private static void register(String id, String name, Function<FilteredList<PokemonEntry>, Node> builder) {
        REGISTRY.put(id, new ChartDef(id, name, builder));
    }

    public static Map<String, ChartDef> all() {
        return REGISTRY;
    }

    public static ChartDef get(String id) {
        return REGISTRY.get(id);
    }
}
