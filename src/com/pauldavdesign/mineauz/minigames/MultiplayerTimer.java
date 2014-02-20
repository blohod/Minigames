package com.pauldavdesign.mineauz.minigames;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

public class MultiplayerTimer{
	private int playerWaitTime;
	private int startWaitTime;
	private Minigame minigame;
	private static Minigames plugin = Minigames.plugin;
	private PlayerData pdata = plugin.pdata;
	private boolean paused = false;
	private int taskID = -1;
	private List<Integer> timeMsg = new ArrayList<Integer>();
	
	public MultiplayerTimer(Minigame mg){
		minigame = mg;
		playerWaitTime = plugin.getConfig().getInt("multiplayer.waitforplayers");
		if(playerWaitTime <= 0)
			playerWaitTime = 10;
		if(minigame.getStartWaitTime() == 0	){
			startWaitTime = plugin.getConfig().getInt("multiplayer.startcountdown");
			if(startWaitTime <= 0)
				startWaitTime = 5;
		}
		else
			startWaitTime = minigame.getStartWaitTime();
		timeMsg.addAll(plugin.getConfig().getIntegerList("multiplayer.timerMessageInterval"));
	}
	
	public void startTimer(){
		playerWaitTime += 1;
//		startWaitTime += 1;
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				if(!paused){
					if(playerWaitTime != 0)
						playerWaitTime -= 1;
					else
						startWaitTime -= 1;
				}
				
				if(playerWaitTime != 0 && !paused){
					if(playerWaitTime == plugin.getConfig().getInt("multiplayer.waitforplayers")){
						sendPlayersMessage(ChatColor.GRAY + MinigameUtils.getLang("time.startup.waitingForPlayers"));
						sendPlayersMessage(ChatColor.GRAY + MinigameUtils.formStr("time.startup.time", playerWaitTime));
					}
					else if(timeMsg.contains(playerWaitTime)){
						sendPlayersMessage(ChatColor.GRAY + MinigameUtils.formStr("time.startup.time", playerWaitTime));
					}
				}
				else if(playerWaitTime == 0 && startWaitTime != 0 && !paused){
					if(startWaitTime == plugin.getConfig().getInt("multiplayer.startcountdown")){
						sendPlayersMessage(ChatColor.GRAY + MinigameUtils.getLang("time.startup.minigameStarts"));
						sendPlayersMessage(ChatColor.GRAY + MinigameUtils.formStr("time.startup.time", startWaitTime));
						freezePlayers(!minigame.canMoveStartWait());
						allowInteraction(minigame.canInteractStartWait());
					}
					else if(timeMsg.contains(startWaitTime)){
						sendPlayersMessage(ChatColor.GRAY + MinigameUtils.formStr("time.startup.time", startWaitTime));
					}
				}
				else if(playerWaitTime == 0 && startWaitTime == 0){
					if(startWaitTime == 0 && playerWaitTime == 0){
						sendPlayersMessage(ChatColor.GREEN + MinigameUtils.getLang("time.startup.go"));
						reclearInventories(minigame);
						pdata.startMPMinigame(minigame);
						freezePlayers(false);
						allowInteraction(true);
					}
					Bukkit.getScheduler().cancelTask(taskID);
				}
			}
		}, 0, 20);
	}
	
	private void sendPlayersMessage(String message){
		for(MinigamePlayer ply : minigame.getPlayers()){
			ply.sendMessage(message);
		}
	}
	
	private void reclearInventories(Minigame minigame){
		for(MinigamePlayer ply : minigame.getPlayers()){
			ply.getPlayer().getInventory().clear();
		}
	}
	
	private void freezePlayers(boolean freeze){
		for(MinigamePlayer ply : minigame.getPlayers()){
			ply.setFrozen(freeze);
		}
	}
	
	private void allowInteraction(boolean allow){
		for(MinigamePlayer ply : minigame.getPlayers()){
			ply.setCanInteract(allow);
		}
	}
	
	public int getPlayerWaitTimeLeft(){
		return playerWaitTime;
	}
	
	public int getStartWaitTimeLeft(){
		return startWaitTime;
	}
	
	public void setPlayerWaitTime(int time){
		playerWaitTime = time;
	}
	
	public void setStartWaitTime(int time){
		startWaitTime = time;
	}
	
	public void pauseTimer(){
		paused = true;
		for(MinigamePlayer ply : minigame.getPlayers()){
			ply.sendMessage(MinigameUtils.getLang("time.startup.timerPaused"), null);
		}
	}
	
	public void pauseTimer(String reason){
		paused = true;
		for(MinigamePlayer ply : minigame.getPlayers()){
			ply.sendMessage(MinigameUtils.formStr("time.startup.timerPaused", reason), null);
		}
	}
	
	public void removeTimer(){
		if(taskID != -1){
			Bukkit.getScheduler().cancelTask(taskID);
		}
	}
	
	public void resumeTimer(){
		paused = false;
		for(MinigamePlayer ply : minigame.getPlayers()){
			ply.sendMessage(MinigameUtils.getLang("time.startup.timerResumed"), null);
		}
	}
	
	public boolean isPaused(){
		return paused;
	}
}
