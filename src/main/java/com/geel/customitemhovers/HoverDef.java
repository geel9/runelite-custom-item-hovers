package com.geel.customitemhovers;

import com.google.gson.annotations.SerializedName;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;

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

    @SerializedName("hovers")
    public String[][] HoverTexts;

    /**
     * An array of Hovers; each element in this array corresponds to an individual hover box that should be rendered.
     *
     * This is produced from `HoverTexts`, which is a 2D array of strings.
     * Each sub-array of strings in `HoverTexts` is treated as an array of lines which are concatenated together.
     */
    public String[] ParsedHoverTexts;

    /**
     * Creates and returns an array of hover text strings, after transformation for a specific item.
     *
     * Transformation involves replacing function calls with their results, and variables with their values.
     */
    public String[] GetTransformedTexts(Item item, ItemComposition composition) {
        String[] transformed = new String[ParsedHoverTexts.length];

        int i = 0;
        for(String text : ParsedHoverTexts) {
            transformed[i++] = HoverEvaluator.Evaluate(text, item, composition);
        }

        return transformed;
    }
}
