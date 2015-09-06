package au.com.mineauz.minigames;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import au.com.mineauz.minigames.degeneration.DegenerationModule;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.MinigameState;
import au.com.mineauz.minigames.minigame.modules.LobbySettingsModule;
import au.com.mineauz.minigames.minigame.modules.MultiplayerModule;
import au.com.mineauz.minigames.sounds.MGSounds;
import au.com.mineauz.minigames.sounds.PlayMGSound;

public class MultiplayerTimer{
	private int playerWaitTime;
	private int startWaitTime;
	private int oStartWaitTime;
	private Minigame minigame;
	private MultiplayerModule multiplayer;
	private static Minigames plugin = Minigames.plugin;
	private PlayerData pdata = plugin.pdata;
	private boolean paused = false;
	private int taskID = -1;
	private List<Integer> timeMsg = new ArrayList<Integer>();
	
	public MultiplayerTimer(Minigame mg){
		minigame = mg;
		multiplayer = minigame.getModule(MultiplayerModule.class);
		
		playerWaitTime = mg.getModule(LobbySettingsModule.class).getPlayerWaitTime();
		
		if (playerWaitTime == 0) {
			playerWaitTime = plugin.getConfig().getInt("multiplayer.waitforplayers");
			if(playerWaitTime <= 0)
				playerWaitTime = 10;
		}
		
		if(multiplayer.getStartWaitTime() == 0	){
			startWaitTime = plugin.getConfig().getInt("multiplayer.startcountdown");
			if(startWaitTime <= 0)
				startWaitTime = 5;
		}
		else
			startWaitTime = multiplayer.getStartWaitTime();
		oStartWaitTime = startWaitTime;
		timeMsg.addAll(plugin.getConfig().getIntegerList("multiplayer.timerMessageInterval"));
	}
	
	public void startTimer(){
//		playerWaitTime += 1;
//		startWaitTime += 1;
		if(taskID != -1)
			removeTimer();
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				LobbySettingsModule module = minigame.getModule(LobbySettingsModule.class);
				if(playerWaitTime != 0 && !paused){
					if(playerWaitTime == plugin.getConfig().getInt("multiplayer.waitforplayers")){
						sendPlayersMessage(ChatColor.GRAY + MinigameUtils.getLang("time.startup.waitingForPlayers"));
						sendPlayersMessage(ChatColor.GRAY + MinigameUtils.formStr("time.startup.time", playerWaitTime));
						minigame.setState(MinigameState.WAITING);
					}
					else if(timeMsg.contains(playerWaitTime)){
						sendPlayersMessage(ChatColor.GRAY + MinigameUtils.formStr("time.startup.time", playerWaitTime));
						PlayMGSound.playSound(minigame, MGSounds.getSound("timerTick"));
					}
				}
				else if(playerWaitTime == 0 && startWaitTime != 0 && !paused){
					if(startWaitTime == oStartWaitTime){
						minigame.setState(MinigameState.STARTING);
						sendPlayersMessage(ChatColor.GRAY + MinigameUtils.getLang("time.startup.minigameStarts"));
						sendPlayersMessage(ChatColor.GRAY + MinigameUtils.formStr("time.startup.time", startWaitTime));
						freezePlayers(!module.canMoveStartWait());
						allowInteraction(module.canInteractStartWait());
						if(module.isTeleportOnPlayerWait()){
							reclearInventories(minigame);
							pdata.startMPMinigame(minigame, true);
						}
					}
					else if(timeMsg.contains(startWaitTime)){
						sendPlayersMessage(ChatColor.GRAY + MinigameUtils.formStr("time.startup.time", startWaitTime));
						PlayMGSound.playSound(minigame, MGSounds.getSound("timerTick"));
					}
				}
				else if(playerWaitTime == 0 && startWaitTime == 0){
					sendPlayersMessage(ChatColor.GREEN + MinigameUtils.getLang("time.startup.go"));
					reclearInventories(minigame);
					if(module.isTeleportOnStart())
						pdata.startMPMinigame(minigame, true);
					else
						pdata.startMPMinigame(minigame, false);
					freezePlayers(false);
					allowInteraction(true);
					
					DegenerationModule degen = minigame.getModule(DegenerationModule.class);
					if (degen != null) {
						degen.start();
					}
					
					if(multiplayer.getTimer() > 0){
						minigame.setMinigameTimer(new MinigameTimer(minigame, multiplayer.getTimer()));
						minigame.broadcast(MinigameUtils.formStr("minigame.timeLeft", MinigameUtils.convertTime(multiplayer.getTimer())), MessageType.Normal);
					}
					
					Bukkit.getScheduler().cancelTask(taskID);
				}
				
				if(!paused){
					if(playerWaitTime != 0)
						playerWaitTime -= 1;
					else
						startWaitTime -= 1;
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
			ply.sendMessage(MinigameUtils.getLang("time.startup.timerPaused"), MessageType.Normal);
		}
	}
	
	public void pauseTimer(String reason){
		paused = true;
		for(MinigamePlayer ply : minigame.getPlayers()){
			ply.sendMessage(MinigameUtils.formStr("time.startup.timerPaused", reason), MessageType.Normal);
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
			ply.sendMessage(MinigameUtils.getLang("time.startup.timerResumed"), MessageType.Normal);
		}
	}
	
	public boolean isPaused(){
		return paused;
	}
}
