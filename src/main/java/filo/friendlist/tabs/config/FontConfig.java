package filo.friendlist.tabs.config;

import lombok.Getter;

public enum FontConfig {
    PLAIN_11(494, 13),
    PLAIN_12(495, 17),
    
    BOLD_12(496, 17),

    QUILL_8(497, 19),

    SUROK(819, 19),

    VERDANA_11(1442, 16),
    VERDANA_11_BOLD(1443, 16),

    TAHOMA_11(1444, 15),

    VERDANA_13(1445, 17),
    VERDANA_13_BOLD(1446, 17),

    VERDANA_15(1447, 19);

    @Getter
    final int fontId;
    @Getter
    final int fontSpacing;
    @Getter
    final int headerOffset;

    FontConfig(int fontId, int fontSpacing) {
        this.fontId = fontId;
        this.fontSpacing = fontSpacing;
        this.headerOffset = 4;
    }

}
