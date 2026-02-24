package model;

import java.time.LocalDate;

public class PokemonEntry {

    private int id;
    private LocalDate date;
    private String day;
    private String time;

    private String pokemonName;
    private int cp;
    private boolean caught;
    private boolean shiny;

    private String weather;
    private String park;
    private String location;

    private String tag;
    private String event;

    private boolean incense;
    private int incenseDuration;

    public PokemonEntry() {
    }

    public PokemonEntry(LocalDate date, String day, String time,
                        String pokemonName, int cp, boolean caught, boolean shiny,
                        String weather, String park, String location,
                        String tag, String event, boolean incense, int incenseDuration) {
        this.date = date;
        this.day = day;
        this.time = time;
        this.pokemonName = pokemonName;
        this.cp = cp;
        this.caught = caught;
        this.shiny = shiny;
        this.weather = weather;
        this.park = park;
        this.location = location;
        this.tag = tag;
        this.event = event;
        this.incense = incense;
        this.incenseDuration = incenseDuration;
    }

    public PokemonEntry(int id, LocalDate date, String day, String time,
                        String pokemonName, int cp, boolean caught, boolean shiny,
                        String weather, String park, String location,
                        String tag, String event, boolean incense, int incenseDuration) {
        this(date, day, time, pokemonName, cp, caught, shiny,
                weather, park, location, tag, event, incense, incenseDuration);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDay() {
        return day;
    }

    public String getTime() {
        return time;
    }

    public String getPokemonName() {
        return pokemonName;
    }

    public int getCp() {
        return cp;
    }

    public boolean isCaught() {
        return caught;
    }

    public boolean isShiny() {
        return shiny;
    }

    public String getWeather() {
        return weather;
    }

    public String getPark() {
        return park;
    }

    public String getLocation() {
        return location;
    }

    public String getTag() {
        return tag;
    }

    public String getEvent() {
        return event;
    }

    public boolean isIncense() {
        return incense;
    }

    public int getIncenseDuration() {
        return incenseDuration;
    }
    
    public void setPokemonName(String pokemonName) {
        this.pokemonName = pokemonName;
    }

    public void setCp(int cp) {
        this.cp = cp;
    }

    public void setCaught(boolean caught) {
        this.caught = caught;
    }

    public void setShiny(boolean shiny) {
        this.shiny = shiny;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public void setPark(String park) {
        this.park = park;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setIncense(boolean incense) {
        this.incense = incense;
    }

    public void setIncenseDuration(int incenseDuration) {
        this.incenseDuration = incenseDuration;
    }
}