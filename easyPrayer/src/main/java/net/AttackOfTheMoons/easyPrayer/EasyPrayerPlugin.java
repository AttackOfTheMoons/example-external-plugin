package net.AttackOfTheMoons.easyPrayer;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import org.pf4j.Extension;

@SuppressWarnings("Duplicates")
@Extension
@PluginDescriptor(
	name = "One Tick Prayer",
	description = "Enables you to spamclick prayers to one tick flick them.",
	tags = {},
	enabledByDefault = false
)
public class EasyPrayerPlugin extends Plugin
{

	@Inject
	private Client client;

	private boolean shouldDeactivate = true;

	@Subscribe
	public void onGameTick(GameTick event)
	{
		shouldDeactivate = true;
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{

		final String option = Text.removeTags(event.getMenuOption()).toLowerCase();

		if (option.equals("activate"))
		{
			shouldDeactivate = false;
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		final String option = Text.removeTags(event.getOption()).toLowerCase();

		MenuEntry[] temp = new MenuEntry[1];
		temp[0] = setMenuEntry("Cancel", "", 0, 0, 0, 0);

		if (option.contains("deactivate") && !shouldDeactivate)
		{
			client.setMenuEntries(temp);
		}
	}

	private MenuEntry setMenuEntry(String option, String target, int ID, int type, int param0, int param1)
	{
		MenuEntry e = new MenuEntry();
		e.setOption(option);
		e.setTarget(target);
		e.setIdentifier(ID);
		e.setType(type);
		e.setParam0(param0);
		e.setParam1(param1);
		return e;
	}
}