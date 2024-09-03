package filo.scouter.config;

import lombok.Getter;

@Getter
public enum OverloadPosition
{
	ANY_ROOM("Any Rooms"),
	COMBAT_FIRST("First Combat") // This is weird, but it doesn't reset user settings.
	{
		@Override
		public String toString()
		{
			return "Combat First";
		};
	};

	private final String roomType;

	OverloadPosition(String roomType)
	{
		this.roomType = roomType;
	}
}
