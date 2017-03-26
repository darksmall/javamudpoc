package server;

public class Inventory {
	
	Item weaponSlot;
	
	
	Inventory() {
		//TODO default inventory
		weaponSlot=Item.fists;

	}


	public Item getWeapon() {
		return weaponSlot;
	}

}
