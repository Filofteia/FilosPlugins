package filo.friendlist.tabs.config;

import lombok.Getter;

@Getter
public enum TextAlignmentConfig
{
    LEFT(0),
    CENTER(1),
    RIGHT(2);

    private final int alignmentId;

    TextAlignmentConfig(int alignmentId) {
        this.alignmentId = alignmentId;
    }
}
