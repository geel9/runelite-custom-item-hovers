package com.geel.customitemhovers;

import com.geel.customitemhovers.expressions.models.BaseExpression;
import com.geel.customitemhovers.expressions.Compiler;
import com.geel.customitemhovers.expressions.Tokenizer;
import com.geel.customitemhovers.expressions.models.CompiledExpression;
import com.geel.customitemhovers.expressions.models.TokenizedExpression;
import com.geel.customitemhovers.models.hovers.HoverFile;
import com.geel.customitemhovers.models.hovers.HoverDef;
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
        }
    }

    private final StringBuilder _hoverBuilder = new StringBuilder();

    private void parseHoverDefHovers(HoverDef d) {
        d.ParsedHoverTexts = new ParsedHover[d.HoverTexts.length];

        int i = 0;
        for (String[] hovers : d.HoverTexts) {
            _hoverBuilder.setLength(0); //clear stringbuilder but keep memory allocated

            // First, collapse each sub-array into a <br/>-delimited single String
            boolean firstLine = true;
            for (String hoverLine : hovers) {
                if (!firstLine)
                    _hoverBuilder.append("</br>");
                _hoverBuilder.append(hoverLine);

                firstLine = false;
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

                // We have a tag opener. Try to read the expression.
                Tokenizer tokenizer = new Tokenizer(concatenatedHovers, curPos);
                CompiledExpression compiledExpression = null;
                try {
                    compiledExpression = Compiler.Compile(tokenizer.tokenize());
                } catch (IllegalArgumentException e) {
                }

                int len = tokenizer.getNumRead();
                int endPos = curPos + len;

                // If a failed parse, just add the raw expression text
                if(compiledExpression == null) {
                    _hoverBuilder.append(concatenatedHovers.substring(curPos, endPos));
                    curPos = endPos;
                    continue;
                }

                // If a successful parse, skip ahead to endPos
                expressionLocations.add(curPos - offset);
                expressions.add(compiledExpression);
                curPos = endPos;
                offset += len;
            }

            d.ParsedHoverTexts[i++] = new ParsedHover(_hoverBuilder.toString(),
                    expressionLocations.toArray(new Integer[0]),
                    expressions.toArray(new CompiledExpression[0]));
        }
    }
}
