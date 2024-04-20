package com.geel.customitemhovers.expressions.providers;

import com.geel.customitemhovers.expressions.functions.Function;
import com.geel.customitemhovers.expressions.models.Token;

public interface IFunctionProvider {
    Function findFunction(String name, Token[] arguments);
}
