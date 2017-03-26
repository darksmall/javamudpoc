package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;

public class Server {

	static int port = 25565;
	static final int tTick = 1000;
	static boolean exit = false;
	static ServerSocket serverSocket;
	static ArrayList<Player> playerList = new ArrayList<Player>();
	static ArrayList<Player[]> combatList = new ArrayList<Player[]>(), combatDelete = new ArrayList<Player[]>();
	static String[] bannedNames = { "nuevo" };
	static Thread ticker;
	static boolean tick = true;
	static Map map;
	static File mapFile = new File("data/map.bin");
	public static Socket incomingSock;
	public static Object lock = new Object();

	public static void main(String args[]) {

		// read map from save file

		if (mapFile.exists()) {
			System.out.println("Encontrado archivo de mapa, cargando...");
			try {
				map = new Map(mapFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
			System.out.println("Mapa cargado");
		} else {
			map = new Map();
		}

		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Escuchando en el puerto " + port);
		} catch (IOException e) {
			System.out.println("No se ha podido escuchar en el puerto " + port);
			System.exit(1);
		}

		ticker = new Thread(new Runnable() {

			public void run() {
				long resto;
				Date start, end;
				// int counter=0;

				while (tick) {
					start = new Date();

					mainPlayerLoop();
					deleteCombats();
					performCombat();

					end = new Date();
					resto = (tTick - (end.getTime() - start.getTime()));
					if (resto < 0) {
						System.out.println("No se consigue seguir el ritmo! Extra: " + (resto));
						continue;
					} else {
						try {
							Thread.sleep(resto);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				}

			}

		}

		);

		ticker.start();

		while (exit == false) {

			try {

				if (!ticker.isAlive())
					ticker.start();

				incomingSock = serverSocket.accept();

				System.out.println("Conexion entrante desde: " + incomingSock.getInetAddress().toString());

				Thread accepter = new Thread(new Runnable() {
					public void run() {
						Player pl = new Player(incomingSock);
						addPlayer(pl);
					}
				});

				accepter.start();

			} catch (IOException e) {

				System.out.println("Ha fallado una conexión");

			}

		}

	}

	public static boolean stringCheck(String[] in, String compTo) {
		for (int i = 0; i < (in.length - 1); i++) {
			if (in[i].equalsIgnoreCase(compTo))
				return true;
		}
		return false;
	}
	
	public static void addPlayer(Player p) {
		synchronized(lock){
			playerList.add(p);
		}
	}
	
	public static void removePlayer(Player p) {
		synchronized(lock){ 
			playerList.remove(p);
		}
	}
	
	public static int numPlayers() {
		synchronized(lock){ 
			return playerList.size();
		}
	}
	
	public static Player getPlayer(int n) {
		synchronized(lock){ 
			return playerList.get(n);
		}
	}

	public static void mainPlayerLoop() {
		synchronized(lock){ 
			ListIterator<Player> li = playerList.listIterator();
			Player p; 
			while(li.hasNext()) {
				p = li.next();
				checkCommands(p);
				checkTimeout(p, li);
			}
		}
	}

	public static void deleteCombats() {
		if (combatDelete.isEmpty())
			return;
		for (Player[] pA : combatDelete) {
			combatList.remove(pA);
		}
		combatDelete.clear();
	}

	public static void performCombat() {
		for (int i = 0; i < combatList.size(); i++) {

			Player[] combatArr = combatList.get(i);

			Player atacker = combatArr[0];
			Player objective = combatArr[1];

			if (atacker.stats.health == 0) {
				atacker.death();
				combatList.remove(combatArr);
				i -= 1;
				continue;
			}

			if (objective.stats.health == 0) {
				objective.death();
				combatList.remove(combatArr);
				i -= 1;
				continue;
			}

			if (atacker.objective == null) {
				if (objective.objective != null)
					continue;
				else {
					combatDelete.add(combatList.get(i));
					continue;
				}
			} else {

				if (objective.getLocation().equals(atacker.getLocation())) {

					if (objective.isDead) {
						atacker.send("Has matado a " + objective.getName());
						atacker.objective = null;
						continue;
					}

					if (atacker.inv.getWeapon().type == 1) {
						int defense = (roll20() + atacker.objective.stats.agility), atk = (roll20() + atacker.stats.strength);

						if (atk > defense) {
							objective.hurt(atacker.inv.getWeapon().damage);
							objective.send(atacker.getName() + " te golpea con sus punyos!");
							atacker.send("Golpeas a " + atacker.objective.getName() + " con tus punyos!");
						} else {
							objective.send("Has esquivado el golpe de " + atacker.getName());
							atacker.send(atacker.objective.getName() + " evita tu golpe!");
						}

					}

					Player[] nPA = { objective, atacker };
					combatList.set(i, nPA);

				} else {
					atacker.objective = null;
				}

			}

		}

	}

	public static void checkCommands(Player p) {
		String msg = p.getMsg();
		if (msg != null) {
			if (msg.contains("" + (char) 27)) {
				p.send(Format.CLEAR + "No hagas eso");
				return;
			}
			if (p.isDead) {
				p.send("Los muertos no hacen esas cosas");
				return;
			}
			String[] msgS = msg.split(" ");
			String actWo = msgS[0];
			if (actWo.equalsIgnoreCase("decir") && msgS.length > 1) {
				p.getLocation().roomMessage(Format.LIGHT_MAGENTA + p.name + " dice: " + Format.CLEAR + msg.substring(6));
			} else if (actWo.equalsIgnoreCase("salir") && msgS.length == 1) {
				System.out.println(p.sock.getInetAddress().toString() + "/" + p.getName() + " se fue");
				try {
					p.sock.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				p.loc.roomMessage(p.getName() + " se desvanece");
			} else if (actWo.equalsIgnoreCase("mirar") && msgS.length == 1) {
				p.look();
			} else if (actWo.equalsIgnoreCase("estado") && msgS.length == 1) {
				p.sendStatus();
			} else if (actWo.equals("n") || actWo.equals("norte") && msgS.length == 1) {
				p.moveN();
			} else if (actWo.equals("e") || actWo.equals("este") && msgS.length == 1) {
				p.moveE();
			} else if (actWo.equals("s") || actWo.equals("sur") && msgS.length == 1) {
				p.moveS();
			} else if (actWo.equals("o") || actWo.equals("oeste") && msgS.length == 1) {
				p.moveW();
			} else if (actWo.equals("ar") || actWo.equals("arriba") && msgS.length == 1) {
				p.moveU();
			} else if (actWo.equals("ab") || actWo.equals("abajo") && msgS.length == 1) {
				p.moveD();
			} else if (actWo.equals("atacar") && msgS.length == 2) {
				Player objective = p.loc.getOnlinePlayerRoomByName(msgS[1]);
				if (objective != null && !(objective.equals(p))) {

					if ((objective.equals(p))) {
						p.send("No te puedes atacar a ti mismo");
						return;
					}

					p.objective = objective;
					Player[] aP = { p, p.objective };
					combatList.add(aP);
					p.send("Atacas a " + objective.getName());
					objective.send(p.getName() + " te esta atacando");
					if (objective.objective == null)
						objective.objective = p;
					return;
				}
				p.send(msgS[1] + " no se encuentra aqui");
			} else if (actWo.equals("parar")) {

			} else {
				p.send(Format.LIGHT_RED + "No te entiendo" + Format.CLEAR);
			}

		}

	}

	public static void checkTimeout(Player p, ListIterator<Player> li) {

		try {
			p.outS.write(0);
		} catch (IOException e) {
			if (p.loc != null) {
				p.loc.roomMessage(p.getName() + " se desvanece");
			}
			System.out.println(p.sock.getInetAddress().toString() + "/" + p.getName() + " dio timeout");
			try {
				p.sock.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			li.remove();
		}

	}

	public static int roll20() {
		return (int) (Math.random() * 20);
	}

}
