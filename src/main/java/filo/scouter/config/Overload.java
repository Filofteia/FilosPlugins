package filo.scouter.config;

import lombok.Getter;

@Getter
public enum Overload
{
	MUTTADILE("Muttadiles"),
	TEKTON("Tekton"),
	VANGUARD("Vanguards"),
	VESPULA("Vespula"),
	VASA("Vasa");

	private final String roomName;

	Overload(String roomName)
	{
		this.roomName = roomName;
	}
}
