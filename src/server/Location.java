package server;

import java.util.ArrayList;

public class Location {
	
	private int[] loc;
	
	String desc;
	ArrayList<Item> items;
	boolean[] exits = {false,false,false,false,false,false};
	String title;
	
	Location (int xAux, int yAux, int zAux) {
		loc = new int[3];
		loc[0]=xAux;
		loc[1]=yAux;
		loc[2]=zAux;
		title = "Algun sitio";
		desc = "No hay nada especial aqui";
		items = new ArrayList<Item>();
	}
	
	Location (int xAux, int yAux, int zAux, String aDesc, String aTitle) {
		loc = new int[3];
		loc[0]=xAux;
		loc[1]=yAux;
		loc[2]=zAux;
		desc = aDesc;
		title = aTitle;
		items = new ArrayList<Item>();
	}
	
	Location (int xAux, int yAux, int zAux, String aDesc, String aTitle, ArrayList<Item> aItems, boolean[] aExits) {
		loc = new int[3];
		loc[0]=xAux;
		loc[1]=yAux;
		loc[2]=zAux;
		desc = aDesc;
		title = aTitle;
		items = aItems;
		exits[0] = aExits[0];
		exits[1] = aExits[1];
		exits[2] = aExits[2];
		exits[3] = aExits[3];
		exits[4] = aExits[4];
		exits[5] = aExits[5];
	}
	
	public int[] getCoords () {
		return loc;
	}
	
	public ArrayList<Player> getPlayers () {
		ArrayList<Player> pL = new ArrayList<Player>();
		synchronized (Server.lock) {
			for (Player p : Server.playerList) {
				if (p.getLocation().equals(this)) pL.add(p);
			}
		}
		return pL;
	}

	public void roomMessage(String string) {
		ArrayList<Player> playersInRoom = getPlayers();
		for (Player p : playersInRoom) {
			p.send(string);
		}
	}

	public boolean[] getExits() {
		return exits;
	}

	public int getX() {
		return loc[0];
	}

	public int getY() {
		return loc[1];
	}

	public int getZ() {
		return loc[2];
	}
	
	void addItem(Item i) {
		items.add(i);
	}
	
	public Player getOnlinePlayerRoomByName(String input) {
		ArrayList<Player> playersInRoom = getPlayers();
		for (Player p : playersInRoom) {
			if (p.name.equals(input)) {
				return p;
			}
		}
		return null;
	}

}
