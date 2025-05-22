package com.geel.customitemhovers.expressions.providers;

import com.geel.customitemhovers.expressions.functions.Function;
import com.geel.customitemhovers.expressions.models.Token;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;

public class BaseFunctionProvider implements IFunctionProvider {
    // A map(functionName -> map(numArguments -> listOfFunctions))
    @Getter
    private HashMap<String, HashMap<Integer, ArrayList<Function>>> functions;

    public BaseFunctionProvider() {
        functions = new HashMap<>();
    }

    public BaseFunctionProvider add(Function f) {
        String name = f.getName();

        // Ensure that the overarching map has an entry for the function name
        if (!functions.containsKey(name)) {
            functions.put(name, new HashMap<>());
        }

        // Get the entry for the function name, and ensure the sub-map has an entry for the number of arguments
        HashMap<Integer, ArrayList<Function>> argumentsMap = functions.get(name);
        int argsHashCode = f.getArgumentsHashCode();
        if (!argumentsMap.containsKey(argsHashCode)) {
            argumentsMap.put(argsHashCode, new ArrayList<>(1));
        }

        // Get the entry for the number of arguments from the sub-map, and insert the function into the list
        ArrayList<Function> funcList = argumentsMap.get(argsHashCode);
        if (funcList.contains(f)) {
            throw new IllegalArgumentException("Function already exists");
        }

        funcList.add(f);
        return this;
    }

    @Override
    public Function findFunction(String name, Token[] arguments) {
        int numArgs = arguments.length;

        // Find the function by name. The result will be a map(numArgs -> list_of_functions)
        HashMap<Integer, ArrayList<Function>> numArgsMap = functions.getOrDefault(name, null);

        // If no functions found, obviously return null
        if (numArgsMap == null) {
            return null;
        }

        // Look for it in the fixed functions, then try variadic functions
        ArrayList<Function> fixedFunctions = numArgsMap.getOrDefault(numArgs, null);
        ArrayList<Function> variadicFunctions = numArgsMap.getOrDefault(-1, null);

        Function fixedFunction = searchFuncs(fixedFunctions, arguments);
        if (fixedFunction != null)
            return fixedFunction;

        return searchFuncs(variadicFunctions, arguments);
    }

    private Function searchFuncs(ArrayList<Function> functions, Token[] arguments) {
        if (functions == null)
            return null;

        for (Function func : functions) {
            if (func.argumentsMatch(arguments)) {
                return func;
            }
        }

        return null;
    }
}
