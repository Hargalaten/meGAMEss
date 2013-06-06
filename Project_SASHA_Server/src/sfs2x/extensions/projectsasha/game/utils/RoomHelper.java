package sfs2x.extensions.projectsasha.game.utils;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import com.smartfoxserver.v2.extensions.SFSExtension;
import sfs2x.extensions.projectsasha.game.GameExtension;
import sfs2x.extensions.projectsasha.game.entities.GameWorld;
import sfs2x.extensions.projectsasha.game.entities.Player;
import sfs2x.extensions.projectsasha.game.entities.Region;
import sfs2x.extensions.projectsasha.game.entities.gateways.Gateway;
import sfs2x.extensions.projectsasha.game.objectives.Objective;


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

}
