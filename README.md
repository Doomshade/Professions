# Professions

A very customizable profession API for bukkit servers. This API includes some examples of professions.

Creating a customizable profession is fairly easy for an experienced programmer, the intention was to make it as easy as possible.


To create a custom profession, you will need to know about these classes:
[Profession](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/Profession.java) - the main profession class
[IProfessionType](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/profession/types/IProfessionType.java) - the profession type (serves as a generic argument to Profession), used to distinguish different profession types
[ItemType](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/profession/types/ItemType.java) - class that manages items
[ItemTypeHolder](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/profession/types/ItemTypeHolder.java) - class that manages ItemType (saves and loads data from files)
[User](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/user/User.java) - an extension of Player in which some data is stored
[UserProfessionData](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/user/UserProfessionData.java) - here is all the user's profession data stored, accessed via User methods
[ProfessionManager](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/ProfessionManager.java) - a manager that manages profession registrations and queries and also stores professions in memory

[AbstractProfessionListener](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/listeners/AbstractProfessionListener.java) - the listener in which you will further call events to handle in a profession (this is optional)

Firstly, you want to make an ItemType. To do that, simply create a class and extend ItemType. The generic argument can be anything - this is the object you will later handle! For example, say we had some hunting profession, we would look for EntityTypes, thus the argument would be EntityType, we would create some class, let's say some class called Prey<EntityType>). Or some mining profession, we would look for Material, thus the argument would be Material (or if you require more data than Material, you can make your own class in which you store the data and Material).
  
```
public class Prey extends ItemType<Mob> {

    

    /**
     * Required constructor
     */
    public Prey() {
        super();
    }

    /**
     * Required constructor
     *
     * @param object
     * @param exp
     */
    public Prey(Mob object, int exp) {
        super(object, exp);
    }
    
    @Override
    protected Map<String, Object> getSerializedObject(Mob object) {
    
        // This is basic serialization of a class
        Map<String, Object> map = new HashMap<>();
        map.put(ENTITY.s, object.type.name());
        map.put(CONFIG_NAME.s, object.configName);
        return map;
    }

    @Override
    protected Mob deserializeObject(Map<String, Object> map) {

        // This is a basic deserialization of a class
        String entityTypeName = (String) map.get(ENTITY.s);
        String configName = (String) map.get(CONFIG_NAME.s);
        
        // Look for the entity type in bukkit's EntityType enum, if found, return the deserialized object, if not, throw an exception
        for (EntityType et : EntityType.values()) {
            if (et.name().equals(entityTypeName)) {
                return new Mob(et, configName);
            }
        }
        throw new IllegalArgumentException(entityTypeName + " is not a valid entity type name!");
    }

    // A custom enum for better readability. 
    enum PreyEnum {
        ENTITY("entity"), CONFIG_NAME("config-name");

        public final String s;

        PreyEnum(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }
    }

    // The declared profession type
    @Override
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        return IHunting.class;
    }

}
```
In this example I left out some code. To see the full class: [Prey](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/profession/types/hunting/Prey.java)

Secondly, we want to register the ItemType. We will register it by calling ProfessionManager#registerItemTypeHolder. To do that, we need to create an ItemTypeHolder, this does not need to be a dedicated class, you can create the ItemTypeHolder as an anonymous inner class. In ItemTypeHolder you will need to override getItemType() method, in which you can create ANY EXAMPLE of the ItemType, this will help with the serialization. For example, take the previous mentioned hunting profession, you would make the method return an EntityType.SKELETON (for example). Do not forget to register the ItemTypeHolder.

Thirdly, you would want to make a class extend Profession and override the abstract methods.
For example, if you would like to a mining-oriented Profession, you would extend Profession<IMining>.
You may create your own profession type by extending IProfessionType and registering it via ProfessionManager#registerProfessionType.
In the Profession class you will want to register the holder in onPostLoad() method by calling addItems(Class<ItemTypeHolder>) and handle events. You will need to check whether or not the event is valid, first (checks for player requirements, level requirements and such) by calling isValidEvent(ProfessionEvent, ItemType) method.

Lastly, you will need to call the event. You will do this by handling the desired bukkit event. Create a class that extends AbstractProfessionListener and register the Listener in your onEnable() (AbstractProfessionListener does implement Listener). For example, for the hunting profession you will look for EntityDeathEvent. You will then want to make sure the event is valid. After that, to call the event, you will want to call the callEvent(Player, ItemTypeGenericArgument, ItemTypeClass). For example, in hunting profession, you would want to call callEvent(event.getEntity().getKiller(), event.getEntity().getType(), Prey.class). This will call an event to all professions that registered Prey as an ItemType to listen to.

I will very soon add code examples.
