package com.geel.customitemhovers.models.itemtables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A raw, JSON-deserialized entry in an ItemTableFile.
 *
 * Contains variables that are "attached" to items
 */
public class RawItemTableEntry extends HashMap<String, String> {
    public String[] getAllNames(String itemName) {
        ArrayList<String> ret = new ArrayList<>();
        ret.add(itemName);

        // Handle $also
        String also = this.getOrDefault("$also", null);
        if (also != null) {
            ret.addAll(Arrays.asList(also.split("\\|")));
        }

        return ret.toArray(new String[0]);
    }
}