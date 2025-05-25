package com.geel.customitemhovers.namespaces;

import com.geel.customitemhovers.providers.GlobalFunctionProvider;
import com.geel.customitemhovers.providers.RootFunctionProvider;
import com.geel.customitemhovers.providers.RootVariableProvider;
import com.geel.customitemhovers.expressions.providers.SimpleNonObjectNamespace;

public class RootNamespace extends SimpleNonObjectNamespace {
    public RootNamespace(RootFunctionProvider functionProvider, RootVariableProvider variableProvider) {
        super(functionProvider, variableProvider);
    }
}
