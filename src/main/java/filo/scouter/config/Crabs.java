package filo.scouter.config;

public enum Crabs
{
	ANY("Any"),
	RARE("Rare & Good"),
	GOOD("Good");

	private final String crabType;
	Crabs(String crabType)
	{
		this.crabType = crabType;
	}
}
