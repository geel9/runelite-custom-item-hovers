package com.geel.customitemhovers.expressions.providers;

import com.geel.customitemhovers.expressions.models.Variable;

public interface IVariableProvider {
    Variable resolveVariable(String name);
}
