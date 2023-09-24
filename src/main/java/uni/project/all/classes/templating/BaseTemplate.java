package uni.project.all.classes.templating;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.io.IOException;
import java.io.StringReader;

abstract class BaseTemplate implements Template {
    public final String id;
    public final String type;

    public freemarker.template.Template template;

    public BaseTemplate(Document dbDoc) throws IOException {
        id = dbDoc.getString("_id");
        type = dbDoc.getString("type");
        template = new freemarker.template.Template(id, new StringReader(dbDoc.getString("raw")));
    }

    public String getRaw() {
        return template.toString();
    }

    public void setRaw(MongoDatabase mongoDb, String templateRaw) throws IOException {
        mongoDb.getCollection("Templates").updateOne(Filters.eq("_id", id), Updates.set("raw", templateRaw));
        template = new freemarker.template.Template(id, new StringReader(templateRaw));
    }
}
