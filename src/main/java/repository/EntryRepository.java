package repository;

import model.PokemonEntry;

import java.util.List;

public interface EntryRepository {

    void insert(PokemonEntry entry);

    void insertWithId(PokemonEntry entry);

    void update(PokemonEntry entry);

    void deleteById(int id);

    void setTagForIds(List<Integer> ids, String tag);

    void clearTagForIds(List<Integer> ids);

    List<PokemonEntry> findAll();

    List<PokemonEntry> findByPokemonFilterYear(String pokemonFilter, int year);

    List<PokemonEntry> findByPokemonFilterMonth(String pokemonFilter, int year, int month);

    List<PokemonEntry> findByTagYear(String tagFilter, int year);

    List<PokemonEntry> findByTagMonth(String tagFilter, int year, int month);

    List<String> findDistinctTags();
    
    List<String> findDistinctPokemonNames();
}
