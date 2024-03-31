package com.geel.customitemhovers;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

@Slf4j
public class HoverFileParser {
    @Inject
    private Gson gson;

    public ArrayList<HoverFile> readHoverFiles(Path dirPath) {
        ArrayList<HoverFile> ret = new ArrayList<>();

        if(!Files.isDirectory(dirPath) || !Files.isReadable(dirPath)){
            return ret;
        }

        try {
            Stream fileStream = Files.list(dirPath);
            for (Iterator it = fileStream.iterator(); it.hasNext(); ) {
                Path p = (Path) it.next();

                //Ensure it's a regular readable file
                if(!Files.isRegularFile(p) || !Files.isReadable(p))
                    continue;

                //Ensure it's a json file
                if(!p.toString().endsWith(".json"))
                    continue;

                HoverFile file = parseHoverFile(p);

                //Must absolutely be a hover file
                if(file == null || file.IsHoverMap == null || !file.IsHoverMap.equals("absolutely"))
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

    private HoverFile parseHoverFile(Path hoverFile){
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
        for(HoverDef d : f.Hovers) {
            parseHoverDefHovers(d);
        }
    }

    private StringBuilder _hoverBuilder = new StringBuilder();
    private void parseHoverDefHovers(HoverDef d) {
        d.ParsedHoverTexts = new String[d.HoverTexts.length];

        int i = 0;
        for(String[] hovers : d.HoverTexts) {
            _hoverBuilder.setLength(0); //clear stringbuilder but keep memory allocated

            boolean firstLine = true;
            for(String hoverLine : hovers) {
                if(!firstLine)
                    _hoverBuilder.append("</br>");
                _hoverBuilder.append(hoverLine);

                firstLine = false;
            }

            d.ParsedHoverTexts[i++] = _hoverBuilder.toString();
        }
    }
}
