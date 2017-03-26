package server;

public class Format {
	
	public static final String base=((char) 27 + "[");
	public static final String BLACK=(base+"30m"), RED=(base+"31m"), GREEN=(base+"32m"), YELLOW=(base+"33m"), BLUE=(base+"34m"), 
			MAGENTA=(base+"35m"), CIAN=(base+"36m"), GRAY=(base+"37m"), CLEAR=(base+"0m"), LIGHT_BLACK=(base+"30;1m"),
			LIGHT_RED=(base+"31;1m"), LIGHT_GREEN=(base+"32;1m"), LIGHT_YELLOW=(base+"33;1m"), LIGHT_BLUE=(base+"34;1m"),
			LIGHT_MAGENTA=(base+"35;1m"), LIGHT_CIAN=(base+"36;1m"), LIGHT_GRAY=(base+"37;1m");

}
