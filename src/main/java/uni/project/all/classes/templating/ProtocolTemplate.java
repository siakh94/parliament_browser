package uni.project.all.classes.templating;

import freemarker.template.TemplateException;
import org.bson.Document;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProtocolTemplate extends BaseTemplate {
    public ProtocolTemplate(Document dbDoc) throws IOException {
        super(dbDoc);
    }

    public String toTeX(HashMap<String, Object> mapping, AgendaItemTemplate agendaItemTemplate, SpeechTemplate speechTemplate) throws TemplateException, IOException {
        StringWriter writer = new StringWriter();
        List<String> agendaItemList = new ArrayList<>();
        ((List<HashMap<String, Object>>) mapping.get("agendaItems")).forEach((agendaItem) -> {
            try {
                agendaItemList.add(agendaItemTemplate.toTeX(agendaItem, speechTemplate));
            } catch (Exception ignored) { }
        });
        mapping.put("agendaItems", agendaItemList);
        mapping.put("lb", "\n");
        template.process(mapping, writer);
        return writer.toString();
    }
}
