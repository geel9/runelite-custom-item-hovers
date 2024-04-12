package com.geel.customitemhovers.models.hovers;

import com.geel.customitemhovers.models.hovers.HoverDef;
import com.google.gson.annotations.SerializedName;

public class HoverFile {
    @SerializedName("is_hover_map")
    public String IsHoverMap;

    @SerializedName("hovers")
    public HoverDef[] Hovers;
}