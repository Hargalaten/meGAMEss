package sfs2x.projectsasha.game.entities.software;

import sfs2x.projectsasha.game.GameConsts;
import sfs2x.projectsasha.game.entities.gateways.Gateway;
import sfs2x.projectsasha.game.utils.TimerHelper;


public class IDS extends Software
{
	
	
	public IDS() 
	{
		super(GameConsts.IDS_NAME, 1);
		setCumulative(GameConsts.IDS_CUMULATIVE);
		setType(GameConsts.IDS);
		setDescription(GameConsts.IDS_DESCRIPTION);
		setCost(GameConsts.IDS_COST,1);

	}
	
	
	public IDS(int version) 
	{
		super(GameConsts.IDS_NAME, version);
		setCumulative(GameConsts.IDS_CUMULATIVE);
		setType(GameConsts.IDS);
		setDescription(GameConsts.IDS_DESCRIPTION);
		setCost(GameConsts.IDS_COST,version);
	}
	
	@Override
	public void upgrade()
	{
		if (this==null)
			return;
		if(this.version < GameConsts.IDS_MAX_LEVEL)
			this.version += 1;
		else
			return;
	}

	public int getDetection(int version)
	{
		return GameConsts.IDS_DETECTION - (25 * this.version);
	}
	
	@Override
	public void runTriggeredAction(Gateway from, Gateway to)
	{
		int time = this.getDetection(this.getVersion());
		
		new TimerHelper(time, this, from, to); //start timed event with delayed time
		
		for(int i = time; i>=0; i--);
		//funzione avviso hack
	}

	@Override
	public void startTimedEvent(Gateway from, Gateway to)
	{
		//Add delayed event here
	}
	

}
