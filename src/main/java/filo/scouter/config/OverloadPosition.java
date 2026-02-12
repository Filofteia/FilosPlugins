package filo.scouter.config;

import lombok.Getter;

@Getter
public enum OverloadPosition
{
	ANY_ROOM("Any Rooms"),
	FIRST_COMBAT("First Combat"); // that was scuffed, and didn't work.

	private final String roomType;

	OverloadPosition(String roomType)
	{
		this.roomType = roomType;
	}
}
