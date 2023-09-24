package uni.project.all.classes.templating;

import freemarker.template.TemplateException;
import org.bson.Document;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

public class SpeechTemplate extends BaseTemplate {
    public SpeechTemplate(Document dbDoc) throws IOException {
        super(dbDoc);
    }

    public String toTeX(HashMap<String, Object> mapping) throws TemplateException, IOException {
        StringWriter writer = new StringWriter();
        template.process(mapping, writer);
        return writer.toString();
    }
}
