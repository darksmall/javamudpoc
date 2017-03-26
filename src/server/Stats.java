package server;

public class Stats {
	
	int health, maxHealth;
	int strength=5, agility=5, intelligence=5, endurance=5, knowledge=5;
	
	Stats() {
		//TODO new player stats constructor
		maxHealth=endurance*4;
		health=maxHealth;		
	}

}
