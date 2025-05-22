package com.geel.customitemhovers.namespaces;

import com.geel.customitemhovers.providers.RootFunctionProvider;
import com.geel.customitemhovers.providers.RootVariableProvider;
import com.geel.customitemhovers.expressions.providers.SimpleNamespace;

public class RootNamespace extends SimpleNamespace {
    public RootNamespace(RootFunctionProvider functionProvider, RootVariableProvider variableProvider) {
        super(functionProvider, variableProvider);
    }
}
