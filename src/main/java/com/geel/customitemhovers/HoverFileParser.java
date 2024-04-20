package com.geel.customitemhovers;

import com.geel.customitemhovers.expressions.Compiler;
import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.StaticEvaluator;
import com.geel.customitemhovers.expressions.Tokenizer;
import com.geel.customitemhovers.expressions.models.CompiledExpression;
import com.geel.customitemhovers.expressions.providers.IFunctionProvider;
import com.geel.customitemhovers.models.hovers.HoverDef;
import com.geel.customitemhovers.models.hovers.HoverFile;
import com.geel.customitemhovers.models.hovers.ParsedHover;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

@Slf4j
public class HoverFileParser {
    @Inject
    private Gson gson;

    @Inject
    private CustomItemHoversFunctionProvider functionProvider;

    public ArrayList<HoverFile> readHoverFiles(Path dirPath) {
        ArrayList<HoverFile> ret = new ArrayList<>();

        if (!Files.isDirectory(dirPath) || !Files.isReadable(dirPath)) {
            return ret;
        }

        try {
            Stream fileStream = Files.list(dirPath);
            for (Iterator it = fileStream.iterator(); it.hasNext(); ) {
                Path p = (Path) it.next();

                //Ensure it's a regular readable file
                if (!Files.isRegularFile(p) || !Files.isReadable(p))
                    continue;

                //Ensure it's a json file
                if (!p.toString().endsWith(".json"))
                    continue;

                HoverFile file = parseHoverFile(p);

                //Must absolutely be a hover file
                if (file == null || file.IsHoverMap == null || !file.IsHoverMap.equals("absolutely"))
                    continue;

                //Post-process (right now just combine arrays of text into single strings)
                postProcessHoverFile(file);

                ret.add(file);
            }
        } catch (IOException e) {
            log.error(e.toString());

            return ret;
        }

        return ret;
    }

    private HoverFile parseHoverFile(Path hoverFile) {
        try {
            byte[] fileBytes = Files.readAllBytes(hoverFile);
            String fileString = new String(fileBytes);

            return gson.fromJson(fileString, HoverFile.class);
        } catch (Exception e) {
            log.error(e.toString());
            return null;
        }
    }

    private void postProcessHoverFile(HoverFile f) {
        for (HoverDef d : f.Hovers) {
            parseHoverDefHovers(d);
            parseHoverDefCondition(d);
        }
    }

    private final StringBuilder _hoverBuilder = new StringBuilder();

    private void parseHoverDefHovers(HoverDef d) {
        d.ParsedHoverTexts = new ParsedHover[d.HoverTexts.length];

        int i = 0;
        for (String[] hovers : d.HoverTexts) {
            CompiledExpression hoverBlockCondition = null; // Condition for whether this hoverblock should render
            int startPoint = 0; // Which entry in `hovers` to start reading from -- the first could be a condition
            _hoverBuilder.setLength(0); //clear stringbuilder but keep memory allocated

            // First, detect if the first entry indicates a condition.
            if (hovers.length > 0 && hovers[0].startsWith("?${") && hovers[0].endsWith("}")) {
                //Try to parse the condition
                hoverBlockCondition = compileSingleExpression(hovers[0].substring(1));

                // If the condition parses and is valid, ignore the first entry when building the hover.
                if (hoverBlockCondition != null) {
                    startPoint++;
                }
            }

            // Collapse each sub-array into a <br/>-delimited single String
            for (int j = startPoint; j < hovers.length; j++) {
                if (j != startPoint)
                    _hoverBuilder.append("</br>");
                _hoverBuilder.append(hovers[j]);
            }

            String concatenatedHovers = _hoverBuilder.toString();
            _hoverBuilder.setLength(0);

            // Now, find all expressions in the hoverText
            ArrayList<CompiledExpression> expressions = new ArrayList<>();
            ArrayList<Integer> expressionLocations = new ArrayList<>();

            int offset = 0;
            int curPos = 0;
            while (curPos < concatenatedHovers.length()) {
                char c = concatenatedHovers.charAt(curPos);

                // If not a tag opener, or the end of the string, just add to output
                if ((c != '$' && c != '<') || curPos == concatenatedHovers.length() - 1) {
                    _hoverBuilder.append(c);
                    curPos++;
                    continue;
                }

                // Ensure this is a tag opener
                char nextChar = concatenatedHovers.charAt(curPos + 1);
                if ((c == '$' && nextChar != '{') || (c == '<' && nextChar != '%')) {
                    // Not a tag opener. Just continue.
                    _hoverBuilder.append(c);
                    _hoverBuilder.append(nextChar);
                    curPos += 2;
                    continue;
                }

                // We have a tag opener. Try to read the expression.
                Tokenizer tokenizer = new Tokenizer(concatenatedHovers, curPos);
                CompiledExpression compiledExpression = null;
                try {
                    compiledExpression = Compiler.Compile(tokenizer.tokenize());
                    compiledExpression = StaticEvaluator.Evaluate(compiledExpression, new ExecutionContext(functionProvider, null));
                } catch (IllegalArgumentException e) {
                }

                int len = tokenizer.getNumRead();
                int endPos = curPos + len;

                // If a failed parse, just add the raw expression text
                if (compiledExpression == null) {
                    String expression = concatenatedHovers.substring(curPos, endPos);
                    curPos = endPos;
                    _hoverBuilder.append(expression);
                    continue;
                }

                // If a successful parse, skip ahead to endPos
                expressionLocations.add(curPos - offset);
                expressions.add(compiledExpression);
                curPos = endPos;
                offset += len;
            }

            d.ParsedHoverTexts[i++] = new ParsedHover(
                    _hoverBuilder.toString(),
                    expressionLocations.toArray(new Integer[0]),
                    expressions.toArray(new CompiledExpression[0]),
                    hoverBlockCondition
            );
        }
    }

    private void parseHoverDefCondition(HoverDef hover) {
        if (hover.ConditionString == null || hover.ConditionString.length() == 0) {
            return;
        }

        if (!hover.ConditionString.startsWith("${") && !hover.ConditionString.startsWith("<%")) {
            hover.ConditionString = "${" + hover.ConditionString + "}";
        }

        // TODO: If static eval returns a constant value, we can resolve the condition here
        //      (either remove the condition if it should always be shown, or remove the hover if it should never be shown)
        hover.Condition = compileSingleExpression(hover.ConditionString);
    }

    private CompiledExpression compileSingleExpression(String expression) {
        try {
            Tokenizer tokenizer = new Tokenizer(expression, 0);
            CompiledExpression compiled = Compiler.Compile(tokenizer.tokenize());
            return StaticEvaluator.Evaluate(compiled, new ExecutionContext(functionProvider, null));
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse hover condition");
            return null;
        }
    }
}
