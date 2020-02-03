package git.doomshade.professions.profession.types.mining.spawn;

import git.doomshade.professions.utils.ISetup;
import org.apache.commons.lang3.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;


public class OreSpawnManager implements ISetup {


    private static OreSpawnManager instance;
    private File oreLocations;
    private FileConfiguration loader;

    private OreSpawnManager(File oreLocations) {
        Validate.notNull(oreLocations, "Ore file cannot be null");
        this.oreLocations = oreLocations;
        this.loader = YamlConfiguration.loadConfiguration(oreLocations);
    }

    public static void init(File oreLocations) {
        instance = new OreSpawnManager(oreLocations);
    }

    public static OreSpawnManager getInstance() {
        return instance;
    }

    @Override
    public void setup() throws Exception {

    }
}
