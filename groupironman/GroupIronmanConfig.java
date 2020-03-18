package net.runelite.client.plugins.groupironman;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("GroupIronman")
public interface GroupIronmanConfig extends Config
{
	@ConfigItem(
		keyName = "members",
		name = "Team Members",
		description = "The List of team member separated by comma"
	)
	default String members()
	{
		return "";
	}
}