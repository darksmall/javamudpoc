package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

public class Map {
	
	HashSet<Location> map;
	
	Map () {
		
		map = new HashSet<Location>();
		map.add(new Location(0,0,0,"Estas en un sitio sin clase, CAMBIAME", "Spawn por defecto"));
		
	}
	
	public Map(File mapFile) throws FileNotFoundException {
		
		map = new HashSet<Location>();
		
		Scanner mapScanner = new Scanner(mapFile).useDelimiter("\n");
		
		while (mapScanner.hasNext()) {
			String in = mapScanner.next(), locdesc, loctitl;
			int locx,locy,locz;
			boolean ns, es, ss, os, ars, abs;
			boolean[] exits = new boolean[6];
			
			if (in.charAt(0)=='/') continue;
			
			Scanner inS = new Scanner(in).useDelimiter(",");
			
			locx=inS.nextInt();
			locy=inS.nextInt();
			locz=inS.nextInt();

			locdesc=inS.next();
			loctitl=inS.next();
			
			ns = inS.nextBoolean();
			es = inS.nextBoolean();
			ss = inS.nextBoolean();
			os = inS.nextBoolean();
			ars = inS.nextBoolean();
			abs = inS.nextBoolean();
			
			exits[0] = ns;
			exits[1] = es;
			exits[2] = ss;
			exits[3] = os;
			exits[4] = ars;
			exits[5] = abs;
			
			//TODO read items from map save
			
			map.add(new Location(locx,locy,locz,locdesc,loctitl, null, exits));
			
		}
		
	}
	
	void saveMap() {
		
		PrintStream mapps;
		
		System.out.println("Guardando mapa");
		
		try {
			mapps = new PrintStream(Server.mapFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}
		
		Server.mapFile.delete();
		
		try {
			Server.mapFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		mapps.println("/X,Y,Z,DESCRIPCION,TITULO,NORTE,ESTE,SUR,OESTE,ARRIBA,ABAJO,");
		
		for (Location loc : Server.map.map) {
			mapps.println(loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.desc + "," + loc.title + "," + 
		loc.getExits()[0] + "," + loc.getExits()[1] + "," + loc.getExits()[2] + "," + loc.getExits()[3] + "," 
					+ loc.getExits()[4] + "," + loc.getExits()[5] + ",");
		}
		
		mapps.close();
		
		System.out.println("Mapa guardado");
		
	}

	Location getLocation (int x, int y, int z) {
		int[] aLoc = new int[3];
		aLoc[0] = x;
		aLoc[1] = y;
		aLoc[2] = z;
		
		for (Location loc : map) {
			if (Arrays.equals(aLoc,loc.getCoords())) return loc;
		}
		
		return null;		
	}
	
	Location getLocation (int[] aLoc) {
		for (Location loc : map) {
			if (Arrays.equals(aLoc,loc.getCoords())) return loc;
		}
		
		return null;
	}
	
	void setLocation (Location loc) {
		map.add(loc);
	}
	
	public Location getSpawn() {
		Location loc = Server.map.getLocation(0,0,0);
		return loc;
	}

}
