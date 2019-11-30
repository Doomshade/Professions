# Professions

A very customizable profession API for bukkit servers. This API includes some examples of professions.

Creating a customizable profession is fairly easy for an experienced programmer, the intention was to make it as easy as possible. Basic OOP and Serialization knowledge is required.

To create a custom profession, you will need to know about these classes:
- [Profession](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/Profession.java) - the main profession class
- [IProfessionType](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/profession/types/IProfessionType.java) - the profession type (serves as a generic argument to Profession), used to differentiate professions
- [ItemType](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/profession/types/ItemType.java) - class that manages items
- [ItemTypeHolder](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/profession/types/ItemTypeHolder.java) - class that manages ItemType (saves and loads data from files)
- [ProfessionManager](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/ProfessionManager.java) - a manager that manages profession registrations and queries and also stores professions in memory
- [AbstractProfessionListener](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/listeners/AbstractProfessionListener.java) - the listener in which you will further call events to handle in a profession (this is optional)

1. Firstly, you want to make an ItemType. To do that, simply create a class and extend ItemType. The generic argument can be anything - this is the object you will later handle! For example, say we had some hunting profession, we would look for EntityTypes, thus the argument would be EntityType, we would create some class, let's say some class called Prey<EntityType>). Or some mining profession, we would look for Material, thus the argument would be Material (or if you require more data than Material, you can make your own class in which you store the data and Material). Override both constructors like so:
  
```
public class Mob {
    final String configName;
    final EntityType type;

    Mob(EntityType type, String configName) {
        this.type = type;
        this.configName = configName;
    }

    public Mob(EntityType type) {
        this(type, "");
    }

    boolean isMythicMob() {
        return !configName.isEmpty();
    }

}

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
In this example I used a custom class as the generic argument. The argument could have easily been EntityType, but I wanted to check, whether or not the mob was a MythicMob. I also left out some code. To see the full class: [Prey](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/profession/types/hunting/Prey.java)

2. Secondly, we want to register the ItemType. We will register it by calling ProfessionManager#registerItemTypeHolder. To do that, we need to create an ItemTypeHolder, this does not need to be a dedicated class, you can create the ItemTypeHolder as an anonymous inner class. In ItemTypeHolder you will need to override getItemType() method, in which you can create ANY EXAMPLE of the ItemType, this will help with the serialization. For example, take the previous mentioned hunting profession, you would make the method return an EntityType.SKELETON (for example). Do not forget to register the ItemTypeHolder.
```
public class YourPlugin extends JavaPlugin {
  
  @Override
  public void onEnable() {
    ProfessionManager.getInstance().registerItemTypeHolder(new ItemTypeHolder<Prey>() {
            @Override
            public Prey getItemType() {
                Prey prey = new Prey(new Mob(EntityType.SKELETON), 10);
                prey.setName(ChatColor.YELLOW + "Skeleton");
                return prey;
            }
        });
  }
}

``` 
3. Thirdly, you would want to make a class extend Profession and override the abstract methods.
For example, if you would like to a mining-oriented Profession, you would extend Profession<IMining>.
You may create your own profession type by extending IProfessionType and registering it via ProfessionManager#registerProfessionType.
In the Profession class you will want to register the holder in onLoad() method by calling addItems(Class<ItemTypeHolder>) and handle events. You will need to check whether or not the event is valid, first by calling isValidEvent(ProfessionEvent, ItemType) method.
``` 
public final class SkinningProfession extends Profession<IHunting> {

    @Override
    public void onLoad() {
      addItems(Prey.class);
    }

    @Override
    public String getID() {
        return "skinning";
    }

    @Override
    @EventHandler
    public <A extends ItemType<?>> void onEvent(ProfessionEvent<A> e) {
    
        // This conversion is not needed. What this accomplishes is basically casting the e.getItemType() to Prey (makes it easier to read, also no need to cast the object)
        ProfessionEvent<Prey> event = getEvent(e, Prey.class);
        
        // This validates the event - checks, if the event is actually a Prey event. This also checks whether or not the player meets the level requirement to receive experience.
        if (!isValidEvent(event, Prey.class) || !playerMeetsLevelRequirements(event)) {
            return;
        }
        
        // Adds exp to the player.
        addExp(event);
    }

}
``` 
4. Lastly, you will need to call the event. You will do this by handling the desired bukkit event. Create a class that extends AbstractProfessionListener and register the Listener in your onEnable() (AbstractProfessionListener does implement Listener). For example, for the hunting profession you will look for EntityDeathEvent. You will then want to make sure the event is valid. After that, to call the event, you will want to call the callEvent(Player, ItemTypeGenericArgument, ItemTypeClass). For example, in hunting profession, you would want to call callEvent(event.getEntity().getKiller(), event.getEntity().getType(), Prey.class). This will call an event to all professions that registered Prey as an ItemType to listen to.

```
public class ProfessionListener extends AbstractProfessionListener {

    @Override
    @EventHandler
    public void onKill(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        if (entity == null || !(entity.getKiller() instanceof Player)) {
            return;
        }
        callEvent(entity.getKiller(), new Mob(entity.getType()), Prey.class);
        
        // Another example of getting the event and adding some extras for you to handle in profession
        // ProfessionEvet<Prey> event = getEvent(entity.getKiller(), new Mob(entity.getType()), Prey.class)
        // event.addExtra(some_extra_object)
        
        // In your Profession in onEvent() method:
        // Object some_extra_object = (Object) event.getExtra(Some_Extra_Object.class);
    }
    
}


public class YourPlugin extends JavaPlugin {
  
  @Override
  public void onEnable() {
    Bukkit.getPluginManager().registerEvents(new ProfessionListener(), this);
    ProfessionManager profMan = ProfessionManager.getInstance();
    profMan.registerItemTypeHolder(new ItemTypeHolder<Prey>() {
            @Override
            public Prey getItemType() {
                Prey prey = new Prey(new Mob(EntityType.SKELETON), 10);
                prey.setName(ChatColor.YELLOW + "Skeleton");
                return prey;
            }
        });
    }
    profMan.registerProfession(new SkinningProfession());
  }
}
```

So the full integration would look like this:

```
public class YourPlugin extends JavaPlugin {
  
  @Override
  public void onEnable() {
    Bukkit.getPluginManager().registerEvents(new ProfessionListener(), this);
    ProfessionManager profMan = ProfessionManager.getInstance();
    profMan.registerItemTypeHolder(new ItemTypeHolder<Prey>() {
            @Override
            public Prey getItemType() {
                Prey prey = new Prey(new Mob(EntityType.SKELETON), 10);
                prey.setName(ChatColor.YELLOW + "Skeleton");
                return prey;
            }
        });
    }
    profMan.registerProfession(new SkinningProfession());
  }
}

public class Mob {
    final String configName;
    final EntityType type;

    Mob(EntityType type, String configName) {
        this.type = type;
        this.configName = configName;
    }

    public Mob(EntityType type) {
        this(type, "");
    }

    boolean isMythicMob() {
        return !configName.isEmpty();
    }

}

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
        Map<String, Object> map = new HashMap<>();
        map.put(ENTITY.s, object.type.name());
        map.put(CONFIG_NAME.s, object.configName);
        return map;
    }

    @Override
    protected Mob deserializeObject(Map<String, Object> map) {
    
        String entityTypeName = (String) map.get(ENTITY.s);
        String configName = (String) map.get(CONFIG_NAME.s);
        
        for (EntityType et : EntityType.values()) {
            if (et.name().equals(entityTypeName)) {
                return new Mob(et, configName);
            }
        }
        throw new IllegalArgumentException(entityTypeName + " is not a valid entity type name!");
    }

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

    @Override
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        return IHunting.class;
    }
}
public class ProfessionListener extends AbstractProfessionListener {

    @Override
    @EventHandler
    public void onKill(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        if (entity == null || !(entity.getKiller() instanceof Player)) {
            return;
        }
        callEvent(entity.getKiller(), new Mob(entity.getType()), Prey.class);
    }
    
}

public final class SkinningProfession extends Profession<IHunting> {

    @Override
    public void onLoad() {
      addItems(Prey.class);
    }

    @Override
    public String getID() {
        return "skinning";
    }

    @Override
    @EventHandler
    public <A extends ItemType<?>> void onEvent(ProfessionEvent<A> e) {
        ProfessionEvent<Prey> event = getEvent(e, Prey.class);
        if (!isValidEvent(event, Prey.class) || !playerMeetsLevelRequirements(event)) {
            return;
        }
        
        addExp(event);
    }

}

```

And that's it! You can make so much more, such as creating your own [CommandHandler](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/commands/AbstractCommandHandler.java) and [Commands](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/commands/AbstractCommand.java) (see [example](https://github.com/Doomshade/Professions/tree/master/src/git/doomshade/professions/profession/types/mining/commands)).

There's also an option to make some items craftable or trainable. The best example would be [EnchantedItemType](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/profession/types/enchanting/EnchantedItemType.java). Focus on the serialize() and deserialize methods:

```
public class EnchantedItemType extends ItemType<Enchant> implements ITrainable, ICraftable {
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.putAll(ITrainable.serializeTrainable(this));
        map.putAll(ICraftable.serializeCraftable(this));
        return map;
    }
    
    @Override
    public void deserialize(Map<String, Object> map) throws ProfessionInitializationException {
        super.deserialize(map);
        ITrainable.deserializeTrainable(map, this);
        ICraftable.deserializeCraftable(map, this);
    }
}
```

Plugin will then handle in the profession GUI the onClick() events and check whether or not the clicked ItemType is instanceof ICraftable. For ITrainable, this is used in [ProfessionTrainerTrait](https://github.com/Doomshade/Professions/blob/master/src/git/doomshade/professions/trait/ProfessionTrainerTrait.java) (hook with Citizens) where you add a trait to an NPC, modifying what trainer the NPC should be, and then, after right clicking the NPC, a GUI will open with all the ITrainables.
