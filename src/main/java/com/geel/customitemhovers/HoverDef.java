package com.geel.customitemhovers;

import com.google.gson.annotations.SerializedName;

public class HoverDef {
    @SerializedName("ids")
    public int[] ItemIDs;

    @SerializedName("items")
    public String[] ItemNames;

    @SerializedName("items_regex")
    public String[] ItemNamesRegex;

    @SerializedName("hovers")
    public String[][] HoverTexts;

    public String[] ParsedHoverTexts;
}
