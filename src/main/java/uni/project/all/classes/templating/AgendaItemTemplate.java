package uni.project.all.classes.templating;

import freemarker.template.TemplateException;
import org.bson.Document;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AgendaItemTemplate extends BaseTemplate {
    public AgendaItemTemplate(Document dbDoc) throws IOException {
        super(dbDoc);
    }

    public String toTeX(HashMap<String, Object> mapping, SpeechTemplate speechTemplate) throws TemplateException, IOException {
        StringWriter writer = new StringWriter();
        List<String> speechList = new ArrayList<>();
        ((List<HashMap<String, Object>>) mapping.get("speeches")).forEach((speech) -> {
            try {
                speechList.add(speechTemplate.toTeX(speech));
            } catch (Exception ignored) { }
        });
        mapping.put("speeches", speechList);
        mapping.put("lb", "\n");
        template.process(mapping, writer);
        return writer.toString();
    }
}
