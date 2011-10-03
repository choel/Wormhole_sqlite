/*
 *   Wormhole X-Treme Plugin for Bukkit
 *   Copyright (C) 2011  Ben Echols
 *                       Dean Bailey
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wormhole_xtreme.wormhole;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import com.wormhole_xtreme.wormhole.config.ConfigManager;
import com.wormhole_xtreme.wormhole.logic.StargateHelper;
import com.wormhole_xtreme.wormhole.model.Stargate;
import com.wormhole_xtreme.wormhole.model.StargateManager;
import com.wormhole_xtreme.wormhole.model.StargateShape;
import com.wormhole_xtreme.wormhole.permissions.StargateRestrictions;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions;
import com.wormhole_xtreme.wormhole.permissions.WXPermissions.PermissionType;
import com.wormhole_xtreme.wormhole.utils.WorldUtils;

/**
 * WormholeXtreme Player Listener.
 * 
 * @author Ben Echols (Lologarithm)
 * @author Dean Bailey (alron)
 */
class WormholeXTremePlayerListener extends PlayerListener
{

    /**
     * Button lever hit.
     * 
     * @param player
     *            the p
     * @param clickedBlock
     *            the clicked
     * @param direction
     *            the direction
     * @return true, if successful
     */
    private static boolean buttonLeverHit(final Player player, final Block clickedBlock, BlockFace direction)
    {
        final Stargate stargate = StargateManager.getGateFromBlock(clickedBlock);

        if (stargate != null)
        {
            if (WorldUtils.isSameBlock(stargate.getGateDialLeverBlock(), clickedBlock) && ((stargate.isGateSignPowered() && WXPermissions.checkWXPermissions(player, stargate, PermissionType.SIGN)) || ( !stargate.isGateSignPowered() && WXPermissions.checkWXPermissions(player, stargate, PermissionType.DIALER))))
            {
                handleGateActivationSwitch(stargate, player);
            }
            else if (WorldUtils.isSameBlock(stargate.getGateIrisLeverBlock(), clickedBlock) && ( !stargate.isGateSignPowered() && WXPermissions.checkWXPermissions(player, stargate, PermissionType.DIALER)))
            {
                stargate.toggleIrisActive(true);
            }
            else if (WorldUtils.isSameBlock(stargate.getGateIrisLeverBlock(), clickedBlock) || WorldUtils.isSameBlock(stargate.getGateDialLeverBlock(), clickedBlock))
            {
                player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
            }
            return true;
        }
        else
        {
        	if (player.getItemInHand().getTypeId() == 57) 
        	{
            if (direction == null)
            {
                switch (clickedBlock.getData())
                {
                    case 1 :
                        direction = BlockFace.SOUTH;
                        break;
                    case 2 :
                        direction = BlockFace.NORTH;
                        break;
                    case 3 :
                        direction = BlockFace.WEST;
                        break;
                    case 4 :
                        direction = BlockFace.EAST;
                        break;
                    default :
                        break;
                }

                if (direction == null)
                {
                    return false;
                }
            }
            // Check to see if player has already run the "build" command.
            final StargateShape shape = StargateManager.getPlayerBuilderShape(player);

            Stargate newGate = null;
            if (shape != null)
            {
                newGate = StargateHelper.checkStargate(clickedBlock, direction, shape);
            }
            else
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINEST, false, "Attempting to find any gate shapes!");
                newGate = StargateHelper.checkStargate(clickedBlock, direction);
            }

            if (newGate != null)
            {
                if (WXPermissions.checkWXPermissions(player, newGate, PermissionType.BUILD) && !StargateRestrictions.isPlayerBuildRestricted(player))
                {
                    if (newGate.isGateSignPowered() && player.getItemInHand().getTypeId() == 57)
                    {
                    	//take a diamond block
                    	ItemStack is = player.getItemInHand();//If I put the setAmount here it would not return an ItemStack
                    	is.setAmount(is.getAmount() - 1);
                    	player.setItemInHand(is); 
                    	player.sendMessage("\u00A73:: \u00A77Consuming a diamond block to complete the Stargate");
                    	player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Stargate Design Valid with Sign Nav.");
                    		if (newGate.getGateName().equals(""))
                    		{
                    			player.sendMessage(ConfigManager.MessageStrings.constructNameInvalid.toString() + "\"\"");
                    		}
                    		else
                    		{
                    			final boolean success = StargateManager.completeStargate(player, newGate);
                    			if (success && player.getItemInHand().getTypeId() == 57)
                    			{
                    				player.sendMessage(ConfigManager.MessageStrings.constructSuccess.toString());
                    				newGate.getGateDialSign().setLine(0, "-" + newGate.getGateName() + "-");
                    				newGate.getGateDialSign().setData(newGate.getGateDialSign().getData());
                    				newGate.getGateDialSign().update();
                    			}
                    			else
                            	{
                            	player.sendMessage("Stargate constrution failed!?");
                            	}
                    		}
                    }
                    else
                    {
                    	if (!newGate.isGateSignPowered()) {
                        // Print to player that it was successful!
                        player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Valid Stargate Design! \u00A73:: \u00A7B<required> \u00A76[optional]");
                        player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Type \'\u00A7F/wxcomplete \u00A7B<name> \u00A76[idc=IDC] [net=NET]\u00A77\' to complete.");
                        // Add gate to unnamed gates.
                        StargateManager.addIncompleteStargate(player, newGate);
                    	} else { 
                    		//Tell them that they need a diamond block
                    		player.sendMessage("\u00A73:: \u00A77You must be holding a diamond block to construct a Stargate");
                    		player.sendMessage("\u00A73:: \u00A77Your sign was damaged by not using a diamond block");
                    		player.sendMessage("\u00A73:: \u00A77Please fix your sign and hold a diamond block when you flip the lever");
                    	}
                    }
                    return true;
                }
                else
                {
                    if (newGate.isGateSignPowered())
                    {
                        newGate.resetTeleportSign();
                    }
                    StargateManager.removeIncompleteStargate(player);
                    if (StargateRestrictions.isPlayerBuildRestricted(player))
                    {
                        player.sendMessage(ConfigManager.MessageStrings.playerBuildCountRestricted.toString());
                    }
                    player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
                    return true;
                }
            }
            else
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, player.getName() + " has pressed a button or lever but did not find any properly created gates.");
            }
        }
        }
        return false;
        
    }

    /**
     * Handle gate activation switch.
     * 
     * @param stargate
     *            the stargate
     * @param player
     *            the player
     * @return true, if successful
     */
    private static boolean handleGateActivationSwitch(final Stargate stargate, final Player player)
    {
        if (stargate.isGateActive() || stargate.isGateLightsActive())
        {
            if (stargate.getGateTarget() != null)
            {
                //Shutdown stargate
                stargate.shutdownStargate(true);
                player.sendMessage(ConfigManager.MessageStrings.gateShutdown.toString());
                return true;
            }
            else
            {
                final Stargate s2 = StargateManager.removeActivatedStargate(player);
                if ((s2 != null) && (stargate.getGateId() == s2.getGateId()))
                {
                    stargate.stopActivationTimer();
                    stargate.setGateActive(false);
                    stargate.toggleDialLeverState(false);
                    stargate.lightStargate(false);
                    player.sendMessage(ConfigManager.MessageStrings.gateDeactivated.toString());
                    return true;
                }
                else
                {
                    if (stargate.isGateLightsActive() && !stargate.isGateActive())
                    {
                        player.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Gate has been activated by someone else already.");
                    }
                    else
                    {
                        player.sendMessage(ConfigManager.MessageStrings.gateRemoveActive.toString());
                    }
                    return false;
                }
            }
        }
        else
        {
            if (stargate.isGateSignPowered())
            {
                if (WXPermissions.checkWXPermissions(player, stargate, PermissionType.SIGN))
                {
                    if ((stargate.getGateDialSign() == null) && (stargate.getGateDialSignBlock() != null))
                    {
                        stargate.tryClickTeleportSign(stargate.getGateDialSignBlock());
                    }

                    if (stargate.getGateDialSignTarget() != null)
                    {
                        if (stargate.dialStargate(stargate.getGateDialSignTarget(), false))
                        {
                            player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Stargates connected!");
                            return true;
                        }
                        else
                        {
                            player.sendMessage(ConfigManager.MessageStrings.gateRemoveActive.toString());
                            return false;
                        }
                    }
                    else
                    {
                        player.sendMessage(ConfigManager.MessageStrings.targetInvalid.toString());
                        return false;
                    }
                }
                else
                {
                    player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
                    return false;
                }
            }
            else
            {
                //Activate Stargate
                player.sendMessage(ConfigManager.MessageStrings.gateActivated.toString());
                player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Chevrons Locked! \u00A73:: \u00A7B<required> \u00A76[optional]");
                player.sendMessage(ConfigManager.MessageStrings.normalHeader.toString() + "Type \'\u00A7F/dial \u00A7B<gatename> \u00A76[idc]\u00A77\'");
                StargateManager.addActivatedStargate(player, stargate);
                stargate.startActivationTimer(player);
                stargate.lightStargate(true);
                return true;
            }
        }
    }

    /**
     * Handle player interact event.
     * 
     * @param event
     *            the event
     * @return true, if successful
     */
    private static boolean handlePlayerInteractEvent(final PlayerInteractEvent event)
    {
        final Block clickedBlock = event.getClickedBlock();
        final Player player = event.getPlayer();

        if ((clickedBlock != null) && ((clickedBlock.getTypeId() == 77) || (clickedBlock.getTypeId() == 69)))
        {
            if (buttonLeverHit(player, clickedBlock, null))
            {
                return true;
            }
        }
        else if ((clickedBlock != null) && (clickedBlock.getTypeId() == 68))
        {
            final Stargate stargate = StargateManager.getGateFromBlock(clickedBlock);
            if (stargate != null)
            {
                if (WXPermissions.checkWXPermissions(player, stargate, PermissionType.SIGN))
                {
                    if (stargate.tryClickTeleportSign(clickedBlock, player))
                    {
                        return true;
                    }
                }
                else
                {
                    player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Handle player move event.
     * 
     * @param event
     *            the event
     * @return true, if successful
     */
    private static boolean handlePlayerMoveEvent(final PlayerMoveEvent event)
    {
        final Player player = event.getPlayer();
        final Location toLocFinal = event.getTo();
        final Block gateBlockFinal = toLocFinal.getWorld().getBlockAt(toLocFinal.getBlockX(), toLocFinal.getBlockY(), toLocFinal.getBlockZ());
        final Stargate stargate = StargateManager.getGateFromBlock(gateBlockFinal);

        if ((stargate != null) && stargate.isGateActive() && (stargate.getGateTarget() != null) && (gateBlockFinal.getTypeId() == (stargate.isGateCustom()
            ? stargate.getGateCustomPortalMaterial().getId()
            : stargate.getGateShape() != null
                ? stargate.getGateShape().getShapePortalMaterial().getId()
                : Material.STATIONARY_WATER.getId())))
        {
            String gatenetwork;
            if (stargate.getGateNetwork() != null)
            {
                gatenetwork = stargate.getGateNetwork().getNetworkName();
            }
            else
            {
                gatenetwork = "Public";
            }
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player in gate:" + stargate.getGateName() + " gate Active: " + stargate.isGateActive() + " Target Gate: " + stargate.getGateTarget().getGateName() + " Network: " + gatenetwork);

            if (ConfigManager.getWormholeUseIsTeleport() && ((stargate.isGateSignPowered() && !WXPermissions.checkWXPermissions(player, stargate, PermissionType.SIGN)) || ( !stargate.isGateSignPowered() && !WXPermissions.checkWXPermissions(player, stargate, PermissionType.DIALER))))
            {
                player.sendMessage(ConfigManager.MessageStrings.permissionNo.toString());
                return false;
            }

            if (ConfigManager.isUseCooldownEnabled())
            {
                if (StargateRestrictions.isPlayerUseCooldown(player))
                {
                    player.sendMessage(ConfigManager.MessageStrings.playerUseCooldownRestricted.toString());
                    player.sendMessage(ConfigManager.MessageStrings.playerUseCooldownWaitTime.toString() + StargateRestrictions.checkPlayerUseCooldownRemaining(player));
                    return false;
                }
                else
                {
                    StargateRestrictions.addPlayerUseCooldown(player);
                }
            }

            if (stargate.getGateTarget().isGateIrisActive())
            {
                player.sendMessage(ConfigManager.MessageStrings.errorHeader.toString() + "Remote Iris is locked!");
                player.setNoDamageTicks(5);
                event.setFrom(stargate.getGatePlayerTeleportLocation());
                event.setTo(stargate.getGatePlayerTeleportLocation());
                //player.teleport(stargate.getGatePlayerTeleportLocation());
                return true;
            }

            final Location target = stargate.getGateTarget().getGatePlayerTeleportLocation();
            player.setNoDamageTicks(5);
            event.setFrom(target);
            event.setTo(target);
            //player.teleport(target);
            if (target != stargate.getGatePlayerTeleportLocation())
            {
                WormholeXTreme.getThisPlugin().prettyLog(Level.INFO, false, player.getName() + " used wormhole: " + stargate.getGateName() + " to go to: " + stargate.getGateTarget().getGateName());
            }
            if (ConfigManager.getTimeoutShutdown() == 0)
            {
                stargate.shutdownStargate(true);
            }
            return true;
        }
        else if (stargate != null)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Player entered gate but wasn't active or didn't have a target.");
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.player.PlayerListener#onPlayerBucketEmpty(org.bukkit.event.player.PlayerBucketEmptyEvent)
     */
    @Override
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event)
    {
        if ( !event.isCancelled())
        {
            final Stargate stargate = StargateManager.getGateFromBlock(event.getBlockClicked());
            if ((stargate != null) || StargateManager.isBlockInGate(event.getBlockClicked()))
            {
                event.setCancelled(true);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.player.PlayerListener#onPlayerBucketFill(org.bukkit.event.player.PlayerBucketFillEvent)
     */
    @Override
    public void onPlayerBucketFill(final PlayerBucketFillEvent event)
    {
        if ( !event.isCancelled())
        {
            final Stargate stargate = StargateManager.getGateFromBlock(event.getBlockClicked());
            if ((stargate != null) || StargateManager.isBlockInGate(event.getBlockClicked()))
            {
                event.setCancelled(true);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.player.PlayerListener#onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent)
     */
    @Override
    public void onPlayerInteract(final PlayerInteractEvent event)
    {
        if (event.getClickedBlock() != null)
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Caught Player: \"" + event.getPlayer().getName() + "\" Event type: \"" + event.getType().toString() + "\" Action Type: \"" + event.getAction().toString() + "\" Event Block Type: \"" + event.getClickedBlock().getType().toString() + "\" Event World: \"" + event.getClickedBlock().getWorld().toString() + "\" Event Block: " + event.getClickedBlock().toString() + "\"");
            if (handlePlayerInteractEvent(event))
            {
                event.setCancelled(true);
                WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Cancelled Player: \"" + event.getPlayer().getName() + "\" Event type: \"" + event.getType().toString() + "\" Action Type: \"" + event.getAction().toString() + "\" Event Block Type: \"" + event.getClickedBlock().getType().toString() + "\" Event World: \"" + event.getClickedBlock().getWorld().toString() + "\" Event Block: " + event.getClickedBlock().toString() + "\"");
            }
        }
        else
        {
            WormholeXTreme.getThisPlugin().prettyLog(Level.FINE, false, "Caught and ignored Player: \"" + event.getPlayer().getName() + "\" Event type: \"" + event.getType().toString() + "\"");
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.player.PlayerListener#onPlayerMove(org.bukkit.event.player.PlayerMoveEvent)
     */
    @Override
    public void onPlayerMove(final PlayerMoveEvent event)
    {
        if (handlePlayerMoveEvent(event))
        {
            ///event.setCancelled(true);
        }
    }
}
