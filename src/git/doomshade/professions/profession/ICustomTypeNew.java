package git.doomshade.professions.profession;

import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.SerializeField;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public abstract class ICustomTypeNew<T> {
    private ItemType<T> itemType;

    public ICustomTypeNew(ItemType<T> itemType) {
        this.itemType = itemType;
    }

    @Deprecated
    public final void deserialize(Map<String, Object> map) throws Exception {
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(SerializeField.class)) {
                String key = field.getAnnotation(SerializeField.class).value();
                try {
                    field.setAccessible(true);
                    field.set(this, map.get(key));
                } catch (IllegalAccessException e) {
                    Professions.logError(e);
                }
            }
        }
    }

    @Deprecated
    public final Map<String, Object> serialize() throws Exception {
        Map<String, Object> map = new HashMap<>();
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(SerializeField.class)) {
                String key = field.getAnnotation(SerializeField.class).value();
                try {
                    field.setAccessible(true);
                    map.put(key, field.get(this));
                } catch (IllegalAccessException e) {
                    Professions.logError(e);
                }
            }
        }
        return map;
    }

    public ItemType<T> getItemType() {
        return itemType;
    }

}
