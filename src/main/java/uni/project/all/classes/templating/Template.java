package uni.project.all.classes.templating;

import com.mongodb.client.MongoDatabase;

import java.io.IOException;
import java.util.Map;

public interface Template {
    String getRaw();
    void setRaw(MongoDatabase mongoDb, String templateRaw) throws IOException;
}
