package com.pauldavdesign.mineauz.minigames.minigame.regions.actions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import com.pauldavdesign.mineauz.minigames.MinigamePlayer;
import com.pauldavdesign.mineauz.minigames.menu.Callback;
import com.pauldavdesign.mineauz.minigames.menu.Menu;
import com.pauldavdesign.mineauz.minigames.menu.MenuItemInteger;
import com.pauldavdesign.mineauz.minigames.menu.MenuItemPage;
import com.pauldavdesign.mineauz.minigames.minigame.nodes.Node;
import com.pauldavdesign.mineauz.minigames.minigame.regions.Region;

public class HealAction implements ActionInterface{

	@Override
	public String getName() {
		return "HEAL";
	}

	@Override
	public boolean useInRegions() {
		return true;
	}

	@Override
	public boolean useInNodes() {
		return false;
	}

	@Override
	public void executeNodeAction(MinigamePlayer player,
			Map<String, Object> args, Node node) {
		
	}

	@Override
	public void executeRegionAction(MinigamePlayer player, Map<String, Object> args, Region region) {
		if((Integer)args.get("a_healamount") > 0){
			if(player.getPlayer().getHealth() != 20){
				double health = (Integer)args.get("a_healamount") + player.getPlayer().getHealth();
				if(health > 20)
					health = 20;
				player.getPlayer().setHealth(health);
			}
		}
		else
			player.getPlayer().damage((Integer)args.get("a_healamount") * -1);
	}

	@Override
	public Map<String, Object> getRequiredArguments() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("a_healamount", 1);
		return args;
	}

	@Override
	public void saveArguments(Map<String, Object> args,
			FileConfiguration config, String path) {
		config.set(path + ".a_healamount", args.get("a_healamount"));
	}

	@Override
	public Map<String, Object> loadArguments(FileConfiguration config,
			String path) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("a_healamount", config.getInt(path + ".a_healamount"));
		return args;
	}

	@Override
	public boolean displayMenu(MinigamePlayer player, Map<String, Object> args,
			Menu previous) {
		Menu m = new Menu(3, "Heal", player);
		m.addItem(new MenuItemPage("Back", Material.REDSTONE_TORCH_ON, previous), m.getSize() - 9);
		final Map<String, Object> fargs = args;
		m.addItem(new MenuItemInteger("Heal Amount", Material.GOLDEN_APPLE, new Callback<Integer>() {
			
			@Override
			public void setValue(Integer value) {
				fargs.put("a_healamount", value);
			}
			
			@Override
			public Integer getValue() {
				return (Integer)fargs.get("a_healamount");
			}
		}, null, null));
		m.displayMenu(player);
		return true;
	}

}