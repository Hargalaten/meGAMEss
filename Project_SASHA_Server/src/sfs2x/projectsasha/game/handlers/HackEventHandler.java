package sfs2x.projectsasha.game.handlers;
import java.util.ArrayList;
import java.util.List;

import sfs2x.projectsasha.game.ia.AIThread;
import sfs2x.projectsasha.game.entities.Objective;
import sfs2x.projectsasha.game.utils.RoomHelper;
import sfs2x.projectsasha.game.GameConsts;
import sfs2x.projectsasha.game.entities.GameWorld;
import sfs2x.projectsasha.game.entities.Player;
import sfs2x.projectsasha.game.entities.gateways.Gateway;
import sfs2x.projectsasha.game.entities.software.DeepThroat;
import sfs2x.projectsasha.game.entities.software.DictionaryAttacker;
import sfs2x.projectsasha.game.entities.software.IDS;
import sfs2x.projectsasha.game.entities.software.Software;
import sfs2x.projectsasha.game.entities.software.Virus;


import com.smartfoxserver.v2.entities.User;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class HackEventHandler extends BaseClientRequestHandler
{
	public void handleClientRequest(User sender, ISFSObject params)
	{
		boolean neutralize = false;
		boolean success = false;
		boolean isVictoryReached = false;
		boolean isQuestComplete = false;
		int attackRelevance;
		List<Gateway> hackingPath = null;
		GameWorld world = RoomHelper.getWorld(this);
		Player p = RoomHelper.getPlayer(this, sender.getName());
		
		
		Gateway from = world.gateways.get(params.getUtfString("gatewayFrom"));
		Gateway to = world.gateways.get(params.getUtfString("gatewayTo"));
		neutralize = params.getBool("neutralize");
			
		if(from.getOwner()==null || from.getOwner()!=p)//if i'm owner of starting node
		{
			sendError("NOTOWNER", sender);
			return;
		}
		
		if(to.getOwner() == p)
		{
			sendError("SELFHACKING", sender);
			return;
		}
		
		if(!from.getOwner().canHack())//my hack is disabled
		{
			trace(from.getOwner().getName()+" cant hack...");
			sendError("HACKDISABLED", sender);
			return;
		}
		
		if(from.getDisabled() > System.currentTimeMillis())//starting node is disabled
		{
			sendError("ATTACKINGDISABLED", sender);
			return;
		}
		
		if(to.getDisabled() > System.currentTimeMillis())//ending node is disabled
		{
			sendError("ATTACKEDISABLED", sender);
			return;
		}
		
		attackRelevance = this.getAttackRelevance(from, to);
		
		if(to.getOwner()!=null && to.getOwner()!=p) // if there is an owner in attacked node and it's not me
		{
			

			if(from.getInstalledSoftware(GameConsts.PROXY) == null) //there is no proxy installed
			{
				for(Gateway g : from.getNeighboors())
				{
					if(g == to) //attacked gateway is a neighboor
					{
						hackingPath = new ArrayList<Gateway>();
						hackingPath.add(from);
						hackingPath.add(to);
						break;
					}
				}							
			}
			else //there is a proxy installed
			{
				hackingPath = from.tracePath(to, attackRelevance);					
			}
			
			if(hackingPath == null)
			{
				sendError("NOPATH", sender);
				return;
			}
			
			
			if(neutralize) // if neutralize
			{
				sendPathInfo(hackingPath, sender); // send path info to the client
				success = this.neutralize(world, from, to, sender);
				if(success)//disable hacked gateway for DISABLED_TIME seconds
				{
					 to.setDisabled(System.currentTimeMillis()+GameConsts.DISABLED_TIME*1000);
					 to.setOwner(null);
				}
			}
			else // if conquer
			{	
				sendPathInfo(hackingPath, sender); // send path info to the client
				success = this.hack(world, from, to, GameConsts.CONQUER_TIME_TRESHOLD, sender);
			}
			
			from.setTrace(hackingPath, attackRelevance);
			if(success)
				sendRefreshRequest(to.getOwner());
		}
		else // if there is an not an owner in attacked node
		{	
			if(from.getInstalledSoftware(GameConsts.PROXY) == null) //there is no proxy installed
			{
				for(Gateway g : from.getNeighboors())
				{
					if(g == to) //attacked gateway is a neighboor
					{
						hackingPath = new ArrayList<Gateway>();
						hackingPath.add(from);
						hackingPath.add(to);
						break;
					}
				}							
			}
			else //there is a proxy installed
			{
				hackingPath = from.tracePath(to, attackRelevance);
			}
			
			if(hackingPath == null)
			{
				sendError("NOPATH", sender);
				return;
			}
			
			sendPathInfo(hackingPath, sender); // send path info to the client
			success = this.hack(world, from, to, sender);
			from.setTrace(hackingPath, attackRelevance);
		}
		
				
		isVictoryReached = checkVictoryConditions(p);
		ISFSObject reback = SFSObject.newInstance();
		reback.putBool("success", success);
		reback.putBool("victoryReached", isVictoryReached);
		
		if(isVictoryReached)
		{
			reback.putBool("lost", true);
			List<User> ul = RoomHelper.getCurrentRoom(this).getUserList();
			ul.remove(sender);
			send("lost", reback, ul);//send a lost request to all the player who lost
		}
		
		isQuestComplete = checkQuestComplete(p,to);
		reback.putBool("questComplete", isQuestComplete);
		send("hack", reback, sender);
	}
	
	public boolean hack(GameWorld world, Gateway from, Gateway to, User sender)
	{
		return hack(world, from, to, 0, sender);
	}
	
	public boolean hack(GameWorld world, Gateway from, Gateway to, int extraTime, User sender)
	{
		boolean ret = false;
		if(changeStatus(from,to))
		{
			long startTime, endTime;
			int difference = this.powerDifference(from, to);
		
			Software[] attackerSw = from.getInstalledSoftwares();
			Software[] defenderSw = to.getInstalledSoftwares();
		
			/*
			 * FIXME: fix for JRE 1.6
			 */
			/*
			for(Software sw: attackerSw)
				if(sw!=null)
					if(sw.getType() == GameConsts.DICTIONARY)
						sw.runTriggeredAction(from, to);
			for(Software sw: defenderSw)
				if(sw!=null)
				{
					if(sw.getType() == GameConsts.IDS)
						if(to.getOwner()!=null)
							sw.runTriggeredAction(from, to);
					if(sw.getType() == GameConsts.VIRUS)
						sw.runTriggeredAction(from, to);
					if(sw.getType() == GameConsts.DEEPTHROAT)
						((DeepThroat)sw).runTriggeredAction(from, to, ((AIThread)world.ai).police);
				}
			*/
			for(Software sw: attackerSw)
				if(sw!=null)
					switch(sw.getType())
					{
						case GameConsts.DICTIONARY:
							((DictionaryAttacker)sw).runTriggeredAction(from, to);
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
								((IDS)sw).runTriggeredAction(from, to);
						break;
						case GameConsts.VIRUS:
							((Virus)sw).runTriggeredAction(from, to);
						break;
						case GameConsts.DEEPTHROAT:
							((DeepThroat)sw).runTriggeredAction(from, to, ((AIThread)world.ai).police);
						break;
						default:
						break;
					}
			
			Player currentOwner = from.getOwner();
			if(difference > 0)
			{
				int waitTime = this.hackTime(world, from, to) + extraTime;
				sendHackTimer(waitTime, sender);
				startTime = System.currentTimeMillis();
				endTime = startTime+(waitTime*1000);
				currentOwner.setCanHack(false); //disable hack for the following seconds
				while(System.currentTimeMillis() < endTime)
				{/*BUSY WAIT*/
					if(from.getOwner() == null)
					{
						currentOwner.setCanHack(true);
						freeStatus(from,to);
						return ret;
					}
				}
				currentOwner.setCanHack(true);
				to.setOwner(from.getOwner());
				freeStatus(from,to);
				ret = true;
			}
			else
			{
				startTime = System.currentTimeMillis();
				endTime = startTime+(GameConsts.FAILTIME*1000);
				int waitTime = (int) ((endTime-startTime)/1000);
				sendHackTimer(waitTime, sender);
				currentOwner.setCanHack(false); //disable hack for the following seconds
				while(System.currentTimeMillis() < endTime)
				{/*BUSY WAIT*/
					if(from.getOwner() == null)
					{
						currentOwner.setCanHack(true);
						freeStatus(from,to);
						return ret;
					}
				}
			
				currentOwner.setCanHack(true);
				ret = false;
				freeStatus(from,to);
				}
			}
			return ret;
		}
	
	public boolean neutralize(GameWorld world, Gateway from, Gateway to, User sender)
	{
		boolean ret = false;
		if(changeStatus(from,to))
		{
			long startTime, endTime;
			int difference = this.powerDifference(from, to);
			
			Software[] attackerSw = from.getInstalledSoftwares();
			Software[] defenderSw = to.getInstalledSoftwares();
			/*
			 * FIXME: fix for JRE 1.6
			 */
			/*
			for(Software sw: attackerSw)
				if(sw!=null)
					if(sw.getType() == GameConsts.DICTIONARY)
						sw.runTriggeredAction(from, to);
			for(Software sw: defenderSw)
				if(sw!=null)
				{
					if(sw.getType() == GameConsts.IDS)
						if(to.getOwner()!=null)
							sw.runTriggeredAction(from, to);
					if(sw.getType() == GameConsts.VIRUS)
						sw.runTriggeredAction(from, to);
					if(sw.getType() == GameConsts.DEEPTHROAT)
						((DeepThroat)sw).runTriggeredAction(from, to, ((AIThread)world.ai).police);
				}
			*/
			for(Software sw: attackerSw)
				if(sw!=null)
					switch(sw.getType())
					{
						case GameConsts.DICTIONARY:
							((DictionaryAttacker)sw).runTriggeredAction(from, to);
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
								((IDS)sw).runTriggeredAction(from, to);
						break;
						case GameConsts.VIRUS:
							((Virus)sw).runTriggeredAction(from, to);
						break;
						case GameConsts.DEEPTHROAT:
							((DeepThroat)sw).runTriggeredAction(from, to, ((AIThread)world.ai).police);
						break;
						default:
						break;
					}
	
			Player currentOwner = from.getOwner();
			if(difference > 0)
			{
				int waitTime = this.hackTime(world, from, to);
				startTime = System.currentTimeMillis();
				endTime = startTime+(waitTime*1000);
				sendHackTimer(waitTime, sender);
				currentOwner.setCanHack(false);//disable hack for the following seconds
				trace("waiting!");
				while(System.currentTimeMillis() < endTime)
				{/*BUSY WAIT*/
					if(from.getOwner() == null)
					{
						trace("i became freeeeee!");
						currentOwner.setCanHack(true);
						freeStatus(from,to);
						return ret;
					}
				}
	
				currentOwner.setCanHack(true);
				
				ret = true;
			}
			else
			{
				startTime = System.currentTimeMillis();
				endTime = startTime+(GameConsts.FAILTIME*1000);
				int waitTime = (int) ((endTime-startTime)/1000);
				sendHackTimer(waitTime, sender);
				currentOwner.setCanHack(false); //disable hack for the following seconds
				
				while(System.currentTimeMillis() < endTime)
				{/*BUSY WAIT*/
					if(from.getOwner() == null)
					{
						currentOwner.setCanHack(true);
						freeStatus(from,to);
						return ret;
					}
				}
				
				currentOwner.setCanHack(true);
				ret = false;
			}
		}
		return ret;
	}
	
	public int powerDifference(Gateway from, Gateway to)
	{
		return from.getAttackLevel()-to.getDefenceLevel();
	}
	
	public int getAttackRelevance(Gateway from, Gateway to)
	{
		float result = ((from.getAttackLevel() + to.getDefenceLevel()) / 200f) *10;
		Math.ceil(result);
		return (int)result;
	}
	
	@SuppressWarnings("unused")
	public int hackTime(GameWorld world, Gateway from, Gateway to)
	{
		int bonus, govBonus = 0;
		if(to.getOwner()!=null)
			govBonus = GameConsts.GOV_BONUS_MULTIPLIER * to.getOwner().getConqueredGateway(world, GameConsts.GOV_GATEWAY);
		int diff = this.powerDifference(from, to)+
				GameConsts.MIL_BONUS_MULTIPLIER * from.getOwner().getConqueredGateway(world, GameConsts.MIL_GATEWAY)-
				govBonus;
		
		int[] timeToLeave = {
						15,15,15,15,15,15,15,15,15, 	// 1-9
						25,25,25,25,25,25,25,25,25,25,	//10-19
						25,25,25,25,25,25,25,25,25,25,	//20-29
						35,35,35,35,35,35,35,35,35,35,	//30-39
						35,35,35,35,35,35,35,35,35,35,	//40-49
						45,45,45,45,45,45,45,45,45,45,	//50-59
						45,45,45,45,45,45,45,45,45,45,	//60-69
						55,55,55,55,55,55,55,55,55,55,	//70-79
						55,55,55,55,55,					//80-84
						65,65,65,65,65,65,65,65,65,65	//85-94
						};
		
		bonus = GameConsts.SCI_BONUS_MULTIPLIER*from.getOwner().getConqueredGateway(world, GameConsts.SCI_GATEWAY);
		
		/* FIXME: only for demo */
		return 10;//120 - timeToLeave[diff-1] - bonus;
	}
	
	public boolean checkVictoryConditions(Player p)
	{
		Objective[] objectives = RoomHelper.getObjectives(this);
		for(Objective o : objectives)
		{
			if(o.isObjectiveReached(o, p))
				return true;
		}
		return false;
	}
	
	public boolean checkQuestComplete(Player p, Gateway g)
	{
		for(int i = 0; i < p.getQuest().size();i++)
		{
			if(p.questComplete(p.getQuest().get(i), g))
			{
				if(p.getQuest().get(i).getRewardMoney()>0)
					p.addMoney(p.getQuest().get(i).getRewardMoney());
				else
				{
					p.getQuest().get(i).getRewardItem().setLock(false);
					p.addInventory(p.getQuest().get(i).getRewardItem());
				}
				return true;
			}
		}
		return false;
	}
	
	synchronized public static boolean changeStatus(Gateway from, Gateway to)
	{
		if(!(from.getBusy() || to.getBusy()))
		{
			from.setBusy(true);
			to.setBusy(true);
			return true;
		}
		return false;
	}
	
	synchronized public static void freeStatus(Gateway from, Gateway to)
	{
		from.setBusy(false);
		to.setBusy(false);
	}

	private void sendPathInfo(List<Gateway> hackingPath, User sender)
	{
		ISFSObject pathReback = SFSObject.newInstance();
		SFSArray pathArray = new SFSArray();
		for (int i = 0; i < hackingPath.size(); i++) 
			pathArray.addUtfString(hackingPath.get(i).getX()+":"+hackingPath.get(i).getY());
		trace("Sending path info to "+sender.getName());
		pathReback.putSFSArray("hackingPath", pathArray);
		send("path", pathReback, sender);
	}
	
	private void sendHackTimer(int seconds, User sender)
	{
		ISFSObject hackSeconds = SFSObject.newInstance();
		hackSeconds.putInt("seconds", seconds);
		trace("sending hack timer to "+sender.getName()+": "+seconds);
		send("hackTimer", hackSeconds, sender);
	}

	private void sendRefreshRequest(Player player)
	{
		//ISFSObject reback = SFSObject.newInstance();
		//reback.putUtfString("refresh", "REFRESH");
		send("refresh", new SFSObject(), RoomHelper.getCurrentRoom(this).getUserList());	
	}
	private void sendError(String errorType, User sender)
	{
		ISFSObject reback = SFSObject.newInstance();
		reback.putBool("success", false);
		
		if(errorType == "NOPATH")
		{
			trace("No hacking path available");
			reback.putUtfString("error", "NOPATH");
		}
		if(errorType == "HACKDISABLED")
		{
			trace("Hack request from " + sender.getName() + ": FAILED since the hack is disabled for this player");
			reback.putUtfString("error", "HACKDISABLED");
		}
		if(errorType == "NOTOWNER")
		{
			trace("Hack request from " + sender.getName() + ": FAILED since the player is not the owner of the gateway");
			reback.putUtfString("error", "NOTOWNER");
		}
		if(errorType == "ATTACKINGDISABLED")
		{
			trace("Hack request from " + sender.getName() + ": FAILED since the hack is disabled FROM this gateway for some seconds");
			reback.putUtfString("error", "ATTACKINGDISABLED");
		}
		if(errorType == "ATTACKEDISABLED")
		{
			trace("Hack request from " + sender.getName() + ": FAILED since the hack is disabled TO this gateway for some seconds");
			reback.putUtfString("error", "ATTACKEDISABLED");
		}
		if(errorType == "SELFHACKING")
		{
			trace("Hack request from " + sender.getName() + ": FAILED he is hacking one of his gateway");
			reback.putUtfString("error", "SELFHACKING");
		}
		send("error", reback, sender);	
	}
}
