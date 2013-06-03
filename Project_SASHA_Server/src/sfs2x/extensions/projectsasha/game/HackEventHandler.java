package sfs2x.extensions.projectsasha.game;
import sfs2x.extensions.projectsasha.game.entities.GameWorld;
import sfs2x.extensions.projectsasha.game.entities.Player;
import sfs2x.extensions.projectsasha.game.entities.gateways.*;
import sfs2x.extensions.projectsasha.game.entities.software.*;
import sfs2x.extensions.projectsasha.game.utils.RoomHelper;
import sfs2x.extensions.projectsasha.game.utils.TimerHelper;


import com.smartfoxserver.v2.entities.User;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class HackEventHandler extends BaseClientRequestHandler{
	public void handleClientRequest(User sender, ISFSObject params){
		int time;
		boolean neutralize;
		boolean success = false;
		GameWorld world = RoomHelper.getWorld(this);
		Player p = new Player(sender);
		
		
		Gateway from = world.gateways.get(params.getUtfString("gatewayFrom"));
		Gateway to = world.gateways.get(params.getUtfString("gatewayTo"));
		neutralize = params.getBool("neutralize");

		if(to.getOwner()!=null)
		{
			if(neutralize == true)
			{
				success = this.hack(world, from, to);
				trace("Hack requesto from " + p.getUserName() + ": from " + from.getState()+" to " + to.getState() + ": " +  (success?"SUCCESS":"FAIL"));
				
				if(success == true)
				{
					// to � da neutralizzare per 60 secondi
				}
			}
			else	//il gateway deve essere conquistato
			{	
				success = this.hack(world, from, to, GameConsts.CONQUER_TIME_TRESHOLD);
				trace("Hack requesto from " + p.getUserName() + ": from " + from.getState()+" to " + to.getState() + ": " +  (success?"SUCCESS":"FAIL"));
			}
				
		}
		else
		{
			success = this.hack(world, from, to);
			trace("Hack requesto from " + p.getUserName() + ": from " + from.getState()+" to " + to.getState() + ": " +  (success?"SUCCESS":"FAIL"));
		}
		ISFSObject reback = SFSObject.newInstance();
		reback.putBool("success", success);
		send("hack", reback, sender);
		
	}
	
	public boolean hack(GameWorld world, Gateway from, Gateway to){
		return hack(world, from, to, 0);
	}
	
	public boolean hack(GameWorld world, Gateway from, Gateway to, int extraTime){
		boolean ret = false;
		long startTime, endTime;
		int difference = this.difference(from, to);
		
		Software[] attackerSw = from.getInstalledSoftwares();
		Software[] defenderSw = to.getInstalledSoftwares();
		
		for(Software sw: attackerSw)
			if(sw!=null)
				switch(sw.getType())
				{
					case GameConsts.DICTIONARY:
						sw.runTriggeredAction(from, to);
						break;
					default:
						break;
				}
		
		for(Software sw: defenderSw)
			if(sw!=null)
				switch(sw.getType())
				{
					case GameConsts.IDS:
						if(to.getOwner()!=null)
							sw.runTriggeredAction(from, to);
						break;
					case GameConsts.VIRUS:
						sw.runTriggeredAction(from, to);
						break;
					case GameConsts.DEEPTHROAT:
						sw.runTriggeredAction(from, to);
					default:
						break;
				}
		
		if(difference > 0)
		{
			int waitTime = this.hackTime(world, from, to) + extraTime;
			startTime = System.currentTimeMillis();
			endTime = startTime+(waitTime*1000);
			while(System.currentTimeMillis() != endTime)
			{
				/*BUSY WAIT*/
				//currentTick = System.currentTimeMillis()-starTime;
				//	if(currentTick%1000==0)
				//		send a countdown to the player for remaining hack time??
			}
			to.setOwner(from.getOwner());
			ret = true;
		}
		else
		{
			startTime = System.currentTimeMillis();
			endTime = startTime+(GameConsts.FAILTIME*1000);
			while(System.currentTimeMillis() != endTime)
			{
				/*BUSY WAIT*/
				//currentTick = System.currentTimeMillis()-starTime;
				//	if(currentTick%1000==0)
				//		send a countdown to the player??
			}
			ret = false;
		}
		return ret;
	}
	
	public int powerDifference(Gateway from, Gateway to)
	{
		return to.getAttackLevel()-from.getDefenceLevel();
	}
	
	public int hackTime(GameWorld world, Gateway from, Gateway to)
	{
		int bonus;
		int diff = this.powerDifference(from, to);
		
		int[] timeToLeave = {
						15,15,15,15,15,15,15,15,15, 	// 1-9
						30,30,30,30,30,30,30,30,30,30,	//10-19
						30,30,30,30,30,30,30,30,30,30,	//20-29
						40,40,40,40,40,40,40,40,40,40,	//30-39
						40,40,40,40,40,40,40,40,40,40,	//40-49
						50,50,50,50,50,50,50,50,50,50,	//50-59
						50,50,50,50,50,50,50,50,50,50,	//60-69
						60,60,60,60,60,60,60,60,60,60,	//70-79
						60,60,60,60,60,					//80-84
						80,80,80,80,80,80,80,80,80,80	//85-94
						};
		
		bonus = 10*from.getOwner().getConqueredGateway(world, GameConsts.SCI_GATEWAY);
		
		return 120 - timeToLeave[diff] - bonus;
	}
}
