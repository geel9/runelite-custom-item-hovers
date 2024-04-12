package com.geel.customitemhovers;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A parsed entry in a TableFile.
 *
 * Contains variables that are "attached" to items
 */
public class ItemTable extends HashMap<String, String> {
    @Getter
    @Setter
    private String itemName;

    @Getter
    @Setter
    private ItemTableFile parentFile;

    public String[] getAllNames() {
        ArrayList<String> ret = new ArrayList<>();
        ret.add(this.itemName);

        // Handle $also
        String also = this.getOrDefault("$also", null);
        if (also != null) {
            ret.addAll(Arrays.asList(also.split("\\|")));
        }

        return ret.toArray(new String[0]);
    }
}
