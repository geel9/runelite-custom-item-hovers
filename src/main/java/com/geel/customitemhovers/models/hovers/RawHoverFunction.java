package com.geel.customitemhovers.models.hovers;

import com.google.gson.annotations.SerializedName;

public class RawHoverFunction {
    @SerializedName("name")
    public String name;

    @SerializedName("arguments")
    public String[] arguments;

    @SerializedName("expression")
    public String expression;
}
