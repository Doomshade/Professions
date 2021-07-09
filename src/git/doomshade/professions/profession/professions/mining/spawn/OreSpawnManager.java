package git.doomshade.professions.profession.professions.mining.spawn;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.ISetup;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;


public class OreSpawnManager implements ISetup {


    private static OreSpawnManager instance;
    private File oreLocations;
    private FileConfiguration loader;

    private OreSpawnManager() {
    }

    public static OreSpawnManager getInstance() {
        return instance;
    }

    @Override
    public void setup() throws Exception {
        if (instance == null) {
            instance = this;
        }

        this.oreLocations = new File(Professions.getInstance().getAdditionalDataFolder(), ".yml");
        this.loader = YamlConfiguration.loadConfiguration(oreLocations);

    }


    @Override
    public void cleanup() throws Exception {

        loader.save(oreLocations);
    }
}
