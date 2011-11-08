package org.intermine.webservice.server.widget;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

public class EnrichmentXMLProcessor implements WidgetResultProcessor {

    private static final WidgetResultProcessor instance = new  EnrichmentXMLProcessor();

    private EnrichmentXMLProcessor() {
        // Not to be instantiated.
    }

    public static WidgetResultProcessor instance() {
        return instance;
    }

    @Override
    public List<String> formatRow(List<Object> row) {
        StringBuffer sb = new StringBuffer("<result>");
        sb.append(formatCell("item", row.get(0)));
        sb.append(formatCell("description", row.get(1)));
        sb.append(formatCell("pValue", row.get(2)));
        sb.append(formatCell("count", row.get(3)));
        sb.append(formatCell("match", row.get(4)));
        sb.append("</result>");
        return new LinkedList<String>(Arrays.asList(sb.toString()));
    }

    private String formatCell(String name, Object contents) {
        StringBuffer sb = new StringBuffer();
        if (contents instanceof List) {
            for (Object o: (List) contents) {
                sb.append(formatCell(name, o));
            }
        } else {
            sb.append("<" + name + ">");
            sb.append(StringEscapeUtils.escapeXml(contents.toString()));
            sb.append("</" + name + ">");
        }
        return sb.toString();
    }

}
