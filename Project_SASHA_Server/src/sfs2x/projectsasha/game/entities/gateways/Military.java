package sfs2x.projectsasha.game.entities.gateways;

import sfs2x.projectsasha.game.GameConsts;
import sfs2x.projectsasha.game.entities.Player;

public class Military extends Gateway
{

	public Military(Player owner, String name, String state, int x, int y, float lat, float lon) 
	{
		super(owner, name, state, x, y, lat, lon);
	}

	
	//GETTERS
	
	@Override
	public int getPaymentAmount() 
	{
		return GameConsts.MIL_BASE_PAYMENT_AMOUNT;
	}

	@Override
	protected int getBaseAttackLevel()
	{
		return GameConsts.MIL_BASE_ATTACK_LEVEL;
	}

	@Override
	protected int getBaseDefenceLevel()
	{
		return GameConsts.MIL_BASE_DEFENCE_LEVEL;
	}
	
	@Override
	public void onGatewayConquered(Player newOwner, Player oldOwner)
	{
		
	}

}
