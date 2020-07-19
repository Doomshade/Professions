package git.doomshade.professions.data;

import git.doomshade.professions.exceptions.ConfigurationException;

/**
 * @author Doomshade
 * @version NOT_YET_IMPLEMENTED
 */
public class SaveSettings extends AbstractSettings {
    private static final String SAVING = "saving";

    SaveSettings() {

    }

    @Override
    public void setup() throws ConfigurationException {
        super.setup();
        if (isSection(SAVING)) {

        }
    }

}
