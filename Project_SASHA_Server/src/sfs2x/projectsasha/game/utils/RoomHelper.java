package sfs2x.projectsasha.game.utils;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import com.smartfoxserver.v2.extensions.SFSExtension;
import sfs2x.projectsasha.game.GameExtension;
import sfs2x.projectsasha.game.entities.GameWorld;
import sfs2x.projectsasha.game.entities.Objective;
import sfs2x.projectsasha.game.entities.Player;
import sfs2x.projectsasha.game.entities.Region;
import sfs2x.projectsasha.game.entities.gateways.Gateway;


// Helper methods to easily get current room or zone and precache the link to ExtensionHelper
public class RoomHelper {

	public static Room getCurrentRoom(BaseClientRequestHandler handler) 
	{
		return handler.getParentExtension().getParentRoom();
	}
	
	public static Room addPlayer(BaseClientRequestHandler handler) 
	{
		return handler.getParentExtension().getParentRoom();
	}

	public static Room getCurrentRoom(SFSExtension extension)
	{
		return extension.getParentRoom();
	}

	public static GameWorld getWorld(BaseClientRequestHandler handler) 
	{
		GameExtension ext = (GameExtension) handler.getParentExtension();
		return ext.getWorld();
	}
	
	public static Objective[] getObjectives(BaseClientRequestHandler handler) 
	{
		GameExtension ext = (GameExtension) handler.getParentExtension();
		return ext.getObjectives();
	}
	
	public static Player getPlayer(BaseClientRequestHandler handler, String name) 
	{
		GameExtension ext = (GameExtension) handler.getParentExtension();
		return ext.getPlayer(name);
	}
	
	public static Player getPlayer(BaseServerEventHandler handler, String name) 
	{
		GameExtension ext = (GameExtension) handler.getParentExtension();
		return ext.getPlayer(name);
	}
	
	public static void putPlayer(BaseClientRequestHandler handler, Player p) 
	{
		GameExtension ext = (GameExtension) handler.getParentExtension();
		ext.putPlayer(p);
	}
	
	public static Gateway getPolicePosition(BaseClientRequestHandler handler) 
	{
		GameExtension ext = (GameExtension) handler.getParentExtension();
		return ext.getPolicePosition();
	}
	
	public static GameWorld getWorld(BaseServerEventHandler handler)
	{
		GameExtension ext = (GameExtension) handler.getParentExtension();
		return ext.getWorld();
	}
	
	public static Region getGatewayRegion(BaseClientRequestHandler handler, Gateway g)
	{
		GameExtension ext = (GameExtension) handler.getParentExtension();
		return ext.getGatewayRegion(g);
	}

	public static long getTimer(BaseClientRequestHandler handler) {
		GameExtension ext = (GameExtension) handler.getParentExtension();
		return ext.getTime();
	}

}
