package filo.friendlist.tabs.data;

import lombok.Data;

@Data
public class FriendTab {
    boolean isExpanded = true;

    int iconId = -1; // Future Update

    int categoryColor;
    int backdropColor;

    public void toggleExpanded()
    {
        isExpanded = !isExpanded;
    }

    public FriendTab(int categoryColor, int backdropColor) {
        this.categoryColor = categoryColor;
        this.backdropColor = backdropColor;
    }
}
