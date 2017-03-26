package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Player {
	
	String name, desc;
	Socket sock;
	PrintWriter out;
	BufferedReader in;
	Location loc;
	Inventory inv;
	Stats stats;
	OutputStream outS;
	Player objective=null;
	boolean isAdmin=false, isDead=false;
	
	Player(Socket inSock) {
		sock=inSock;
		
		try {
			
			outS = sock.getOutputStream();
			out = new PrintWriter(outS);	
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out.println(Format.RED + "COMO OSAS ENTRAR AQUI?" + Format.CLEAR);
			out.print(Format.LIGHT_RED + "Dime tu nombre (o \"" + Format.RED + "nuevo\" " + Format.LIGHT_RED + 
					"si eres nuevo aqui): " + Format.CLEAR);
			out.flush();
			String input = null;
			while (true) {
				input = in.readLine();
				if (input!=null) break;
			}

			
			if (input.equalsIgnoreCase("nuevo")) {
				
				while (true) {
					out.print("Cual es tu nombre?: ");
					out.flush();
					input = in.readLine();
					if (Server.stringCheck(Server.bannedNames, input)) {
						out.println("Nombre prohibido");
						out.flush();
						continue;
					}
					if (input.length()>14 || input.length()<3) {
						out.println("Tu nombre no puede ser mas largo que 13 caracteres y mas corto que 3");
						out.flush();
						continue;
					}
					if (input.contains(" ") || input.contains("" + (char) 27 )) {
						out.println("Nombre prohibido");
						out.flush();
						continue;
					}
					
					name = input;					
					inv = new Inventory();
					stats = new Stats();
					desc = "Recien llegado.";
					move(Server.map.getSpawn());
					loc.roomMessage("Aparece " + name + " de la nada!");
					System.out.println("Nuevo jugador " + name);
					break;
					
				}
				
			} else {
				// TODO read player save from file
				System.out.println(sock.getInetAddress().toString() + " se fue");
				sock.close();
			}
			
			
		} catch (IOException e) {
			System.out.println(inSock.getInetAddress().toString() + " se fue");
			//e.printStackTrace(System.out);
			return;
		} catch (NullPointerException e) {
			e.printStackTrace();
			try {
				inSock.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		
		
	}
	
	public void send (String msg) {
		if (out!=null) {
			out.println(msg);
			out.flush();
		} else {
			System.out.println(this.sock.getInetAddress().toString() + " se fué");
			try {
				sock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Server.removePlayer(this);
		}
		
	}
	
	public String getMsg() {
		String output=null;
		
		try {
			if (in.ready()) {
				output = in.readLine();
			}		
		} catch (IOException e) {
			System.out.println(sock.getInetAddress().toString() + " quit");
			Server.removePlayer(this);
		}
		return output;
	}
	
	public void sendDesc() {
		send(Format.BLUE + "-" +  loc.title + "-" + Format.CLEAR);
		send(Format.LIGHT_BLUE + loc.desc + Format.CLEAR);
		if (loc.items == null || !(loc.items.isEmpty())) {/*send("blabla"); TODO*/}
	}
	
	public void sendStatus() {
		send(Format.LIGHT_RED + "HP: " + stats.health + "/" + stats.maxHealth + Format.CLEAR);
	}
	
	public void move (Location nLoc) {
		this.loc=nLoc;
		look();
	}
	
	public void move (int x, int y, int z) {
		this.loc=Server.map.getLocation(x, y, z);
		look();
	}
	
	public void move (int[] aLoc) {
		this.loc=Server.map.getLocation(aLoc);
		look();
	}
	
	public void moveN() {
		if (this.loc.getExits()[0]==true) {
			loc.roomMessage(this.getName() + " se va al norte");
			int[] aLoc = {loc.getX()+1,loc.getY(),loc.getZ()};
			this.move(aLoc);
			loc.roomMessage("Llega " + this.getName() + " desde el sur");
		} else {
			this.send("No puedes ir por ahi");
		}		
	}
	
	public void moveE() {
		if (this.loc.getExits()[1]==true) {
			loc.roomMessage(this.getName() + " se va al este");
			int[] aLoc = {loc.getX(),loc.getY(),loc.getZ()+1};
			this.move(aLoc);
			loc.roomMessage("Llega " + this.getName() + " desde el oeste");
		} else {
			this.send("No puedes ir por ahi");
		}		
	}
	
	public void moveS() {
		if (this.loc.getExits()[2]==true) {
			loc.roomMessage(this.getName() + " se va al sur");
			int[] aLoc = {loc.getX()-1,loc.getY(),loc.getZ()};
			this.move(aLoc);
			loc.roomMessage("Llega " + this.getName() + " desde el norte");
		} else {
			this.send("No puedes ir por ahi");
		}		
	}
	
	public void moveW() {
		if (this.loc.getExits()[3]==true) {
			loc.roomMessage(this.getName() + " se va al oeste");
			int[] aLoc = {loc.getX(),loc.getY(),loc.getZ()-1};
			this.move(aLoc);
			loc.roomMessage("Llega " + this.getName() + " desde el este");
		} else {
			this.send("No puedes ir por ahi");
		}		
	}
	
	public void moveU() {
		if (this.loc.getExits()[4]==true) {
			loc.roomMessage(this.getName() + " se va hacia arriba");
			int[] aLoc = {loc.getX(),loc.getY()+1,loc.getZ()};
			this.move(aLoc);
			loc.roomMessage("Llega " + this.getName() + " desde abajo");
		} else {
			this.send("No puedes ir por ahi");
		}		
	}
	
	public void moveD() {
		if (this.loc.getExits()[5]==true) {
			loc.roomMessage(this.getName() + " se va hacia abajo");
			int[] aLoc = {loc.getX(),loc.getY()-1,loc.getZ()};
			this.move(aLoc);
			loc.roomMessage("Llega " + this.getName() + " desde arriba");
		} else {
			this.send("No puedes ir por ahi");
		}		
	}

	public Location getLocation() {
		return loc;
	}
	
	public void look () {
		sendDesc();
		ArrayList<Player> playersInRoom = this.loc.getPlayers();
		boolean[] exits = this.loc.getExits();
		String sPlayers="", sExits="";
		for (Player p : playersInRoom) {
			if (p.equals(this)) continue;
			if (playersInRoom.size()==1 || (playersInRoom.lastIndexOf(p)==(playersInRoom.size()-1))) sPlayers+=(p.getName());
			else sPlayers+=(p.getName() + ", ");
		}
		if (exits[0]==true) sExits+=("norte ");
		if (exits[1]==true) sExits+=("este ");
		if (exits[2]==true) sExits+=("sur ");
		if (exits[3]==true) sExits+=("oeste ");
		if (exits[4]==true) sExits+=("arriba ");
		if (exits[5]==true) sExits+=("abajo ");
		if (sExits.equals("")) sExits = "ninguna";
		if (sPlayers.equals("")) sPlayers = "estas solo";
		send(Format.GREEN + "Salidas: " + Format.LIGHT_GREEN + sExits + Format.CLEAR);
		send(Format.YELLOW + "Gente que ves: " + Format.LIGHT_YELLOW + sPlayers + Format.CLEAR);
	}

	String getName() {
		return name;
	}
	
	void hurt(int dmg) {
		this.stats.health-=dmg;
		if (this.stats.health<0) this.stats.health=0;
	}
	
	void heal(int pts) {
		this.stats.health+=pts;
		if (this.stats.health>this.stats.maxHealth) this.stats.health=this.stats.maxHealth;
	}

	void death() {
		isDead=true;
		send(Format.RED + "Has muerto!" + Format.CLEAR);
	}

}
