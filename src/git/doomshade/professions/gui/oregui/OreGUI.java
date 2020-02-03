package git.doomshade.professions.gui.oregui;

import git.doomshade.guiapi.*;
import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.profession.types.mining.OreItemType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class OreGUI extends GUI {
    public static final String ORE_LOCATION = "ore-location";
    private Location oreLocation = null;

    protected OreGUI(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
    }

    @Override
    public void init() throws GUIInitializationException {
        GUIInventory.Builder builder = getInventoryBuilder();
        final ItemTypeHolder<OreItemType> holder = Professions.getProfessionManager().getItemTypeHolder(OreItemType.class);

        int i = -1;
        for (OreItemType ore : holder.getRegisteredItemTypes()) {
            GUIItem item = new GUIItem(ore.getGuiMaterial(), ++i);
            item.changeItem(this, () -> ore.getIcon(null).getItemMeta());
            builder = builder.withItem(item);
        }

        this.oreLocation = getContext().getContext(ORE_LOCATION);
    }

    @Override
    public void onGuiClick(GUIClickEvent e) {
    }
}
