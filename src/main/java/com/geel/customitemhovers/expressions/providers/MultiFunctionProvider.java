package com.geel.customitemhovers.expressions.providers;

import com.geel.customitemhovers.expressions.functions.Function;
import com.geel.customitemhovers.expressions.models.Token;
import com.geel.customitemhovers.expressions.models.Variable;

import java.util.ArrayList;

public class MultiFunctionProvider implements IFunctionProvider {
    protected final ArrayList<IFunctionProvider> providers = new ArrayList<>();

    public MultiFunctionProvider() {

    }

    public MultiFunctionProvider(IFunctionProvider provider1, IFunctionProvider provider2) {
        this.Add(provider1).Add(provider2);
    }

    public MultiFunctionProvider Add(IFunctionProvider provider) {
        if (provider != null) {
            this.providers.add(provider);
        }
        return this;
    }

    @Override
    public Function findFunction(String variableName, Token[] arguments) {
        for (IFunctionProvider provider : providers) {
            Function resolved = provider.findFunction(variableName, arguments);
            if (resolved != null) {
                return resolved;
            }
        }

        return null;
    }
}
