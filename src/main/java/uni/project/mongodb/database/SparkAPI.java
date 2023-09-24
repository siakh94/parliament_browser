package uni.project.mongodb.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import freemarker.template.TemplateException;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.hucompute.textimager.uima.type.Sentiment;
import org.json.JSONObject;
import org.json.JSONTokener;
import spark.Spark;
import uni.project.MainClass;
import freemarker.template.Configuration;
import uni.project.all.classes.templating.AgendaItemTemplate;
import uni.project.all.classes.templating.ProtocolTemplate;
import uni.project.all.classes.templating.SpeechTemplate;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.function.Function;
import java.util.stream.Collectors;

import static spark.Spark.get;
import static spark.Spark.put;
import static spark.Spark.post;
import static spark.Spark.delete;
import static spark.Spark.before;
import static uni.project.mongodb.database.MongoDBConnectionHandler.deserializeCasFromXml;

public class SparkAPI {
    private MongoDBConnectionHandler db;
    private Configuration config;

    static int sum(Object a, Object b) {
        return Integer.sum((int) a, (int) b);
    }

    public SparkAPI(MongoDBConnectionHandler db) throws IOException {
        this.db = db;
        config = new Configuration();
        config.setClassForTemplateLoading(MainClass.class, "templates");
        config.setDirectoryForTemplateLoading(new File("templates/"));
    }

    /**
     * Construct a speaker document using its ID.
     * @param speakerId Speaker's ID.
     * @return Speaker document.
     */
    public Document speakerFromId(String speakerId) {
        MongoDatabase mongoDb = this.db.getDb();
        MongoCursor<Document> speakerIterator = mongoDb.getCollection("Speakers").find(Filters.eq("_id", speakerId)).iterator();
        // Speaker is not present.
        if (!speakerIterator.hasNext()) return null;
        Document speaker = speakerIterator.next();
        Document speakerMap = new Document();
        speakerMap.put("first", speaker.getString("FirstName"));
        speakerMap.put("last", speaker.getString("LastName"));
        speakerMap.put("name", speaker.getString("FirstName") + speaker.getString("LastName"));
        speakerMap.put("id", speakerId);
        MongoCursor<Document> imageIterator = mongoDb.getCollection("Photos").find(Filters.eq("SpeakerID", speakerId)).iterator();
        speakerMap.put("image", imageIterator.hasNext() ? imageIterator.next().getString("PhotoLink") : null);
        if (speaker.containsKey("FractionID")) {
            Document fractionMap = new Document();
            fractionMap.put("name", speaker.getString("Fraction"));
            fractionMap.put("id", speaker.getInteger("FractionID"));
            speakerMap.put("fraction", fractionMap);
        }
        if (speaker.containsKey("PartyID")) {
            Document partyMap = new Document();
            partyMap.put("name", speaker.getString("Party"));
            partyMap.put("id", speaker.getInteger("PartyID"));
            speakerMap.put("party", partyMap);
        }
        return speakerMap;
    }

    /**
     * Apply bindings to a freemarker template.
     * @param template Template name.
     * @param input Mappings.
     * @return Filled template.
     * @throws TemplateException
     * @throws IOException
     */
    private String applyTemplate(String template, HashMap<String, Object> input) throws TemplateException, IOException {
        Writer stringWriter = new StringWriter();
        config.getTemplate(template).process(input, stringWriter);
        return stringWriter.toString();
    }

    /**
     * Resolve User object from session key.
     * @param key Session key (in application cookie).
     * @return User.
     */
    private User fromSession(String key) {
        MongoDatabase mongoDb = db.getDb();
        MongoCursor<Document> sesIter = mongoDb.getCollection("Sessions").find(new Document().append("key", key)).iterator();
        if (!sesIter.hasNext()) {
            throw new ValueException("session was not present in database");
        }
        Document sesDoc = sesIter.next();
        MongoCursor<Document> userIter = mongoDb.getCollection("Users").find(new Document().append("username", sesDoc.getString("username"))).iterator();
        if (!userIter.hasNext()) {
            throw new ValueException("user was not present in database");
        }
        Document user = userIter.next();
        return new User(user.getString("username"), user.getList("permissions", String.class));
    }

    /**
     * Initialize API endpoints.
     * @throws IOException
     */
    public void init() throws IOException {
        MongoDatabase mongoDb = db.getDb();
        WebSocket ws = new WebSocket(mongoDb);
        ws.init();
        // Static file location: templates/.
        Spark.staticFiles.externalLocation(new File("templates/").getPath());

        // Re-generate "admin" user on launch to ensure demonstration capabilities.
        mongoDb.getCollection("Users").updateOne(
                Filters.eq("_id", "admin"),
                Updates.combine(
                        Updates.set("username", "admin"),
                        Updates.set("password", "admin"),
                        Updates.set("permissions", new ArrayList<String>() {
                            {
                                add("admin");
                                add("protocol");
                                add("speech");
                                add("template");
                            }
                        })
                ),
                new UpdateOptions().upsert(true)
        );

        // Credential settings (session cookie).
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Credentials", "true");
        });

        /**
         * Home page.
         */
        get("/", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
            } catch (Exception e) {
                // User was not found. Proceed without login.
                response.status(200);
                HashMap<String, Object> user = new HashMap<>();
                user.put("loggedIn", false);
                return applyTemplate("index.ftl", user);
            }
            response.status(200);
            return applyTemplate("index.ftl", requester.toHashMap());
        });

        /**
         * Login page..
         */
        get("/login", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
            } catch (Exception e) {
                response.status(200);
                return applyTemplate("login.ftl", new HashMap<>());
            }
            // If user was already logged in, redirect to home page.
            response.redirect("/");
            return "OK";
        });

        /**
         * Admin page.
         */
        get("/admin", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("admin");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            response.status(200);
            ArrayList<Document> docs = new ArrayList<>();
            // Prepare user list to display.
            mongoDb.getCollection("Users").find().iterator().forEachRemaining(docs::add);
            HashMap<String, Object> hm = new HashMap<>();
            hm.put("users", docs.stream().map(doc -> new User(doc).toHashMap()).toArray());
            hm.putAll(requester.toHashMap());
            return applyTemplate("admin.ftl", hm);
        });

        /**
         * Editing page.
         */
        get("/edit", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                assert requester.getPermissions().size() != 0;
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            HashMap<String, Object> hm = requester.toHashMap();
            ArrayList<HashMap<String, Object>> templates = new ArrayList<>();
            mongoDb.getCollection("Templates").find().iterator().forEachRemaining((doc) -> {
                HashMap<String, Object> template = new HashMap<>();
                try {
                    template.put("id", URLEncoder.encode(doc.getString("_id"), "UTF-8"));
                } catch (Exception ignored) {
                    template.put("id", doc.getString("_id"));
                }
                template.put("type", doc.getString("type"));
                templates.add(template);
            });
            hm.put("templates", templates);
            return applyTemplate("edit.ftl", hm);
        });

        /**
         * Speech editing page.
         */
        get("/edit/speech", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("speech");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            HashMap<String, Object> mapping = requester.toHashMap();
            String id = request.queryParams("id");
            String agendaItemId = request.queryParams("agendaItemId");
            if (id != null) {
                // Speech was already present. Properties are fetched and sent to frontend.
                id = URLDecoder.decode(id, "UTF-8");
                MongoCursor<Document> speechIter = mongoDb.getCollection("Speeches").find(Filters.eq("_id", id)).projection(Projections.include("SpeakerID", "Speech")).iterator();
                if (!speechIter.hasNext()) {
                    response.status(404);
                    return "Not Found";
                }
                Document speechDoc = speechIter.next();
                mapping.put("isnew", false);
                mapping.put("id", id);
                mapping.put("speech", speechDoc.getString("Speech"));
                mapping.put("speakerId", speechDoc.getString("SpeakerID"));
                response.status(200);
                return applyTemplate("editspeech.ftl", mapping);
            } else if (agendaItemId != null) {
                // Proceed with new speech.
                agendaItemId = URLDecoder.decode(agendaItemId, "UTF-8");
                mapping.put("isnew", true);
                mapping.put("id", agendaItemId);
                mapping.put("speech", "");
                mapping.put("speakerId", "");
                response.status(200);
                return applyTemplate("editspeech.ftl", mapping);
            }
            response.status(400);
            return "Bad Request";
        });

        /**
         * Agenda item editing page.
         */
        get("/edit/agendaItem", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("protocol");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            HashMap<String, Object> mapping = requester.toHashMap();
            String id = request.queryParams("id");
            String protocolId = request.queryParams("protocolId");
            if (id != null) {
                // Agenda item was already present.
                id = URLDecoder.decode(id, "UTF-8");
                MongoCursor<Document> agendaItemIter = mongoDb.getCollection("AgendaItems").find(Filters.eq("_id", id)).projection(Projections.include("Title")).iterator();
                if (!agendaItemIter.hasNext()) {
                    response.status(404);
                    return "Not Found";
                }
                Document agendaDoc = agendaItemIter.next();
                mapping.put("isnew", false);
                mapping.put("id", id);
                mapping.put("title", agendaDoc.getString("Title"));
                ArrayList<HashMap<String, Object>> speeches = new ArrayList<>();
                mongoDb.getCollection("Speeches").find(Filters.eq("AgendaItemId", id)).projection(Projections.include("SpeakerID", "Speaker's Firstname", "Speaker's Lastname", "_id")).iterator().forEachRemaining((doc) -> {
                    HashMap<String, Object> mp = new HashMap<>();
                    mp.put("id", doc.getString("_id"));
                    mp.put("speaker", doc.getString("Speaker's Firstname") + doc.getString("Speaker's Lastname"));
                    speeches.add(mp);
                });
                mapping.put("speeches", speeches);
                response.status(200);
                return applyTemplate("editagendaitem.ftl", mapping);
            } else if (protocolId != null) {
                // User is attempting to create a new agenda item under the specified protocol.
                protocolId = URLDecoder.decode(protocolId, "UTF-8");
                mapping.put("isnew", true);
                mapping.put("id", protocolId);
                mapping.put("title", "");
                mapping.put("speeches", new ArrayList<>());
                response.status(200);
                return applyTemplate("editagendaitem.ftl", mapping);
            }
            response.status(400);
            return "Bad Request";
        });

        /**
         * Protocol editing page.
         */
        get("/edit/protocol", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("protocol");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            HashMap<String, Object> mapping = requester.toHashMap();
            String id = request.queryParams("id");
            if (id != null) {
                // Existing protocol is being modified.
                id = URLDecoder.decode(id, "UTF-8");
                MongoCursor<Document> protocolIter = mongoDb.getCollection("Protocols").find(Filters.eq("_id", id)).iterator();
                if (!protocolIter.hasNext()) {
                    response.status(404);
                    return "Not Found";
                }
                Document protocolDoc = protocolIter.next();
                mapping.put("isnew", false);
                mapping.put("id", id);
                mapping.put("dateformatted", new SimpleDateFormat("yyyy-MM-dd").format(new Date(protocolDoc.getLong("ExactTime") * 1000L)));
                mapping.put("starttime", protocolDoc.getString("Starttime"));
                mapping.put("endtime", protocolDoc.getString("Endtime"));
                mapping.put("title", protocolDoc.getString("Title"));
                mapping.put("location", protocolDoc.getString("Place"));
                mapping.put("period", protocolDoc.getInteger("ElectionPeriod"));
                ArrayList<HashMap<String, Object>> agendaItems = new ArrayList<>();
                mongoDb.getCollection("AgendaItems").find(Filters.eq("ProtocolID", id)).projection(Projections.include("Title", "_id")).iterator().forEachRemaining((doc) -> {
                    HashMap<String, Object> mp = new HashMap<>();
                    mp.put("id", doc.getString("_id"));
                    mp.put("title", doc.getString("Title"));
                    agendaItems.add(mp);
                });
                mapping.put("agendaitems", agendaItems);
                response.status(200);
                return applyTemplate("editprotocol.ftl", mapping);
            }
            // New protocol is being made.
            mapping.put("isnew", true);
            mapping.put("dateformatted", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            mapping.put("starttime", "");
            mapping.put("endtime", "");
            mapping.put("title", "");
            mapping.put("location", "Berlin");
            mapping.put("period", 20);
            mapping.put("agendaitems", new ArrayList<>());
            response.status(200);
            return applyTemplate("editprotocol.ftl", mapping);
        });

        /**
         * Login endpoint. Passes session key to client upon successful login.
         */
        post("/login", "application/json", (request, response) -> {
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String username = body.getString("username");
            String password = body.getString("password");
            Document filter = new Document().append("username", username).append("password", password);
            MongoCursor<Document> user = mongoDb.getCollection("Users").find(filter).iterator();
            if (!user.hasNext()) {
                // Either username or password is invalid. Login failed.
                response.status(403);
                user.close();
                return "Forbidden";
            }
            // Username and password valid, a new session is created and its key is given to the client.
            String key = Base64.getEncoder().encodeToString(ByteBuffer.allocate(64).putInt(new Random().nextInt((int) Math.pow(2, 64))).array());
            Document session = new Document().append("username", username).append("key", key);
            mongoDb.getCollection("Sessions").insertOne(session);
            response.header("set-cookie", "session=" + key + "; Expires=" + 2147483647 + "; Secure");
            response.status(200);
            user.close();
            return "OK";
        });

        /**
         * Logout endpoint. Invalidates user session.
         */
        get("/logout", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
            } catch (Exception e) {
                response.status(400);
                return "Bad Request";
            }
            mongoDb.getCollection("Sessions").deleteOne(new Document().append("key", request.cookie("session")));
            response.removeCookie("session");
            response.redirect("/");
            response.status(200);
            return "OK";
        });

        SpeechTemplate standardSpeech = new SpeechTemplate(mongoDb.getCollection("Templates").find(Filters.eq("_id", "1")).first());
        AgendaItemTemplate standardAgendaItem = new AgendaItemTemplate(mongoDb.getCollection("Templates").find(Filters.eq("_id", "2")).first());
        ProtocolTemplate standardProtocol = new ProtocolTemplate(mongoDb.getCollection("Templates").find(Filters.eq("_id", "3")).first());

        /**
         * Protocol PDF visualization endpoint. Updates /src.pdf endpoint to display the requested pdf.
         */
        get("/templated/protocol", (request, response) -> {
            String id = URLDecoder.decode(request.queryParams("id"), "UTF-8");
            Document protocol = mongoDb.getCollection("Protocols").find(Filters.eq("_id", id)).first();
            assert protocol != null;
            HashMap<String, Object> protocolMap = new HashMap<>();
            protocolMap.put("id", id);
            protocolMap.put("date", protocol.getString("Date"));
            protocolMap.put("starttime", protocol.getString("Starttime"));
            protocolMap.put("endtime", protocol.getString("Endtime"));
            protocolMap.put("title", protocol.getString("Title"));
            protocolMap.put("place", protocol.getString("Place"));
            protocolMap.put("period", protocol.getInteger("ElectionPeriod"));
            ArrayList<HashMap<String, Object>> agendaItems = new ArrayList<>();
            for (Document agendaItem : mongoDb.getCollection("AgendaItems").find(Filters.eq("ProtocolID", id))) {
                HashMap<String, Object> agendaMap = new HashMap<>();
                String agendaId = agendaItem.getString("_id");
                agendaMap.put("id", agendaId);
                agendaMap.put("title", agendaItem.getString("Title"));
                ArrayList<HashMap<String, Object>> speeches = new ArrayList<>();
                for (Document speech : mongoDb.getCollection("Speeches").find(Filters.eq("AgendaItemId", agendaId))) {
                    HashMap<String, Object> speechMap = new HashMap<>();
                    speechMap.put("id", speech.getString("_id"));
                    speechMap.put("content", speech.getString("Speech"));
                    MongoCursor<Document> speakerIter = mongoDb.getCollection("Speakers").find(Filters.eq("_id", speech.getString("SpeakerID"))).iterator();
                    if (speakerIter.hasNext()) {
                        Document speaker = speakerIter.next();
                        speechMap.put("speakerId", speaker.getString("_id"));
                        speechMap.put("firstname", speaker.getString("FirstName"));
                        speechMap.put("lastname", speaker.getString("LastName"));
                        if (speaker.containsKey("Fraction")) {
                            speechMap.put("fraction", speaker.getString("Fraction"));
                            speechMap.put("fractionId", speaker.getInteger("FractionID"));
                        }
                        if (speaker.containsKey("Party")) {
                            speechMap.put("party", speaker.getString("Party"));
                            speechMap.put("partyId", speaker.getInteger("PartyID"));
                        }
                    }
                    speeches.add(speechMap);
                }
                agendaMap.put("speeches", speeches);
                agendaItems.add(agendaMap);
            }
            protocolMap.put("agendaItems", agendaItems);
            String texCode = standardProtocol.toTeX(protocolMap, standardAgendaItem, standardSpeech);
            File file = new File("latex-out/src.tex");
            if (!file.createNewFile()) {
                file.delete();
                file.createNewFile();
            }
            FileWriter fw = new FileWriter("latex-out/src.tex");
            fw.write(texCode);
            fw.close();
            ProcessBuilder pb = new ProcessBuilder("pdflatex", "-interaction", "nonstopmode", "src.tex").inheritIO().directory(new File("latex-out/"));
            Process process = pb.start();
            process.waitFor();
            try {
                new File("templates/src.pdf").delete();
            } catch (Exception ignored) { }
            new File("latex-out/src.pdf").renameTo(new File("templates/src.pdf"));
            response.status(200);
            response.redirect("/src.pdf");
            return "OK";
        });

        /**
         * Templating page.
         */
        get("/template", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("template");
            } catch (Exception e) {
                response.status(400);
                return "Bad Request";
            }
            String id = request.queryParams("id");
            HashMap<String, Object> mapping = requester.toHashMap();
            if (id != null) {
                MongoCursor<Document> tempIter = mongoDb.getCollection("Templates").find(Filters.eq("_id", id)).iterator();
                if (!tempIter.hasNext()) {
                    response.status(404);
                    return "Not Found";
                }
                Document temp = tempIter.next();
                mapping.put("isnew", false);
                mapping.put("id", id);
                mapping.put("type", temp.getString("type"));
                mapping.put("raw", temp.getString("raw"));
            } else {
                mapping.put("isnew", true);
                mapping.put("type", "protocol");
                mapping.put("raw", "");
            }
            return applyTemplate("edittemplate.ftl", mapping);
        });

        /**
         * Create new template.
         */
        put("/template", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("template");
            } catch (Exception e) {
                response.status(400);
                return "Bad Request";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String id = new ObjectId().toString();
            String type = body.getString("type");
            String raw = body.getString("raw");
            mongoDb.getCollection("Templates").insertOne(new Document()
                    .append("_id", id)
                    .append("type", type)
                    .append("raw", raw)
            );
            response.status(200);
            return new Document().append("id", id).toJson();
        });

        /**
         * Update existing template.
         */
        post("/template", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("template");
            } catch (Exception e) {
                response.status(400);
                return "Bad Request";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String id = body.getString("id");
            String raw = body.getString("raw");
            mongoDb.getCollection("Templates").updateOne(Filters.eq("_id", id), Updates.set("raw", raw));
            response.status(200);
            return "OK";
        });

        /**
         * Delete template.
         */
        delete("/template", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("template");
            } catch (Exception e) {
                response.status(400);
                return "Bad Request";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String id = body.getString("id");
            mongoDb.getCollection("Templates").deleteOne(Filters.eq("_id", id));
            response.status(200);
            return "OK";
        });

        /**
         * Create new user.
         */
        put("/user", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("admin");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String username = body.getString("username");
            String password = body.getString("password");
            ArrayList<String> permissions = new ArrayList<>();
            body.getJSONArray("permissions").iterator().forEachRemaining((p) -> permissions.add((String) p));
            Document document = new Document();
            document.put("_id", username);
            document.put("username", username);
            document.put("password", password);
            document.put("permissions", permissions);
            mongoDb.getCollection("Users").insertOne(document);
            response.status(200);
            return "OK";
        });

        /**
         * Update existing user.
         */
        post("/user", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("admin");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String username = body.getString("username");
            ArrayList<String> permissions = new ArrayList<>();
            body.getJSONArray("permissions").iterator().forEachRemaining((p) -> permissions.add((String) p));
            Document filter = new Document();
            filter.put("username", username);
            mongoDb.getCollection("Users").updateOne(filter, Updates.set("permissions", permissions));
            response.status(200);
            return "OK";
        });

        /**
         * Delete user.
         */
        delete("/user", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("admin");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String username = body.getString("username");
            Document filter = new Document().append("username", username);
            mongoDb.getCollection("Users").deleteOne(filter);
            response.status(200);
            return "OK";
        });

        /**
         * Visualization results (displayed on main page). Execution time is volatile if sample size is too large.
         */
        get("/search", (request, response) -> {
            response.type("application/json");
            Document tokens = new Document();
            Document partsOfSpeech = new Document();
            int[] sentiment = {0, 0, 0};
            Document namedPersons = new Document();
            Document namedLocations = new Document();
            Document namedOrganizations = new Document();
            Document speakerSpeechCount = new Document();
            Document results = new Document();

            String query = request.queryParams("query");
            String from = request.queryParams("from");
            String until = request.queryParams("until");

            Bson queryFilter = null;
            if (query != null) queryFilter = Filters.regex("Speech", Pattern.compile(URLDecoder.decode(query, "UTF-8"), Pattern.CASE_INSENSITIVE));
            ArrayList<Bson> protocolFilters = new ArrayList<>();
            if (from != null) protocolFilters.add(Filters.gte("ExactTime", Long.parseLong(URLDecoder.decode(from, "UTF-8")) / 1000));
            if (until != null) protocolFilters.add(Filters.lte("ExactTime", Long.parseLong(URLDecoder.decode(until, "UTF-8")) / 1000));
            FindIterable<Document> protocolFinder = protocolFilters.size() > 0 ? mongoDb.getCollection("Protocols").find(Filters.and(protocolFilters)).projection(Projections.include("_id")) : mongoDb.getCollection("Protocols").find().projection(Projections.include("_id"));
            for (Document protocol : protocolFinder) {
                ArrayList<Bson> queryFilters = new ArrayList<>();
                queryFilters.add(Filters.eq("ProtocolID", protocol.getString("_id")));
                if (queryFilter != null) queryFilters.add(queryFilter);

                FindIterable<Document> speechFinder = mongoDb.getCollection("Speeches").find(Filters.and(queryFilters));
                speechFinder.projection(Projections.include("_id", "SpeakerID"));
                for (Document speech : speechFinder) {
                    MongoCursor<Document> nlpIterable = mongoDb.getCollection("SpeechNLP").find(Filters.eq("_id", speech.getString("_id"))).iterator();
                    if (!nlpIterable.hasNext()) continue;

                    Document nlp = nlpIterable.next();
                    nlp.getList("POS", String.class).forEach(pos -> partsOfSpeech.merge(pos, 1, SparkAPI::sum));
                    double avgSentiment = nlp.getDouble("Avg-Sentiment");
                    if (avgSentiment < 0) sentiment[0]++;
                    else if (avgSentiment == 0) sentiment[1]++;
                    else sentiment[2]++;
                    nlp.getList("LemmaValue", String.class).forEach(tok -> tokens.merge(tok, 1, SparkAPI::sum));
                    nlp.getList("Persons", String.class).forEach(person -> namedPersons.merge(person, 1, SparkAPI::sum));
                    nlp.getList("Locations", String.class).forEach(location -> namedLocations.merge(location, 1, SparkAPI::sum));
                    nlp.getList("Organisations", String.class).forEach(organization -> namedOrganizations.merge(organization, 1, SparkAPI::sum));

                    String speakerId = speech.getString("SpeakerID");
                    if (speakerId == null) continue;
                    speakerSpeechCount.merge(speakerId, 1, SparkAPI::sum);
                }
            }

            ArrayList<Document> TOK = new ArrayList<>();
            ArrayList<Document> POS = new ArrayList<>();
            Document sentimentDoc = new Document();
            ArrayList<Document> peopleDoc = new ArrayList<>();
            ArrayList<Document> placesDoc = new ArrayList<>();
            ArrayList<Document> organizationsDoc = new ArrayList<>();
            Document NE = new Document().append("people", peopleDoc).append("places", placesDoc).append("organizations", organizationsDoc);
            ArrayList<Document> speakers = new ArrayList<>();
            tokens.forEach((tok, count) -> TOK.add(new Document().append("specifier", tok).append("count", count)));
            partsOfSpeech.forEach((pos, count) -> POS.add(new Document().append("specifier", pos).append("count", count)));
            sentimentDoc.append("negative", sentiment[0]).append("neutral", sentiment[1]).append("positive", sentiment[2]);
            namedPersons.forEach((pers, count) -> peopleDoc.add(new Document().append("specifier", pers).append("count", count)));
            namedLocations.forEach((loc, count) -> placesDoc.add(new Document().append("specifier", loc).append("count", count)));
            namedOrganizations.forEach((org, count) -> organizationsDoc.add(new Document().append("specifier", org).append("count", count)));
            speakerSpeechCount.forEach((speakerId, count) -> {
                Document speaker = speakerFromId(speakerId);
                if (speaker != null) speakers.add(speaker.append("count", count));
            });
            results.append("TOK", TOK).append("POS", POS).append("sentiment", sentimentDoc).append("NE", NE).append("speakers", speakers);
            response.status(200);
            return results.toJson();
        });

        /**
         * Manual navigation results (used in corpus-index). Also filters based on query data.
         */
        get("/corpus", (request, response) -> {
            response.type("application/json");
            Calendar calendar = Calendar.getInstance(new Locale("DE"));

            String query = request.queryParams("query");
            String from = request.queryParams("from");
            String until = request.queryParams("until");
            String groupBy = request.queryParams("groupBy");

            Document groups = new Document();
            Function<Calendar, String> groupFn = null;
            if (groupBy == null) {
                groupFn = (cal) -> "Ergebnisse";
            } else {
                switch (groupBy) {
                    case "day":
                        groupFn = (cal) -> cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH);
                        break;
                    case "week":
                        groupFn = (cal) -> "Woche " + cal.get(Calendar.WEEK_OF_YEAR) + ", " + cal.get(Calendar.YEAR);
                        break;
                    case "month":
                        groupFn = (cal) -> cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1);
                        break;
                    case "year":
                        groupFn = (cal) -> cal.get(Calendar.YEAR) + "";
                        break;
                    default:
                        groupFn = (cal) -> "Ergebnisse";
                }
            }

            ArrayList<Bson> protocolFilters = new ArrayList<>();
            Bson queryFilter = null;
            if (query != null) queryFilter = Filters.regex("Speech", Pattern.compile(URLDecoder.decode(query, "UTF-8"), Pattern.CASE_INSENSITIVE));
            if (from != null) protocolFilters.add(Filters.gte("ExactTime", Long.parseLong(URLDecoder.decode(from, "UTF-8")) / 1000));
            if (until != null) protocolFilters.add(Filters.lte("ExactTime", Long.parseLong(URLDecoder.decode(until, "UTF-8")) / 1000));

            FindIterable<Document> protocolIter = protocolFilters.size() > 0 ? mongoDb.getCollection("Protocols").find(Filters.and(protocolFilters)).projection(Projections.include("_id", "Title", "Date", "ExactTime")) : mongoDb.getCollection("Protocols").find().projection(Projections.include("_id", "Title", "Date", "ExactTime"));
            for (Document protocol : protocolIter) {
                calendar.setTime(new Date(protocol.getLong("ExactTime") * 1000L));
                String groupName = groupFn.apply(calendar);
                Document protocolDoc = new Document();
                protocolDoc.put("id", protocol.getString("_id"));
                protocolDoc.put("title", protocol.getString("Title"));
                if (query != null) {
                    ArrayList<Document> agendaItems = new ArrayList<>();
                    for (Document agendaItem : mongoDb.getCollection("AgendaItems").find(Filters.eq("ProtocolID", protocol.getString("_id")))) {
                        ArrayList<Document> speeches = new ArrayList<>();
                        ArrayList<Bson> queryFilters = new ArrayList<>();
                        queryFilters.add(Filters.eq("AgendaItemId", agendaItem.getString("_id")));
                        queryFilters.add(queryFilter);
                        for (Document speech : mongoDb.getCollection("Speeches").find(Filters.and(queryFilters))) {
                            Document putSpeech = new Document();
                            putSpeech.put("id", speech.getString("_id"));
                            putSpeech.put("speaker", speakerFromId(speech.getString("SpeakerID")));
                            speeches.add(putSpeech);
                        }
                        if (speeches.size() > 0) {
                            Document agendaItemDoc = new Document();
                            agendaItemDoc.put("id", agendaItem.getString("_id"));
                            agendaItemDoc.put("title", agendaItem.getString("Title"));
                            agendaItemDoc.put("speeches", speeches);
                            agendaItems.add(agendaItemDoc);
                        }
                    }
                    if (agendaItems.size() == 0) continue;
                    protocolDoc.put("agendaItems", agendaItems);
                }
                List<Document> list = null;
                if (groups.containsKey(groupName)) {
                    list = groups.getList(groupName, Document.class);
                } else {
                    list = new ArrayList<>();
                    groups.put(groupName, list);
                }
                list.add(protocolDoc);
            }
            Document returns = new Document();
            List<Document> groupsDoc = new ArrayList<>();
            groups.forEach((groupName, protocolDocs) -> groupsDoc.add(new Document().append("groupName", groupName).append("protocols", protocolDocs)));
            returns.put("groups", groupsDoc);
            response.status(200);
            return returns.toJson();
        });

        /**
         * Speech analysis result endpoint. Used in full text visualization.
         */
        get("/speech", (request, response) -> {
            response.type("application/json");
            String id = URLDecoder.decode(request.queryParams("id"), "UTF-8");
            MongoCursor<Document> speechIter = mongoDb.getCollection("Speeches").find(Filters.eq("_id", id)).projection(Projections.include("SpeakerID")).iterator();
            if (!speechIter.hasNext()) {
                response.status(404);
                return "Not Found";
            }
            Document speech = speechIter.next();
            Document doc = new Document();
            doc.put("id", id);
            doc.put("speaker", speakerFromId(speech.getString("SpeakerID")));

            MongoCursor<Document> nlpIter = mongoDb.getCollection("SpeechNLP").find(Filters.eq("_id", id)).projection(Projections.include("SpeechCas")).iterator();
            if (!nlpIter.hasNext()) {
                doc.put("sentences", null);
            } else {
                ArrayList<Document> sentenceDocs = new ArrayList<>();
                Document nlpDoc = nlpIter.next();
                JCas jcas = JCasFactory.createJCas();
                deserializeCasFromXml(nlpDoc.getString("SpeechCas"), jcas);
                Iterator<Sentence> sentences = JCasUtil.select(jcas, Sentence.class).iterator();
                Iterator<Sentiment> sentiments = JCasUtil.select(jcas, Sentiment.class).iterator();
                ArrayList<NamedEntity> namedEntities = new ArrayList<>(JCasUtil.select(jcas, NamedEntity.class));

                while (sentences.hasNext()) {
                    Sentence sentence = sentences.next();
                    double sentiment = sentiments.next().getSentiment();
                    int sentenceBegin = sentence.getBegin();
                    int sentenceEnd = sentence.getEnd();
                    List<NamedEntity> namedEntityList = namedEntities.stream().filter((ne) -> ne.getBegin() >= sentenceBegin && ne.getEnd() <= sentenceEnd).collect(Collectors.toList());
                    sentenceDocs.add(
                            new Document()
                                    .append("sentiment", sentiment)
                                    .append("text", sentence.getCoveredText())
                                    .append("namedEntities", namedEntityList.stream().map((ne) -> new Document().append("type", ne.getValue()).append("position", new Document().append("begin", ne.getBegin() - sentenceBegin).append("end", ne.getEnd() - sentenceBegin))).collect(Collectors.toList()))
                    );
                }
                doc.put("sentences", sentenceDocs);
            }
            response.status(200);
            return doc.toJson();
        });

        /**
         * Raw speech text without analysis.
         */
        get("/speechRaw", (request, response) -> {
            response.type("application/json");
            String id = URLDecoder.decode(request.queryParams("id"), "UTF-8");
            MongoCursor<Document> resIter = mongoDb.getCollection("Speeches").find(Filters.eq("_id", id)).projection(Projections.include("Speech", "SpeakerID")).iterator();
            if (!resIter.hasNext()) {
                response.status(404);
                return "Not Found";
            }
            Document doc = resIter.next();
            response.status(200);
            return new Document().append("id", id).append("speaker", speakerFromId(doc.getString("SpeakerID"))).append("content", doc.getString("Speech")).toJson();
        });

        /**
         * Agenda item corpus index info.
         */
        get("/agendaItem", (request, response) -> {
            response.type("application/json");
            String id = URLDecoder.decode(request.queryParams("id"), "UTF-8");
            MongoCursor<Document> agendaItemIter = mongoDb.getCollection("AgendaItems").find(Filters.eq("_id", id)).projection(Projections.include("Title")).iterator();
            if (!agendaItemIter.hasNext()) {
                response.status(404);
                return "Not Found";
            }
            Document outDoc = new Document();
            Document agendaDoc = agendaItemIter.next();
            outDoc.put("id", id);
            outDoc.put("title", agendaDoc.getString("Title"));
            ArrayList<Document> speeches = new ArrayList<>();
            mongoDb.getCollection("Speeches").find(Filters.eq("AgendaItemId", id)).projection(Projections.include("_id", "SpeakerID")).iterator().forEachRemaining((doc) -> speeches.add(new Document().append("id", doc.getString("_id")).append("speaker", speakerFromId(doc.getString("SpeakerID")))));
            outDoc.put("speeches", speeches);
            response.status(200);
            return outDoc.toJson();
        });

        /**
         * Protocol corpus index info.
         */
        get("/protocol", (request, response) -> {
            response.type("application/json");
            String id = URLDecoder.decode(request.queryParams("id"), "UTF-8");
            MongoCursor<Document> protocolIter = mongoDb.getCollection("Protocols").find(Filters.eq("_id", id)).projection(Projections.include("Title")).iterator();
            if (!protocolIter.hasNext()) {
                response.status(404);
                return "Not Found";
            }
            Document outDoc = new Document();
            Document protocolDoc = protocolIter.next();
            outDoc.put("id", id);
            outDoc.put("title", protocolDoc.getString("Title"));
            ArrayList<Document> agendaItems = new ArrayList<>();
            mongoDb.getCollection("AgendaItems").find(Filters.eq("ProtocolID", id)).projection(Projections.include("_id", "Title")).iterator().forEachRemaining((doc) -> agendaItems.add(new Document().append("id", doc.getString("_id")).append("title", doc.getString("Title"))));
            outDoc.put("agendaItems", agendaItems);
            response.status(200);
            return outDoc.toJson();
        });

        /**
         * Delete a protocol.
         */
        delete("/edit/protocol", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("protocol");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String id = body.getString("id");
            FindIterable<Document> speechIter = mongoDb.getCollection("Speeches").find(Filters.eq("ProtocolID", id)).projection(Projections.include("_id"));
            ArrayList<String> speechIds = new ArrayList<>();
            speechIter.iterator().forEachRemaining((doc) -> speechIds.add(doc.getString("_id")));
            FindIterable<Document> commentIter = mongoDb.getCollection("Comments").find(Filters.in("SpeechID", speechIds));
            ArrayList<String> commentIds = new ArrayList<>();
            commentIter.iterator().forEachRemaining((doc) -> commentIds.add(doc.getString("_id")));
            mongoDb.getCollection("SpeechNLP").deleteMany(Filters.in("_id", speechIds));
            mongoDb.getCollection("CommentNLP").deleteMany(Filters.in("_id", commentIds));
            mongoDb.getCollection("Speeches").deleteMany(Filters.eq("ProtocolID", id));
            mongoDb.getCollection("Comments").deleteMany(Filters.in("SpeechID", speechIds));
            mongoDb.getCollection("AgendaItems").deleteMany(Filters.eq("ProtocolID", id));
            mongoDb.getCollection("Protocols").deleteOne(Filters.eq("_id", id));
            response.status(200);
            return "OK";
        });

        /**
         * Delete an agenda item.
         */
        delete("/edit/agendaItem", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("protocol");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String id = body.getString("id");
            FindIterable<Document> speechIter = mongoDb.getCollection("Speeches").find(Filters.eq("AgendaItemId", id)).projection(Projections.include("_id"));
            ArrayList<String> speechIds = new ArrayList<>();
            speechIter.iterator().forEachRemaining((doc) -> speechIds.add(doc.getString("_id")));
            FindIterable<Document> commentIter = mongoDb.getCollection("Comments").find(Filters.in("SpeechID", speechIds));
            ArrayList<String> commentIds = new ArrayList<>();
            commentIter.iterator().forEachRemaining((doc) -> commentIds.add(doc.getString("_id")));
            mongoDb.getCollection("SpeechNLP").deleteMany(Filters.in("_id", speechIds));
            mongoDb.getCollection("CommentNLP").deleteMany(Filters.in("_id", commentIds));
            mongoDb.getCollection("Speeches").deleteMany(Filters.eq("AgendaItemId", id));
            mongoDb.getCollection("Comments").deleteMany(Filters.in("SpeechID", speechIds));
            mongoDb.getCollection("AgendaItems").deleteOne(Filters.eq("_id", id));

            MongoCursor<Document> protocolIter = mongoDb.getCollection("Protocols").find(Filters.eq("AgendaItems", id)).projection(Projections.include("_id", "AgendaItems")).iterator();
            if (protocolIter.hasNext()) {
                Document protocol = protocolIter.next();
                List<String> agendaList = protocol.getList("AgendaItems", String.class);
                agendaList.removeIf((itemId) -> itemId.equals(id));
                mongoDb.getCollection("Protocols").updateOne(Filters.eq("_id", protocol.getString("_id")), Updates.set("AgendaItems", agendaList));
            }

            response.status(200);
            return "OK";
        });

        /**
         * Delete a speech.
         */
        delete("/edit/speech", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("speech");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String id = body.getString("id");
            MongoCursor<Document> speechIter = mongoDb.getCollection("Speeches").find(Filters.eq("_id", id)).projection(Projections.include("AgendaItemId")).iterator();
            if (!speechIter.hasNext()) {
                response.status(404);
                return "Not Found";
            }
            Document speechDoc = speechIter.next();
            FindIterable<Document> commentIter = mongoDb.getCollection("Comments").find(Filters.eq("SpeechID", id)).projection(Projections.include("_id"));
            ArrayList<String> commentIds = new ArrayList<>();
            commentIter.iterator().forEachRemaining((doc) -> commentIds.add(doc.getString("_id")));
            mongoDb.getCollection("CommentNLP").deleteMany(Filters.in("_id", commentIds));
            mongoDb.getCollection("Comments").deleteMany(Filters.eq("SpeechID", id));
            mongoDb.getCollection("Speeches").deleteOne(Filters.eq("_id", id));
            mongoDb.getCollection("AgendaItems").updateOne(Filters.eq("_id", speechDoc.getString("AgendaItemId")), Updates.inc("Total Number of Speeches", -1));
            response.status(200);
            return "OK";
        });

        /**
         * Speaker editing page.
         */
        get("/edit/speaker", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("protocol");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            String id = request.queryParams("id");
            HashMap<String, Object> hm = requester.toHashMap();
            if (id != null) {
                MongoCursor<Document> speakerIter = mongoDb.getCollection("Speakers").find(Filters.eq("_id", id)).iterator();
                if (!speakerIter.hasNext()) {
                    response.status(404);
                    return "Not Found";
                }
                Document speaker = speakerIter.next();
                hm.put("isnew", false);
                hm.put("id", id);
                hm.put("title", speaker.getString("Title"));
                hm.put("firstname", speaker.getString("FirstName"));
                hm.put("lastname", speaker.getString("LastName"));
                hm.put("dob", new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd.MM.yyyy").parse(speaker.getString("Birthday"))));
                String death = speaker.getString("Death");
                if (death.length() > 0) {
                    hm.put("deceased", true);
                    hm.put("dod", new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd.MM.yyyy").parse(speaker.getString("Death"))));
                } else {
                    hm.put("deceased", false);
                    hm.put("dod", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                }
                hm.put("placeOfBirth", speaker.getString("PlaceOfBirth"));
                hm.put("sex", speaker.getString("SEX"));
                hm.put("maritalStatus", speaker.getString("MaritalStatus"));
                hm.put("religion", speaker.getString("Religion"));
                hm.put("academicTitle", speaker.getString("AcademicTitle"));
                hm.put("occupation", speaker.getString("Occupation"));
                hm.put("role", speaker.getString("Role"));
                hm.put("isLeader", speaker.getBoolean("IsLeader"));
                if (speaker.containsKey("PartyID")) {
                    hm.put("partyId", speaker.getInteger("PartyID"));
                } else {
                    hm.put("partyId", "");
                }
                if (speaker.containsKey("FractionID")) {
                    hm.put("fractionId", speaker.getInteger("FractionID"));
                } else {
                    hm.put("fractionId", "");
                }
                MongoCursor<Document> imgIter = mongoDb.getCollection("Photos").find(Filters.eq("SpeakerID", id)).iterator();
                if (!imgIter.hasNext()) {
                    hm.put("image", "");
                } else {
                    hm.put("image", imgIter.next().getString("PhotoLink"));
                }
            } else {
                hm.put("isnew", true);
                hm.put("title", "");
                hm.put("firstname", "");
                hm.put("lastname", "");
                hm.put("dob", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                hm.put("deceased", false);
                hm.put("dod", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                hm.put("placeOfBirth", "");
                hm.put("sex", "");
                hm.put("maritalStatus", "");
                hm.put("religion", "");
                hm.put("academicTitle", "");
                hm.put("occupation", "");
                hm.put("role", "");
                hm.put("isLeader", false);
                hm.put("partyId", "");
                hm.put("fractionId", "");
                hm.put("image", "");
            }
            response.status(200);
            return applyTemplate("editspeaker.ftl", hm);
        });

        /**
         * Create a new speaker.
         */
        put("/edit/speaker", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("protocol");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String id = new ObjectId().toString();
            Document speakerDoc = new Document()
                    .append("_id", id)
                    .append("Title", body.getString("title"))
                    .append("FirstName", body.getString("firstname"))
                    .append("LastName", body.getString("lastname"))
                    .append("Birthday", new SimpleDateFormat("dd.MM.yyyy").format(new Date(body.getLong("dob"))))
                    .append("PlaceOfBirth", body.getString("placeOfBirth"))
                    .append("SEX", body.getString("sex"))
                    .append("MaritalStatus", body.getString("maritalStatus"))
                    .append("Religion", body.getString("religion"))
                    .append("AcademicTitle", body.getString("academicTitle"))
                    .append("Occupation", body.getString("occupation"))
                    .append("Role", body.getString("role"))
                    .append("IsLeader", body.getBoolean("isLeader"))
                    .append("Speaker's Speech-Length", 0);

            if (body.getBoolean("deceased")) {
                speakerDoc.put("Death", new SimpleDateFormat("dd.MM.yyyy").format(new Date(body.getLong("dod"))));
            } else {
                speakerDoc.put("Death", "");
            }

            if (body.has("partyId") && body.getString("partyId").length() > 0) {
                int partyId = body.getInt("partyId");
                MongoCursor<Document> partyIter = mongoDb.getCollection("Parties").find(Filters.eq("_id", partyId)).iterator();
                if (!partyIter.hasNext()) {
                    response.status(404);
                    return "Not Found";
                }
                Document partyDoc = partyIter.next();
                speakerDoc.put("PartyID", partyId);
                speakerDoc.put("Party", partyDoc.getString("Party"));
            }

            if (body.has("fractionId") && body.getString("fractionId").length() > 0) {
                int fractionId = body.getInt("fractionId");
                MongoCursor<Document> fractionIter = mongoDb.getCollection("Fractions").find(Filters.eq("_id", fractionId)).iterator();
                if (!fractionIter.hasNext()) {
                    response.status(404);
                    return "Not Found";
                }
                Document fractionDoc = fractionIter.next();
                speakerDoc.put("FractionID", fractionId);
                speakerDoc.put("Fraction", fractionDoc.getString("Fraction"));
            }

            if (body.has("imageUrl") && body.getString("imageUrl").length() > 0)
            mongoDb.getCollection("Photos").insertOne(new Document().append("_id", new ObjectId().toString()).append("PhotoLink", body.getString("imageUrl")).append("SpeakerID", id));
            mongoDb.getCollection("Speakers").insertOne(speakerDoc);
            response.status(200);
            return new Document().append("id", id).toJson();
        });

        /**
         * Update an existing speaker.
         */
        post("/edit/speaker", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("protocol");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String id = body.getString("id");
            MongoCursor<Document> speakerIter = mongoDb.getCollection("Speakers").find(Filters.eq("_id", id)).iterator();
            if (!speakerIter.hasNext()) {
                response.status(404);
                return "Not Found";
            }
            ArrayList<Bson> updates = new ArrayList<>();
            if (body.has("title")) {
                updates.add(Updates.set("Title", body.getString("title")));
            }
            if (body.has("firstname")) {
                updates.add(Updates.set("FirstName", body.getString("firstname")));
            }
            if (body.has("lastname")) {
                updates.add(Updates.set("LastName", body.getString("lastname")));
            }
            if (body.has("partyId")) {
                int partyId = body.getInt("partyId");
                MongoCursor<Document> partyIter = mongoDb.getCollection("Parties").find(Filters.eq("_id", partyId)).iterator();
                if (!partyIter.hasNext()) {
                    response.status(404);
                    return "Not Found";
                }
                Document partyDoc = partyIter.next();
                updates.add(Updates.set("PartyID", partyId));
                updates.add(Updates.set("Party", partyDoc.getString("Party")));
            }
            if (body.has("fractionId")) {
                int fractionId = body.getInt("fractionId");
                MongoCursor<Document> fractionIter = mongoDb.getCollection("Fractions").find(Filters.eq("_id", fractionId)).iterator();
                if (!fractionIter.hasNext()) {
                    response.status(404);
                    return "Not Found";
                }
                Document fractionDoc = fractionIter.next();
                updates.add(Updates.set("FractionID", fractionId));
                updates.add(Updates.set("Fraction", fractionDoc.getString("Fraction")));
            }
            if (body.has("dob")) {
                updates.add(Updates.set("Birthday", new SimpleDateFormat("dd.MM.yyyy").format(new Date(body.getLong("dob")))));
            }
            if (body.has("dod")) {
                updates.add(Updates.set("Death", new SimpleDateFormat("dd.MM.yyyy").format(new Date(body.getLong("dod")))));
            }
            if (body.has("placeOfBirth")) {
                updates.add(Updates.set("PlaceOfBirth", body.getString("placeOfBirth")));
            }
            if (body.has("sex")) {
                updates.add(Updates.set("SEX", body.getString("sex")));
            }
            if (body.has("maritalStatus")) {
                updates.add(Updates.set("MaritalStatus", body.getString("maritalStatus")));
            }
            if (body.has("religion")) {
                updates.add(Updates.set("Religion", body.getString("religion")));
            }
            if (body.has("academicTitle")) {
                updates.add(Updates.set("AcademicTitle", body.getString("academicTitle")));
            }
            if (body.has("occupation")) {
                updates.add(Updates.set("Occupation", body.getString("occupation")));
            }
            if (body.has("role")) {
                updates.add(Updates.set("Role", body.getString("role")));
            }
            if (body.has("isLeader")) {
                updates.add(Updates.set("IsLeader", body.getBoolean("isLeader")));
            }
            if (body.has("imageUrl") && body.getString("imageUrl").length() > 0) {
                mongoDb.getCollection("Photos").updateOne(Filters.eq("SpeakerID", id), Updates.set("PhotoLink", body.getString("imageUrl")));
            }
            mongoDb.getCollection("Speakers").updateOne(Filters.eq("_id", id), updates);
            response.status(200);
            return "OK";
        });

        /**
         * Delete a speaker.
         */
        delete("/edit/speaker", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("protocol");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String id = body.getString("id");
            mongoDb.getCollection("Speakers").deleteOne(Filters.eq("_id", id));
            response.status(200);
            return "OK";
        });

        /**
         * Edit a speech. Analysis is repeated upon edit.
         */
        post("/edit/speech", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("speech");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String id = body.getString("id");
            ArrayList<Bson> updates = new ArrayList<>();

            if (body.has("speakerId")) {
                String speakerId = body.getString("speakerId");
                updates.add(Updates.set("SpeakerID", speakerId));
            }
            if (body.has("content")) {
                String content = body.getString("content");
                updates.add(Updates.set("Speech", content));
            }

            mongoDb.getCollection("Speeches").updateOne(Filters.eq("_id", id), updates);
            db.singleSpeech(id);
            response.status(200);
            return "OK";
        });

        /**
         * Edit an agenda item.
         */
        post("/edit/agendaItem", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("protocol");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String id = body.getString("id");
            String title = body.getString("title");
            mongoDb.getCollection("AgendaItems").updateOne(Filters.eq("_id", id), Updates.set("Title", title));
            response.status(200);
            return "OK";
        });

        /**
         * Edit a protocol.
         */
        post("/edit/protocol", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("protocol");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String id = body.getString("id");
            ArrayList<Bson> updates = new ArrayList<>();

            if (body.has("title")) {
                updates.add(Updates.set("Title", body.getString("title")));
            }
            if (body.has("date")) {
                long dateMillis = body.getLong("date");
                updates.add(Updates.set("ExactTime", (int) (dateMillis / 1000)));
                updates.add(Updates.set("Date", new SimpleDateFormat("dd-MM-yyyy").format((new Date(dateMillis)))));
            }
            if (body.has("starttime")) {
                updates.add(Updates.set("Starttime", body.getString("starttime")));
            }
            if (body.has("endtime")) {
                updates.add(Updates.set("Endtime", body.getString("endtime")));
            }
            if (body.has("location")) {
                updates.add(Updates.set("Place", body.getString("location")));
            }
            if (body.has("period")) {
                updates.add(Updates.set("ElectionPeriod", body.getInt("period")));
            }
            mongoDb.getCollection("Protocols").updateOne(Filters.eq("_id", id), updates);
            response.status(200);
            return "OK";
        });

        /**
         * Create a new protocol.
         */
        put("/edit/protocol", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("protocol");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            int period = body.getInt("period");
            long dateMillis = body.getLong("date");
            Document protocolDoc = new Document()
                    .append("Title", body.getString("title"))
                    .append("Date", new SimpleDateFormat("dd.MM.yyyy").format(new Date(dateMillis)))
                    .append("ExactTime", (int) (dateMillis / 1000))
                    .append("Starttime", body.getString("starttime"))
                    .append("Endtime", body.getString("endtime"))
                    .append("Place", body.getString("location"))
                    .append("ElectionPeriod", body.getInt("period"))
                    .append("AgendaItems", new ArrayList<>());
            ArrayList<Integer> indeces = new ArrayList<>();
            indeces.add(0);
            mongoDb.getCollection("Protocols").find(Filters.eq("ElectionPeriod", period)).projection(Projections.include("Index")).iterator().forEachRemaining((doc) -> indeces.add(doc.getInteger("Index")));
            int thisIndex = indeces.stream().max(Integer::compare).get() + 1;
            protocolDoc.put("_id", " " + period + "/" + thisIndex);
            protocolDoc.put("Index", thisIndex);
            mongoDb.getCollection("Protocols").insertOne(protocolDoc);
            response.status(200);
            return new Document().append("id", protocolDoc.getString("_id")).toJson();
        });

        /**
         * Create a new agenda item under a specified protocol.
         */
        put("/edit/agendaItem", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("protocol");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String protocolId = body.getString("protocolId");
            Document agendaItemDoc = new Document()
                    .append("ProtocolID", protocolId);
            ArrayList<Integer> indeces = new ArrayList<>();
            indeces.add(0);
            mongoDb.getCollection("AgendaItems").find(Filters.eq("ProtocolID", protocolId)).projection(Projections.include("AgendaIndex")).iterator().forEachRemaining((doc) -> indeces.add(Integer.parseInt(doc.getString("AgendaIndex").replaceAll(" ", ""))));
            int thisIndex = indeces.stream().max(Integer::compare).get() + 1;
            agendaItemDoc.put("AgendaIndex", " " + thisIndex);
            agendaItemDoc.put("_id", protocolId + "; " + thisIndex);
            agendaItemDoc.put("Title", body.has("title") ? body.getString("title") : "Tagesordnungspunkt " + thisIndex);
            agendaItemDoc.put("Total Number of Speeches", 0);
            mongoDb.getCollection("Protocols").updateOne(Filters.eq("_id", protocolId), Updates.push("AgendaItems", agendaItemDoc.getString("_id")));
            mongoDb.getCollection("AgendaItems").insertOne(agendaItemDoc);
            response.status(200);
            return new Document().append("id", agendaItemDoc.getString("_id")).toJson();
        });

        /**
         * Create a new speech under a specified agenda item. Analysis is launched upon creation.
         */
        put("/edit/speech", "application/json", (request, response) -> {
            User requester = null;
            try {
                requester = fromSession(request.cookie("session"));
                requester.hasPermission("speech");
            } catch (Exception e) {
                response.status(403);
                return "Forbidden";
            }
            JSONObject body = new JSONObject(new JSONTokener(request.body()));
            String speakerId = body.getString("speakerId");
            String content = body.getString("content");
            String agendaItemId = body.getString("agendaItemId");

            MongoCursor<Document> agendaIter = mongoDb.getCollection("AgendaItems").find(Filters.eq("_id", agendaItemId)).projection(Projections.include("ProtocolID")).iterator();
            if (!agendaIter.hasNext()) {
                response.status(404);
                return "AgendaItem not found";
            }
            Document agendaDoc = agendaIter.next();

            String speechId = new ObjectId().toString();
            Document speechDoc = new Document()
                    .append("_id", speechId)
                    .append("Speech", content)
                    .append("AgendaItemId", agendaItemId)
                    .append("ProtocolID", agendaDoc.getString("ProtocolID"));
            Document speaker = speakerFromId(speakerId);
            if (speaker == null) {
                response.status(404);
                return "Speaker not found";
            }
            speechDoc
                    .append("Speaker's Firstname", speaker.getString("first"))
                    .append("Speaker's Lastname", speaker.getString("last"))
                    .append("SpeakerID", speakerId);
            mongoDb.getCollection("Speeches").insertOne(speechDoc);
            mongoDb.getCollection("AgendaItems").updateOne(Filters.eq("_id", agendaItemId), Updates.inc("Total Number of Speeches", 1));
            db.singleSpeech(speechId);
            response.status(200);
            return new Document().append("id", speechDoc.getString("_id")).toJson();
        });
    }
}
