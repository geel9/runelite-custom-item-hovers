package com.geel.customitemhovers.models.hovers;

import com.geel.customitemhovers.expressions.models.CompiledExpression;
import com.google.gson.annotations.SerializedName;

/**
 * A parsed entry in a hoverfile.
 */
public class HoverDef {
    @SerializedName("ids")
    public int[] ItemIDs;

    @SerializedName("items")
    public String[] ItemNames;

    @SerializedName("items_regex")
    public String[] ItemNamesRegex;

    @SerializedName("condition")
    public String ConditionString;

    @SerializedName("hovers")
    public String[][] HoverTexts;

    /**
     * The hover file that this entry belongs to.
     * This is used to make user-defined functions available to the hover.
     */
    public HoverFile OwningFile;

    /**
     * A condition expression, which controls whether or not this hover will display.
     */
    public CompiledExpression Condition;

    /**
     * An array of Hovers; each element in this array corresponds to an individual hover box that should be rendered.
     *
     * This is produced from `HoverTexts`, which is a 2D array of strings.
     * Each sub-array of strings in `HoverTexts` is treated as an array of lines which are concatenated together.
     */
    public ParsedHover[] ParsedHoverTexts;
}
