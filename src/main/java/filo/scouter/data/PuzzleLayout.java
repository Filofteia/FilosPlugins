package filo.scouter.data;

public enum PuzzleLayout
{
	// 3C2P
	FSCCSPCPSF("A", "C"),	//1
	SCPFCCSPSF("A", "C"),	//2
	SFCCSPCPSF("A", "C"),	//3
	SPSFPCCCSF("A", "C"),	//4
	SCSPFCCSPF("C", "C"),	//5

	// 4C1P
	SCPFCCCSSF("C"),	//1
	SCCFCPSCSF("A"),	//2
	SCFCPCSCFS("C"),	//3

	// 4C2P
	SCFPCCSPCF("C", "B"), 	//1
	SCFPCSCPCF("A", "B"),	//2
	SFCCPCSCPF("A", "B"),	//3
	SCFCPCCSPF("A", "C"),	//4
	SPCFCCSPCF("C", "C"),	//5
	FSCCPPCSCF("A", "C"),	//6
	SCFPCPCCSF("A", "B"),	//7
	FSCPCCSCPF("A", "B"),	//8
	SCFCPCSCPF("A", "A"),	//9
	SCPFCCSPCF("B", "C"),	//10
	SPCFCSCCPF("A", "C"),	//11
	SCPFCCCPSF("B", "B"),	//12
	FSPCCPSCCF("A", "A"), 	//13
	SCPFCPCSCF("B", "A"),	//14
	SCCFPCCSPF("A", "A");	//15

	private final String[] puzzleType = new String[2];

	PuzzleLayout(String... puzzleTypes)
	{
		for (int i = 0; i < puzzleTypes.length; i++)
		{
			puzzleType[i] = puzzleTypes[i];
		}
	}

	public static PuzzleLayout getByLayout(String raidLayout)
	{
		for (PuzzleLayout layout : PuzzleLayout.values())
		{
			if (layout.name().equalsIgnoreCase(raidLayout))
				return layout;
		}

		return null;	// Only if I don't support the layout (5c2p / 5c3p)
	}

	public String getPuzzleType(int puzzleIndex)
	{
		return puzzleIndex > puzzleType.length || puzzleIndex == -1 ? "N/a" : puzzleType[puzzleIndex];
	}
}
