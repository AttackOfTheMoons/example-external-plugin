package net.AttackOfTheMoons.mtaGiga.enchantment;

import lombok.Getter;

public enum EnchantmentItem
{
	CUBE("Cube", 6899),
	CYLINDER("Cylinder", 6898),
	PENTAMID("Pentamid", 6901),
	ICOSAHEDRON("Icosahedron", 6900),
	DRAGONSTONE("Dragonstone", 6903);

	@Getter
	private final int id;
	@Getter
	private final String name;

	EnchantmentItem(String name, int id)
	{
		this.id = id;
		this.name = name;
	}

	static EnchantmentItem getByID(int id)
	{
		switch (id)
		{
			case 6899:
				return CUBE;
			case 6898:
				return CYLINDER;
			case 6901:
				return PENTAMID;
			case 6900:
				return ICOSAHEDRON;
			case 6903:
				return DRAGONSTONE;
			default:
				return null;
		}
	}
}
