package com.geel.customitemhovers.models.hovers;

import com.geel.customitemhovers.expressions.models.BaseExpression;
import com.geel.customitemhovers.expressions.models.CompiledExpression;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A parsed Hover from a HoverFile.
 *
 * One ParsedHover is created for every 1D hover array in the 2D array of a HoverDef. That is, for every individual
 * hover box.
 *
 * This class exists as a caching mechanism for expressions; we want to parse (tokenize + shunting-yard) hover texts
 * as infrequently as possible (on load only).
 *
 * Therefore, `hoverText` contains a hoverbox text with all expressions removed.
 * `expressionLocations` is an ascending array of positions that expression results should be inserted,
 * and `expressions` is an array of expressions.
 *
 * To fully resolve `hoverText`, simply copy it, and then iterate through `expressionLocations` and `expressions`
 * simultaneously (their indexes align) -- evaluate each expression in turn, and insert the string result into
 * `hoverText` at the location indicated by `expressionLocations[i]`.
 */
@AllArgsConstructor
public class ParsedHover {
    /**
     * The text for this hover box, without any expressions.
     *
     * If any expressions were present in the original text, use `expressionLocations` and `expressions` to
     * build the final text.
     */
    @Getter
    private final String hoverText;

    @Getter
    private final Integer[] expressionLocations;

    @Getter
    private final CompiledExpression[] expressions;

    @Getter
    private final CompiledExpression condition;
}
