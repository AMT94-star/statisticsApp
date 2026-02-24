package service;

import model.PokemonEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class StatsService {

    private static final StatsService instance = new StatsService();

    public static StatsService getInstance() {
        return instance;
    }

    public StatsSnapshot compute(List<PokemonEntry> entries) {
        if (entries == null) entries = List.of();

        int total = entries.size();
        int caught = (int) entries.stream().filter(PokemonEntry::isCaught).count();
        int shiny = (int) entries.stream().filter(PokemonEntry::isShiny).count();

        double shinyRate = total == 0 ? 0.0 : (double) shiny / total;

        //avg CP
        List<Integer> cps = entries.stream()
                .map(PokemonEntry::getCp)
                .filter(cp -> cp > 0)
                .toList();
        double avgCp = cps.isEmpty() ? 0.0 : cps.stream().mapToInt(i -> i).average().orElse(0.0);

        //tag distribution
        Map<String, Long> tagCounts = entries.stream()
                .map(e -> safe(e.getTag()).trim())
                .map(t -> t.isBlank() ? "Untagged" : t)
                .collect(groupingBy(s -> s, counting()));

        Map<LocalDate, Long> dailyCounts = entries.stream()
                .map(PokemonEntry::getDate)
                .filter(Objects::nonNull)
                .collect(groupingBy(d -> d, counting()));

        //top pokie counts
        Map<String, Long> pokemonCounts = entries.stream()
                .map(e -> safe(e.getPokemonName()).trim())
                .map(n -> n.isBlank() ? "Unknown" : n)
                .collect(groupingBy(s -> s, counting()));

        return new StatsSnapshot(total, caught, shiny, shinyRate, avgCp, tagCounts, dailyCounts, pokemonCounts);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    public record StatsSnapshot(
            int total,
            int caught,
            int shiny,
            double shinyRate,
            double avgCp,
            Map<String, Long> tagCounts,
            Map<LocalDate, Long> dailyCounts,
            Map<String, Long> pokemonCounts
    ) {
    }
}
