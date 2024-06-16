package filo.scouter.config;

import lombok.Getter;

@Getter
public enum OverloadPosition
{
	ANY_ROOM("Any Rooms"),
	COMBAT_FIRST("First Combat");

	private final String roomType;

	OverloadPosition(String roomType)
	{
		this.roomType = roomType;
	}
}
