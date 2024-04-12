package com.geel.customitemhovers;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class ItemTableFile {
    @SerializedName("is_table_file")
    public String IsTableFile;

    @SerializedName("name")
    public String Name;

    @SerializedName("items")
    public Map<String, ItemTable> Items;
}