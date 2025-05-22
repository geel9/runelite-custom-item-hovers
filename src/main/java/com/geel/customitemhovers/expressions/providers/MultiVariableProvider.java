package com.geel.customitemhovers.expressions.providers;

import com.geel.customitemhovers.expressions.models.Variable;

import java.util.ArrayList;

public class MultiVariableProvider implements IVariableProvider{
    private final ArrayList<IVariableProvider> providers = new ArrayList<>();

    public MultiVariableProvider() {

    }

    public MultiVariableProvider(IVariableProvider provider1, IVariableProvider provider2) {
        this.Add(provider1).Add(provider2);
    }

    public MultiVariableProvider Add(IVariableProvider provider) {
        this.providers.add(provider);
        return this;
    }

    @Override
    public Variable resolveVariable(String variableName) {
        for(IVariableProvider provider : providers) {
            Variable resolved = provider.resolveVariable(variableName);
            if(resolved != null) {
                return resolved;
            }
        }

        return null;
    }
}
