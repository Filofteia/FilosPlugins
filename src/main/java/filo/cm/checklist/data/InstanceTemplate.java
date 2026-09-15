/*
 * Copyright (c) 2018, Kamiel
 * Copyright (c) 2025, Filofteia <https://github.com/Filofteia>
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
package filo.cm.checklist.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Copied from Core: net.runelite.api - InstanceTemplates.java by Kamiel, then modified to CMs
 */
@AllArgsConstructor
public enum InstanceTemplate
{

	// F3
	RAIDS_ICE_DEMON_F3(3296, 5344, 0, 32, 32, 3, "Ice Demon"),
	RAIDS_FARMING_F3(3328, 5440, 1, 32, 32, 3, "Farming (F3)"),
	RAIDS_END_F3(3264, 5152, 0, 32, 32, 3, "End (F3)"),

	// F2
	RAIDS_THIEVING_F2(3296, 5376, 0, 32, 32, 2, "Thieving"),
	RAIDS_FARMING_F2(3296, 5440, 0, 32, 32, 2, "Farming (F2)"),
	RAIDS_END_F2(3264, 5120, 0, 32, 32, 2, "End (F2)"),

	// F1
	RAIDS_FARMING_F1(3296, 5440, 1, 32, 32, 1, "Farming (F1)"),
	RAIDS_END_F1(3296, 5152, 0, 32, 32, 1, "End (F1)"),

	// F0
	RAIDS_OLM_F0(3224, 5712, 0, 16, 16, 0, "Olm");

	@Getter	private final int baseX;
	@Getter	private final int baseY;
	@Getter	private final int plane;
	@Getter	private final int width;
	@Getter	private final int height;
	@Getter	private final int playerFloor;
	@Getter	private final String roomName;

	public static InstanceTemplate findMatch(int chunkData, int playerFloor)
	{
		int y = (chunkData >> 3 & 0x7FF) * 8;
		int x = (chunkData >> 14 & 0x3FF) * 8;
		int plane = chunkData >> 24 & 0x3;

		for (InstanceTemplate template : InstanceTemplate.values())
		{
			if (plane == template.getPlane()
				&& x >= template.getBaseX() && x < template.getBaseX() + template.getWidth()
				&& y >= template.getBaseY() && y < template.getBaseY() + template.getHeight()
				&& (playerFloor == template.playerFloor || playerFloor == -1))
			{
				return template;
			}
		}

		return null;
	}
}
