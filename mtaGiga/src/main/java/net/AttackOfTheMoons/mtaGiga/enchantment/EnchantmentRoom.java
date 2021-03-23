package net.AttackOfTheMoons.mtaGiga.enchantment;

/*
 * Copyright (c) 2018, Jasper Ketelaar <Jasper0781@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.AttackOfTheMoons.mtaGiga.MTAAdvancedConfig;
import net.AttackOfTheMoons.mtaGiga.MTARoom;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class EnchantmentRoom extends MTARoom
{
	private static final int MTA_ENCHANT_REGION = 13462;

	private final Client client;
	private final List<WorldPoint> dragonstones = new ArrayList<>();
	private EnchantmentItem best;
	private int lastSpell = -1;
	private int indexOfBest = -1;

	@Inject
	private EnchantmentRoom(MTAAdvancedConfig config, Client client)
	{
		super(config);
		this.client = client;
		best = null;
	}

	private void findAnyItem()
	{
		if (client == null || best == null)
		{
			indexOfBest = -1;
			return;
		}
		final Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget == null)
		{
			indexOfBest = -1;
			best = null;
			return;
		}
		final List<WidgetItem> itemList = (List<WidgetItem>) inventoryWidget.getWidgetItems();
		int lastIndex = -1;
		int lastId = -1;
		for (int i = itemList.size() - 1; i >= 0; i--)
		{
			final WidgetItem item = itemList.get(i);
			if (item.getId() == EnchantmentItem.DRAGONSTONE.getId())
			{
				indexOfBest = item.getIndex();
				best = EnchantmentItem.DRAGONSTONE;
				return;
			}
			else if (item.getId() == best.getId())
			{
				indexOfBest = item.getIndex();
				return;
			}
			else if (item.getId() == EnchantmentItem.CUBE.getId() || item.getId() == EnchantmentItem.CYLINDER.getId()
				|| item.getId() == EnchantmentItem.PENTAMID.getId() || item.getId() == EnchantmentItem.ICOSAHEDRON.getId())
			{
				lastId = item.getId();
				lastIndex = item.getIndex();
			}
		}
		best = EnchantmentItem.getByID(lastId);
		indexOfBest = lastIndex;
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!inside() || !config.advanced())
		{
			return;
		}
		if (indexOfBest == -1)
		{
			best = null;
			return;
		}
		if (event.getOption().equals("Cast") && event.getTarget().startsWith("<col=00ff00>Lvl-"))
		{
			MenuEntry e = event.clone();
			e.setOption("<col=00ff00>Cast</col>");
			e.setTarget(event.getTarget() + " -> <col=ff9040>" + best.getName() + "</col>");
			e.setOpcode(MenuAction.ITEM_USE_ON_WIDGET.getId());
			e.setActionParam1(WidgetInfo.INVENTORY.getId());
			lastSpell = event.getActionParam1();
			client.insertMenuItem(
				e.getOption(),
				e.getTarget(),
				e.getOpcode(),
				e.getIdentifier(),
				e.getParam0(),
				e.getParam1(),
				true
			);
		}
	}


	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!inside() || !config.advanced() || best == null)
		{
			return;
		}
		if (event.getMenuOption().equals("<col=00ff00>Cast</col>"))
		{
			MenuEntry old = new MenuEntry(
				event.getMenuOption(), event.getMenuTarget(), event.getId(), event.getMenuAction().getId(), event.getActionParam(), event.getWidgetId(), false
			);
			MenuEntry tmp = old.clone();
			tmp.setActionParam(indexOfBest);
			tmp.setId(best.getId());
			final String spellName = event.getMenuTarget().split(" ->")[0];
			tmp.setTarget(spellName + " -> <col=ff9040>" + best.getName() + "</col>");
			client.setSelectedSpellName(spellName);
			client.setSelectedSpellWidget(lastSpell);
			System.out.println("Changing clicked");
			event.setMenuEntry(tmp);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOADING)
		{
			dragonstones.clear();
			best = null;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!inside())
		{
			dragonstones.clear();
			best = null;
			return;
		}
		if (config.advanced())
		{
			final Widget cube = client.getWidget(195, 10);
			final Widget cylinder = client.getWidget(195, 12);
			final Widget pentamid = client.getWidget(195, 14);
			final Widget icosahedron = client.getWidget(195, 16);
			if (cube != null && !cube.isHidden())
			{
				best = EnchantmentItem.CUBE;
			}
			else if (cylinder != null && !cylinder.isHidden())
			{
				best = EnchantmentItem.CYLINDER;
			}
			else if (pentamid != null && !pentamid.isHidden())
			{
				best = EnchantmentItem.PENTAMID;
			}
			else if (icosahedron != null && !icosahedron.isHidden())
			{
				best = EnchantmentItem.ICOSAHEDRON;
			}
			else
			{
				best = null;
			}
			findAnyItem();
		}
		else
		{
			best = null;
		}
		if (!config.enchantment())
		{
			return;
		}
		WorldPoint nearest = findNearestStone();
		if (nearest != null)
		{
			client.setHintArrow(nearest);
		}
		else
		{
			client.clearHintArrow();
		}
	}

	private WorldPoint findNearestStone()
	{
		WorldPoint nearest = null;
		double dist = Double.MAX_VALUE;
		WorldPoint local = client.getLocalPlayer().getWorldLocation();
		for (WorldPoint worldPoint : dragonstones)
		{
			double currDist = local.distanceTo(worldPoint);
			if (nearest == null || currDist < dist)
			{
				dist = currDist;
				nearest = worldPoint;
			}
		}
		return nearest;
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		final TileItem item = itemSpawned.getItem();
		final Tile tile = itemSpawned.getTile();

		if (item.getId() == ItemID.DRAGONSTONE_6903)
		{
			WorldPoint location = tile.getWorldLocation();
			log.debug("Adding dragonstone at {}", location);
			dragonstones.add(location);
		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned)
	{
		final TileItem item = itemDespawned.getItem();
		final Tile tile = itemDespawned.getTile();

		if (item.getId() == ItemID.DRAGONSTONE_6903)
		{
			WorldPoint location = tile.getWorldLocation();
			log.debug("Removed dragonstone at {}", location);
			dragonstones.remove(location);
		}
	}

	@Override
	public boolean inside()
	{
		Player player = client.getLocalPlayer();
		return player != null && player.getWorldLocation().getRegionID() == MTA_ENCHANT_REGION
			&& player.getWorldLocation().getPlane() == 0;
	}
}
