package server;

public class Item {
	
	static Item fists=new Item("Puños", 1,1);
	
	String name;
	int type, damage; //1=weapon
	
	Item (String aname, int atype, int adamage) {
		type=atype;
		damage=adamage;
		name=aname;
	}

}
