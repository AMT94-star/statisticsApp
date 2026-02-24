package service;

import model.PokemonEntry;
import repository.EntryRepository;
import repository.SqlEntryRepository;
import util.TextNormalizer;

import java.util.List;

public class EntryService {

    private static final EntryService INSTANCE = new EntryService(new SqlEntryRepository());
    private final EntryRepository repo;

    private EntryService(EntryRepository repo) {
        this.repo = repo;
    }

    public static EntryService getInstance() {
        return INSTANCE;
    }

    public void add(PokemonEntry entry) {
        normalize(entry);
        repo.insert(entry);
    }

    public void update(PokemonEntry entry) {
        normalize(entry);
        repo.update(entry);
    }

    public void deleteById(int id) {
        repo.deleteById(id);
    }

    public void undoInsertWithId(PokemonEntry entry) {
        normalize(entry);
        repo.insertWithId(entry);
    }

    //Hämtar alla entries oavsett år, används av OverallStatsView
    public List<PokemonEntry> getAllEntries() {
        return repo.findAll();
    }

    public List<PokemonEntry> getByPokemonFilterYear(String pokemonFilter, int year) {
        return repo.findByPokemonFilterYear(pokemonFilter, year);
    }

    public List<PokemonEntry> getByPokemonFilterMonth(String pokemonFilter, int year, int month) {
        return repo.findByPokemonFilterMonth(pokemonFilter, year, month);
    }

    public List<PokemonEntry> getByTagYear(String tagFilter, int year) {
        return repo.findByTagYear(tagFilter, year);
    }

    public List<PokemonEntry> getByTagMonth(String tagFilter, int year, int month) {
        return repo.findByTagMonth(tagFilter, year, month);
    }

    public List<String> getDistinctTags() {
        return repo.findDistinctTags();
    }

    //alla unika pokémon i databasen
    public List<String> getDistinctPokemonNames() {
        return repo.findDistinctPokemonNames();
    }

    public void setTagForIds(List<Integer> ids, String tag) {
        String t = TextNormalizer.smartTitle(tag);
        repo.setTagForIds(ids, t);
    }

    public void clearTagForIds(List<Integer> ids) {
        repo.clearTagForIds(ids);
    }

    private void normalize(PokemonEntry e) {
        if (e == null) return;
        e.setPokemonName(TextNormalizer.smartTitle(e.getPokemonName()));
        e.setTag(TextNormalizer.smartTitle(e.getTag()));
    }
}
