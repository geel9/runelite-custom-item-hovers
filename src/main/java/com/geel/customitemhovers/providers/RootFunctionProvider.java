package com.geel.customitemhovers.providers;

import com.geel.customitemhovers.expressions.providers.MultiFunctionProvider;

public class RootFunctionProvider extends MultiFunctionProvider {

    public RootFunctionProvider(GlobalFunctionProvider globalFunctionProvider, HoverFileFunctionProvider hoverFileFunctionProvider) {
        super(hoverFileFunctionProvider, globalFunctionProvider); // We want to register the hoverfile-specific provider first so it overrides the global scope
    }

    public void setHoverFileFunctionProvider(HoverFileFunctionProvider hoverFileFunctionProvider) {
        // TODO: this is all very hacky

        if (hoverFileFunctionProvider == null) {
            if (providers.size() == 2) {
                this.providers.remove(1);
            }

            return;
        }

        if (providers.size() == 1) {
            this.Add(hoverFileFunctionProvider);
        } else {
            this.providers.set(1, hoverFileFunctionProvider);
        }
    }
}
