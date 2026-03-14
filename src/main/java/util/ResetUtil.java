package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class ResetUtil {

    public static void resetAll() {
        resetDatabase();
        resetPreferences();
        System.out.println("All user data cleared. Program will start fresh.");
    }

    //tar bort databasen
    private static void resetDatabase() {
        try {
            Path db = findDatabase();
            if (db != null && Files.exists(db)) {
                Files.delete(db);
                System.out.println("Deleted database: " + db);
            } else {
                System.out.println("No database found.");
            }
        } catch (IOException e) {
            System.out.println("Could not delete database: " + e.getMessage());
        }
    }

    //raderar sparade preferences
    private static void resetPreferences() {
        try {
            Preferences root = Preferences.userRoot();
            //ta bort poketracker noden och allt under
            if (root.nodeExists("poketracker")) {
                root.node("poketracker").removeNode();
                root.flush();
                System.out.println("Deleted preferences (username, chart settings).");
            } else {
                System.out.println("No preferences found.");
            }
        } catch (BackingStoreException e) {
            System.out.println("Could not delete preferences: " + e.getMessage());
        }
    }

    private static Path findDatabase() {
        // vanliga platser där databasen kan ligga
        String[] candidates = {
                "pokemon_stats.db",
                "statistics/pokemon_stats.db",
                System.getProperty("user.home") + "/pokemon_stats.db",
        };
        for (String c : candidates) {
            Path p = Path.of(c);
            if (Files.exists(p)) return p;
        }
        return null;
    }

    // kör direkt från kommandoraden mvn exec:java -Dexec.mainClass=util.ResetUtil
    public static void main(String[] args) {
        System.out.println("=== Pokémon Statistics - Reset All Data ===");
        resetAll();
    }
}
