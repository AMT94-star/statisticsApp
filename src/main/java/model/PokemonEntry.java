package model;

import java.time.LocalDate;

public class PokemonEntry {

    private int id;
    private String pokemonName;
    private boolean seen;
    private boolean caught;
    private String location;
    private boolean shiny;
    private String weather;
    private boolean incense;
    private int incenseDuration;
    private String tag;
    private String event;
    private LocalDate date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPokemonName() {
        return pokemonName;
    }

    public void setPokemonName(String pokemonName) {
        this.pokemonName = pokemonName;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public boolean isCaught() {
        return caught;
    }

    public void setCaught(boolean caught) {
        this.caught = caught;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isShiny() {
        return shiny;
    }

    public void setShiny(boolean shiny) {
        this.shiny = shiny;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public boolean isIncense() {
        return incense;
    }

    public void setIncense(boolean incense) {
        this.incense = incense;
    }

    public int getIncenseDuration() {
        return incenseDuration;
    }

    public void setIncenseDuration(int incenseDuration) {
        this.incenseDuration = incenseDuration;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}