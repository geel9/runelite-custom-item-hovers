package com.geel.customitemhovers.providers;

import com.geel.customitemhovers.expressions.providers.BaseFunctionProvider;
import com.geel.customitemhovers.models.hovers.HoverFile;
import com.geel.customitemhovers.models.hovers.ParsedHoverFunction;

public class HoverFileFunctionProvider extends BaseFunctionProvider {
    public HoverFileFunctionProvider(HoverFile hoverFile) {
        super();

        for(ParsedHoverFunction function : hoverFile.ParsedUserDefinedFunctions) {
            add(function.getTrueFunction());
        }
    }
}
