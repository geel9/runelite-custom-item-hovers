package com.geel.customitemhovers.models.hovers;

import com.geel.customitemhovers.expressions.providers.IFunctionProvider;
import com.geel.customitemhovers.providers.HoverFileFunctionProvider;
import com.google.gson.annotations.SerializedName;

public class HoverFile {
    @SerializedName("is_hover_map")
    public String IsHoverMap;

    @SerializedName("hovers")
    public HoverDef[] Hovers;

    @SerializedName("functions")
    public RawHoverFunction[] RawUserDefinedFunctions;

    @SerializedName("parsed_functions")
    public ParsedHoverFunction[] ParsedUserDefinedFunctions;

    /**
     * Provides access to user-defined functions defined in this hover file.
     *
     * TODO: Jesus Christ, the amount of spaghetti code is getting bad -- this class holds a FunctionProvider which also
     *       holds this class?! Stop it. Get some help.
     *       ...
     *       But I just want to make UDFs work :(
     */
    public HoverFileFunctionProvider FunctionProvider;
}