# Professions

A very customizable profession API for bukkit servers. This API includes some examples of professions.

Creating a customizable profession is fairly easy for an experienced programmer, the intention was to make it as easy as possible.

To create a custom profession, you will need to know about these classes:
Profession - the main profession class
ProfessionType - the profession type (serves as a generic argument to Profession), used to distinguish different profession types
ItemType - class that manages items
ItemTypeHolder - class that manages ItemType (saves and loads data from files)
User - an extension of Player in which some data is stored
UserProfessionData - here is all the user's profession data stored, accessed via User methods
ProfessionManager - a manager that manages profession registrations and queries and also stores professions in memory

AbstractProfessionListener - the listener in which you will further call events to handle in a profession (this is optional)

Firstly, you want to make an ItemType. To do that, simply create a class and extend ItemType. The generic argument can be anything - this is the object you will later handle! For example, say we had some hunting profession, we would look for EntityTypes, thus the argument would be EntityType, we would create some class, let's say some class called Prey<EntityType>). Or some mining profession, we would look for Material, thus the argument would be Material (or if you require more data than Material, you can make your own class in which you store the data and Material).

Secondly, we want to register the ItemType. We will register it by calling ProfessionManager#registerItemTypeHolder. To do that, we need to create an ItemTypeHolder, this does not need to be a dedicated class, you can create the ItemTypeHolder as an anonymous inner class. In ItemTypeHolder you will need to override getItemType() method, in which you can create ANY EXAMPLE of the ItemType, this will help with the serialization. For example, take the previous mentioned hunting profession, you would make the method return an EntityType.SKELETON (for example). Do not forget to register the ItemTypeHolder.

Thirdly, you would want to make a class extend Profession and override the abstract methods.
For example, if you would like to a mining-oriented Profession, you would extend Profession<IMining>.
You may create your own profession type by extending IProfessionType and registering it via ProfessionManager#registerProfessionType.
In the Profession class you will want to register the holder in onPostLoad() method by calling addItems(Class<ItemTypeHolder>) and handle events. You will need to check whether or not the event is valid, first (checks for player requirements, level requirements and such) by calling isValidEvent(ProfessionEvent, ItemType) method.

Lastly, you will need to call the event. You will do this by handling the desired bukkit event. Create a class that extends AbstractProfessionListener and register the Listener in your onEnable() (AbstractProfessionListener does implement Listener). For example, for the hunting profession you will look for EntityDeathEvent. You will then want to make sure the event is valid. After that, to call the event, you will want to call the callEvent(Player, ItemTypeGenericArgument, ItemTypeClass). For example, in hunting profession, you would want to call callEvent(event.getEntity().getKiller(), event.getEntity().getType(), Prey.class). This will call an event to all professions that registered Prey as an ItemType to listen to.

I will very soon add code examples.
