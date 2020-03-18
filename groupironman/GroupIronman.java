package net.runelite.client.plugins.groupironman;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@PluginDescriptor(name = "Group Ironman", description = "A plugin that only allows trading from people in your group")
public class GroupIronman extends Plugin
{
	List<String> teamMembers = new ArrayList<>();

	@Inject
	private Client client;

	@Inject
	private GroupIronmanConfig config;

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("GroupIronman"))
		{
			return;
		}
		GetTeamMembers();
	}

	@Override
	protected void startUp() throws Exception
	{
		GetTeamMembers();
	}

	@Override
	protected void shutDown() throws Exception
	{
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
	}




	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
	{
		if(menuEntryAdded != null)
		{
			try
			{
				List<Integer> invalidOptions = new ArrayList<>();
				String option = menuEntryAdded.getOption();

				String targetName = RemoveHtmlFromTargetName(menuEntryAdded.getTarget());

				if(option.equals("Trade with"))
				{
					if (!PlayerInGroup(targetName))
					{
						invalidOptions.add(menuEntryAdded.getIdentifier());
					}
				}
				else if(option.equals("Exchange ") || option.equals("Exchange") || targetName.contains("Grand Exchange Clerk"))
				{
					invalidOptions.add(menuEntryAdded.getIdentifier());
				}

				SetMenuOptions(invalidOptions);


			}
			catch (Exception e)
			{
				log.error("Exception at MenuEntry: " + e.getMessage());
			}
		}
		else
		{
			log.info("Menu was null");
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if(chatMessage.getType() == ChatMessageType.TRADEREQ)
		{
			if(!PlayerInGroup(chatMessage.getName()))
			{
				client.getChatLineMap().get(chatMessage.getType().getType()).removeMessageNode(chatMessage.getMessageNode());
				client.addChatMessage(ChatMessageType.CONSOLE, "", "A Player outside of your group tried to trade you.", "");
				client.refreshChat();
			}
		}
	}

	@Provides
	GroupIronmanConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GroupIronmanConfig.class);
	}


	private void GetTeamMembers()
	{
		try
		{
			teamMembers.clear();
			String input = config.members();
			teamMembers.addAll(Arrays.asList( input.split(",")));
		}
		catch (Exception e)
		{
			log.error("Could not get team members: " + e.getMessage());
		}
	}

	private boolean PlayerInGroup(String name)
	{
		for(String s : teamMembers)
		{
			if(s.compareToIgnoreCase(name) == 0)
			{
				return true;
			}
		}
		return false;
	}

	private String RemoveHtmlFromTargetName(String targetName)
	{
		int idx = targetName.indexOf('>');


		int endOfName =  targetName.indexOf("<", idx);

		if (idx != -1)
		{
			if(endOfName != -1) {
				targetName = targetName.substring(idx + 1, endOfName);
			}
			else
			{
				targetName = targetName.substring(idx + 1);
			}
		}
		return targetName;
	}

	private void SetMenuOptions(List<Integer> invalidOptions)
	{
		MenuEntry[] allMenuEntries = client.getMenuEntries();
		List<MenuEntry> validMenuEntries = new ArrayList<>();

		for (MenuEntry allMenuEntry : allMenuEntries)
		{
			boolean optionValid = true;

			for (Integer invalidOption : invalidOptions)
			{
				if (allMenuEntry.getIdentifier() == invalidOption)
				{
					optionValid = false;
					break;
				}
			}
			if (optionValid)
			{
				validMenuEntries.add(allMenuEntry);
			}
		}

		client.setMenuEntries(validMenuEntries.toArray(new MenuEntry[0]));
	}

}