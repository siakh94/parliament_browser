package uni.project.mongodb.database;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.SerialFormat;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasIOUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.hucompute.textimager.uima.type.Sentiment;
import org.hucompute.textimager.uima.type.category.CategoryCoveredTagged;
import scala.App;
import uni.project.all.*;
import uni.project.all.classes.mongoclasses.*;
import uni.project.parser.FileImport;
import uni.project.parser.NLP;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * This class mainly helps us handle with MongoDb
 * stuff like connection,NLP, updating, aggregation etc...
 *
 * @author Siamak Choromkheirabadi
 */
public class MongoDBConnectionHandler {

    //we define a bunch of MongoDB Classes and reference them to variables
    private MongoDatabase mongoDatabase;

    private PlenaryProtocol protocol_copy;

    private int a = 1;
    private int b = 1;
    private int c = 1;


    /**
     * constructor
     */
    public MongoDBConnectionHandler() {

    }


    /**
     * @return the mongoDataBase
     */
    public MongoDatabase getDb() {
        return mongoDatabase;
    }

    /**
     * takes info from the txt-file and uses them to connect to mongodb
     * if there's no txt.file, manually written data will be used
     *
     * @return the MongoDataBase
     */
    public MongoDatabase connection() {
        try {
            Properties properties = getConnectionCredentials();
            String user = "";
            String db = "";
            char[] pw = "".toCharArray();
            String host = "";
            String port = "";

            // If the prop couldnt be loaded, use the internal credentials then....
            if (properties == null) {
                user = "PRG_WiSe22_Group_2_2";
                db = "PRG_WiSe22_Group_2_2";
                pw = "wh7aQHBJ".toCharArray();
                host = "prg2022.texttechnologylab.org";
                port = "27020";
            } else {
                user = properties.getProperty("remote_user");
                db = properties.getProperty("remote_database");
                pw = properties.getProperty("remote_password").toCharArray();
                host = properties.getProperty("remote_host");
                port = properties.getProperty("remote_port");
            }

            MongoCredential credential = MongoCredential.createCredential(
                    user,
                    db,
                    pw);

            String finalHost = host;
            String finalPort = port;
            MongoClientSettings settings = MongoClientSettings.builder()
                    .credential(credential)
                    .applyToSslSettings(builder -> builder.enabled(false))
                    .applyToClusterSettings(builder ->
                            builder.hosts(Arrays.asList(
                                    new ServerAddress(finalHost,
                                            Integer.parseInt(finalPort)))))
                    .build();

            com.mongodb.client.MongoClient mongoClient = MongoClients.create(settings);
            // Accessing the database
            MongoDatabase database = mongoClient.getDatabase(properties.getProperty("remote_database"));
            System.out.println("Connected to MongoDB !");
            this.mongoDatabase = database;
            return database;
        } catch (Exception ex) {
            return null;
        }
    }


    /**
     * Reads from the config.properties file and returns the values
     *
     * @return properties applied to the txt-file (containing mongoCredidentials)
     */
    private static Properties getConnectionCredentials() {
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("PRG_WiSe22_Group_2_2.txt")) {
            Properties prop = new Properties();
            // load a properties file
            prop.load(input);
            return prop;
        } catch (Exception ex) {
            System.out.println("Couldn't open the properties file, error: " + ex.getMessage());
            System.out.println("Using internal connection.");
            return null;
        }
    }


    //protocol data will be stored on mongoCollection

    /**
     * stores a protocol object internally and also in the MongoDB
     *
     * @param protocol
     */
    public void mongoProtocol(PlenaryProtocol protocol) {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        this.protocol_copy = protocol;
        //changes the default id to a unique id from protocol
        Document mongoDocument_copy = new Document("_id", protocol_copy.getTitle().replaceAll("Plenarprotokoll", ""));
        //creates a field called date and adds the date of the protocol as value
        mongoDocument_copy.put("Date", protocol_copy.getDate());
        //same as above just different stuff
        mongoDocument_copy.put("Starttime", protocol_copy.getBeginTime());
        mongoDocument_copy.put("Endtime", protocol_copy.getEndTime());
        mongoDocument_copy.put("Index", protocol_copy.getIndex());
        mongoDocument_copy.put("Title", protocol_copy.getTitle());
        mongoDocument_copy.put("Place", protocol_copy.getPlace());
        mongoDocument_copy.put("ElectionPeriod", protocol_copy.getElectionPeriod());
        List<String> tops = new ArrayList<>();
        protocol_copy.getAgendaItems().stream().forEach(agendaItem -> {
            //removes Tagesordnungspunkt and Plenarprotokoll from the documents and saves only the index
            if (agendaItem.getIndex().contains("Tagesordnungspunkt")) {
                String a = protocol_copy.getTitle().replaceAll("Plenarprotokoll", "") + agendaItem.
                        getIndex().replaceAll("Tagesordnungspunkt", ";");
                tops.add(a);
            }
            if (agendaItem.getIndex().contains("Zusatzpunkt")) {
                //same as above just for Zusatzpunkt!
                String b = protocol_copy.getTitle().replaceAll("Plenarprotokoll", "") + agendaItem.
                        getIndex().replaceAll("Zusatzpunkt", ";");
                tops.add(b);
            }

        });

        mongoDocument_copy.put("AgendaItems", tops);

        try {
            //a unixTime formatted time of protocols
            Date date = df.parse(protocol_copy.getDate() + " " + protocol_copy.getBeginTime());
            long unixTime = (long) date.getTime() / 1000;

            mongoDocument_copy.put("ExactTime", unixTime);

        } catch (Exception e) {
            //System.out.println("Dude ! MongoDB is already loaded !! ");
        }
        //creates a PlenaryProtocol obj and passes the doc to it
        PlenaryProtocol pp = new PlenaryProtocol_MongoDB_Impl(mongoDocument_copy);
        //adds the modified obj to a list (same as uploading to mongo just this time internally !)
        protocol_copy.getInfo().addMongoProtocol(pp);
        try {
            //uploads the fully modified document to MongoDB
            getMongoCollection("Protocols").insertOne(mongoDocument_copy);

        } catch (Exception e) {
            //System.out.println("Dude ! MongoDB is already loaded !! ");
        }

        //calls the method
        mongoAgendaItem();

    }


    /**
     * stores AgendaItems internally and also in the MongoDB
     */
    public void mongoAgendaItem() {

        //same as with mongoProtocols ... (therefore no comments!)
        protocol_copy.getAgendaItems().stream().forEach(agendaItem -> {

            Document mongoDocument_copy = new Document();

            if (agendaItem.getIndex().contains("Tagesordnungspunkt")) {
                mongoDocument_copy.put("_id", (protocol_copy.getTitle().replaceAll("Plenarprotokoll", "") + agendaItem.
                        getIndex().replaceAll("Tagesordnungspunkt", ";")));
                mongoDocument_copy.put("AgendaIndex", agendaItem.getIndex().replaceAll("Tagesordnungspunkt", ""));
            }
            if (agendaItem.getIndex().contains("Zusatzpunkt")) {
                mongoDocument_copy.put("_id", (protocol_copy.getTitle().replaceAll("Plenarprotokoll", "") + agendaItem.
                        getIndex().replaceAll("Zusatzpunkt", ";")));
                mongoDocument_copy.put("AgendaIndex", agendaItem.getIndex().replaceAll("Zusatzpunkt", ""));
            }

            mongoDocument_copy.put("Title", agendaItem.getIndex());
            mongoDocument_copy.put("ProtocolID", protocol_copy.getTitle().replaceAll("Plenarprotokoll", ""));
            mongoDocument_copy.put("Total Number of Speeches", agendaItem.getSpeeches().size());
            AgendaItem ai = new AgendaItem_MongoDB_Impl(mongoDocument_copy);
            protocol_copy.getInfo().addMongoAgendaItem(ai);
            try {
                getMongoCollection("AgendaItems").insertOne(mongoDocument_copy);
            } catch (Exception e) {
                // System.out.println("Dude ! MongoDB is already loaded !! ");
            }

        });


        mongoSpeech();

    }

    /**
     * stores Speeches internally and also in the MongoDB
     */
    public void mongoSpeech() {

        //same as with mongoProtocols ... (therefore no comments!)
        protocol_copy.getAgendaItems().
                stream().forEach(agendaItem -> agendaItem.getSpeeches().stream().forEach(speech -> {

                    String id = speech.getID();

                    if (!(id.isEmpty())) {
                        if (speech.getText().length() > 4) {
                            Document mongoDocument_copy = new Document("_id", speech.getID());
                            mongoDocument_copy.put("Speech", speech.getText());
                            if (speech.getSpeaker() != null) {
                                mongoDocument_copy.put("SpeakerID", speech.getSpeaker().getID());
                                mongoDocument_copy.put("Speaker's Firstname", speech.getSpeaker().getFirstName());
                                mongoDocument_copy.put("Speaker's Lastname", speech.getSpeaker().getName());
                            }
                            if (agendaItem.getIndex().contains("Tagesordnungspunkt")) {
                                mongoDocument_copy.put("AgendaItemId", (protocol_copy.getTitle().replaceAll("Plenarprotokoll", "") + agendaItem.
                                        getIndex().replaceAll("Tagesordnungspunkt", ";")));
                            }
                            if (agendaItem.getIndex().contains("Zusatzpunkt")) {
                                mongoDocument_copy.put("AgendaItemId", (protocol_copy.getTitle().replaceAll("Plenarprotokoll", "") + agendaItem.
                                        getIndex().replaceAll("Zusatzpunkt", ";")));
                            }

                            mongoDocument_copy.put("ProtocolID", protocol_copy.getTitle().replaceAll("Plenarprotokoll", ""));

                            Speech si = new Speech_MongoDB_Impl(mongoDocument_copy);
                            protocol_copy.getInfo().addMongoSpeech(si);
                            try {
                                getMongoCollection("Speeches").insertOne(mongoDocument_copy);
                            } catch (Exception e) {
                                //  System.out.println("Dude ! MongoDB is already loaded !! ");
                            }


                        }

                    }


                }));


        mongoComment();

    }

    /**
     * stores comments internally and also in the MongoDB
     */
    public void mongoComment() {

        //same as with mongoProtocols ... (therefore no comments!)
        protocol_copy.getAgendaItems().stream().
                forEach(agendaItem -> agendaItem.
                        getSpeeches().stream().forEach(speech -> speech.
                                getComments().stream().forEach(comment -> {
                                    Document mongoDocument_copy = new Document();
                                    if (comment.getComment() != null) {
                                        mongoDocument_copy.put("Comment", comment.getComment());
                                        mongoDocument_copy.put("_id", c);
                                        mongoDocument_copy.put("FakeID", c);
                                        if (comment.getSpeaker() != null) {
                                            mongoDocument_copy.put("Commentator", comment.getSpeaker().getName());
                                            mongoDocument_copy.put("CommentatorID", comment.getSpeaker().getID());

                                        }
                                        if (comment.getSpeech() != null) {
                                            mongoDocument_copy.put("SpeechID", comment.getSpeech().getID());

                                        }

                                    }
                                    c = c + 1;
                                    Comment cm = new Comment_MongoDB_Impl(mongoDocument_copy);
                                    protocol_copy.getInfo().addMongoComment(cm);
                                    try {
                                        getMongoCollection("Comments").insertOne(mongoDocument_copy);
                                    } catch (Exception e) {
                                        // System.out.println("Dude ! MongoDB is already loaded !! ");
                                    }

                                })));


    }

    /**
     * stores speakers internally and also in the MongoDB
     *
     * @param speaker
     */
    public void mongoSpeakers(Speaker speaker) {

        //same as with mongoProtocols ... (therefore no comments!)
        if (speaker.getID() != "") {

            Document mongoDocument_copy = new Document("_id", speaker.getID());
            mongoDocument_copy.put("Title", speaker.getTitle());
            mongoDocument_copy.put("FirstName", speaker.getFirstName());
            mongoDocument_copy.put("LastName", speaker.getName());
            if (speaker.getFraction() != null) {
                protocol_copy.getInfo().getMongoFractions().stream().forEach(fraction -> {
                    if (Objects.equals(speaker.getFraction().getName(), ((Fraction_MongoDB_Impl) fraction).getFraction())) {
                        mongoDocument_copy.put("FractionID", ((Fraction_MongoDB_Impl) fraction).getFractionID());

                    }
                });
                mongoDocument_copy.put("Fraction", speaker.getFraction().getName());
            }
            if (speaker.getParty() != null) {
                protocol_copy.getInfo().getMongoParties().stream().forEach(p -> {
                    if (Objects.equals(speaker.getParty().getName(), ((Party_MongoDB_Impl) p).getParty())) {
                        mongoDocument_copy.put("PartyID", ((Party_MongoDB_Impl) p).getPartyID());

                    }
                });
                mongoDocument_copy.put("Party", speaker.getParty().getName());
            }
            if (speaker.getBirthday() != null) {
                mongoDocument_copy.put("Birthday", speaker.getBirthday());
            }
            if (speaker.getDeath() != null) {
                mongoDocument_copy.put("Death", speaker.getDeath());
            }
            if (speaker.getBirthPlace() != null) {
                mongoDocument_copy.put("PlaceOfBirth", speaker.getBirthPlace());
            }
            if (speaker.getSex() != null) {
                mongoDocument_copy.put("SEX", speaker.getSex());
            }
            if (speaker.getMaritalStatus() != null) {
                mongoDocument_copy.put("MaritalStatus", speaker.getMaritalStatus());
            }
            if (speaker.getReligion() != null) {
                mongoDocument_copy.put("Religion", speaker.getReligion());
            }
            if (speaker.getAcTitle() != null) {
                mongoDocument_copy.put("AcademicTitle", speaker.getAcTitle());
            }
            if (speaker.getOccupation() != null) {
                mongoDocument_copy.put("Occupation", speaker.getOccupation());
            }
            mongoDocument_copy.put("Role", speaker.getRole());
            mongoDocument_copy.put("Speaker's Speech-Length", speaker.getAvgLength());
            mongoDocument_copy.put("IsLeader", speaker.isLeader());


            Speaker sp = new Speaker_MongoDB_Impl(mongoDocument_copy);
            protocol_copy.getInfo().addMongoSpeaker(sp);
            try {
                getMongoCollection("Speakers").insertOne(mongoDocument_copy);
            } catch (Exception e) {
                // System.out.println("Dude ! MongoDB is already loaded !! ");
            }

        }


    }

    /**
     * stores fractions internally and also in the MongoDB
     *
     * @param fraction
     */
    public void mongoFractions(Fraction fraction) {
        //same as with mongoProtocols ... (therefore no comments!)
        Document mongoDocument_copy = new Document("_id", a);
        mongoDocument_copy.put("Fraction", fraction.getName());
        if (fraction.getMembers() != null) {
            mongoDocument_copy.put("Fraction-Members", Arrays.asList(fraction.getMembers().toString().split(",")));
            mongoDocument_copy.put("Total number of Members", fraction.getMembers().size());
        }
        Fraction fm = new Fraction_MongoDB_Impl(mongoDocument_copy);
        protocol_copy.getInfo().addMongoFraction(fm);
        try {
            getMongoCollection("Fractions").insertOne(mongoDocument_copy);
        } catch (Exception e) {
            //System.out.println("Dude ! MongoDB is already loaded !! ");
        }
        a = a + 1;

    }

    /**
     * stores parties internally and also in the MongoDB
     *
     * @param party
     */
    public void mongoParties(Party party) {
        //same as with mongoProtocols ... (therefore no comments!)
        Document mongoDocument_copy = new Document("_id", b);
        mongoDocument_copy.put("Party", party.getName());
        if (party.getMembers() != null) {
            mongoDocument_copy.put("Party-Members", Arrays.asList(party.getMembers().toString().split(",")));
            mongoDocument_copy.put("Total number of Members", party.getMembers().size());
        }
        Party pm = new Party_MongoDB_Impl(mongoDocument_copy);
        protocol_copy.getInfo().addMongoParty(pm);
        try {
            getMongoCollection("Parties").insertOne(mongoDocument_copy);
        } catch (Exception e) {
            // System.out.println("Dude ! MongoDB is already loaded !! ");
        }
        b = b + 1;

    }

    /**
     * stores photos in the MongoDB
     *
     * @throws IOException
     */
    public void PhotoAndData() throws IOException {

        try {
            //iterates through already downloaded photos and takes for each one the doc
            FileImport.MPs().stream().forEach(document -> {
                //gets the value of the field called FullName and splits them by comma
                String[] s = document.getString("FullName").split(",");
                String first = "";
                //removes dr. from the string
                if (s[1].contains(" Dr. ")) {
                    first = s[1].replaceAll(" Dr. ", "");

                } else {
                    //removes extra space
                    first = s[1].replace(" ", "");


                }
                //iterates through documents in collection Speaker in `mongoDB
                //and checks if the firstname and last names match, if so, give me ID!
                FindIterable findIterable = mongoDatabase.getCollection("Speakers").
                        find(new Document("LastName", " " + s[0]));
                MongoCursor<Document> mongoCursor = findIterable.iterator();
                String finalFirst = first;
                mongoCursor.forEachRemaining(doc -> {

                    if (doc != null && (Objects.equals(doc.getString("FirstName"), finalFirst))) {
                        String id = doc.getString("_id");
                        //stores the specific SpeakerID in the document
                        document.put("SpeakerID", id);

                    }


                });

                try {
                    //uploads the document to a "to be created collection" named photos
                    getMongoCollection("Photos").insertOne(document);
                } catch (Exception e) {
                    //System.out.println("Dude ! MongoDB is already loaded !! ");
                }


            });

        } catch (Exception exception) {
            // System.out.println("Dude ! MongoDB is already loaded !!!!!!!!!");
        }

    }


    //Comments on the way to mongo!


    /**
     * gives us the desired MongoCollection
     *
     * @param newCollection
     * @return
     */
    //helps us create a specific collection (unique name) and not the default collection
    public MongoCollection<Document> getMongoCollection(String newCollection) {
        return this.mongoDatabase.getCollection(newCollection);
    }

    //helps us update stuff on MongoDB, ex.: changing values

    /**
     * updates stuff in MongoDB (Collections, Fields and Values)
     *
     * @param collectionName
     * @param fieldName
     * @param currentValue
     * @param wantedValue
     */
    public void update(String collectionName, String fieldName, String currentValue, String wantedValue) {
        //iterates through the MongoDbDocuments in the given MongoDBCollection
        FindIterable findIterable = mongoDatabase.getCollection(collectionName).
                find(new Document(fieldName, currentValue));
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        mongoCursor.forEachRemaining(doc -> {
            //if doc isn't null, it replaces the old field and value with the new ones

            if (doc != null) {
                Bson bson = new Document(fieldName, wantedValue);
                Bson changed = new Document("$set", bson);
                getMongoCollection(collectionName).updateOne(doc, changed);
            } else {
                System.out.println("Dude! There's no such thing there :-D");
            }

        });

    }

    /**
     * deletes stuff in MongoDB (Collections, Fields and Values)
     *
     * @param collectionName
     * @param fieldName
     * @param currentValue
     */

    public void delete(String collectionName, String fieldName, String currentValue) {

        //iterates through the MongoDbDocuments in the given MongoDBCollection
        FindIterable findIterable = mongoDatabase.getCollection(collectionName).
                find(new Document(fieldName, currentValue));
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        mongoCursor.forEachRemaining(doc -> {
            //if not null then delete the wanted document
            if (doc != null) {
                Bson bson = new Document(fieldName, currentValue);
                getMongoCollection(collectionName).deleteOne(bson);
            } else {
                System.out.println("Dude! There's no such thing there :-D");
            }

        });

    }

    /**
     * creates stuff in MongoDB (Collections, Fields and Values)
     *
     * @param collection
     * @param field
     * @param value
     */
    public void create(String collection, String field, String value) {
        //creates a new collection or new fields and values
        Document document = new Document(field, value);
        getMongoCollection(collection).insertOne(document);
    }

    /**
     * reads DDC3 csv-file and converts numbers to their related Strings (names)
     *
     * @param file
     * @param separator
     * @return
     * @throws FileNotFoundException
     */
    private Properties readPropertyFile(File file, String separator) throws FileNotFoundException {
        Properties properties = new Properties();
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] keyValue = line.split(separator);
            Iterator<String> places = Arrays.stream(keyValue).iterator();
            String key = places.next();
            ArrayList<String> list = new ArrayList<>();
            places.forEachRemaining(list::add);
            properties.setProperty(key, String.join(separator, list));
        }
        return properties;

    }

    /**
     * analyses only one desired speech through NLP and eventually stores it in MongoDB
     *
     * @param id
     */

    public void singleSpeech(String id) {
        //same process as nlpPipeline

        // Create a pipeline
        NLP engine = new NLP();
        engine.initPipeline();


        System.out.println("Starting the NLP-Engine !!!!!!!");


        Bson bsonTest = new Document("_id", id);

        Document doc = mongoDatabase.getCollection("Speeches").find().filter(bsonTest).first();

        // Get a speech from the database
        Document speechDoc = doc;

        // Build an empty jcas object
        JCas speechCas = null;
        try {
            speechCas = JCasFactory.createText(doc.getString("Speech"), "de");
        } catch (UIMAException e) {
            throw new RuntimeException(e);
        }
        // Run it through the pipeline - now its analysed!
        try {
            SimplePipeline.runPipeline(speechCas, engine.getAggregateBuilder().createAggregate());
        } catch (AnalysisEngineProcessException e) {
            throw new RuntimeException(e);
        } catch (ResourceInitializationException e) {
            throw new RuntimeException(e);
        }

        // Save the analysed cas object in the database, so we don't have to do that again
        String jCasAsString = null;
        try {
            jCasAsString = serializeCasToXml(speechCas);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Document document = new Document();
        document.put("_id", speechDoc.getString("_id"));
        document.put("SpeakerID", speechDoc.getString("SpeakerID"));
        document.put("SpeechCas", jCasAsString);
        document.put("Persons", "");
        document.put("Locations", "");
        document.put("Organisations", "");
        document.put("POS", "");
        document.put("Avg-Sentiment", 0);
        document.put("LemmaValue", "");
        document.put("CoveredText", "");
        document.put("CoarseValue", "");
        document.put("Nouns", "");
        document.put("Verbs", "");
        document.put("DDC3", "");
        document.put("DDCValue", "");
        document.put("DDCScore", "");

        try {
            mongoDatabase.getCollection("testing").insertOne(document);
            //SpeechNLP


        } catch (Exception e) {
            //System.out.println("Dude ! MongoDB is already loaded !! ");
        }


        Document d = mongoDatabase.getCollection("testing").find().filter(bsonTest).first();


        // Get the casXML from the db again and build the jcas from it again!
        String casXml = d.getString("SpeechCas");
        JCas emptyJcas = null;
        try {
            emptyJcas = JCasFactory.createJCas();
        } catch (UIMAException e) {
            throw new RuntimeException(e);
        }
        try {
            deserializeCasFromXml(casXml, emptyJcas);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        Collection<String> per = new ArrayList<>(0);
        Collection<String> loc = new ArrayList<>(0);
        Collection<String> org = new ArrayList<>(0);
        Collection<String> posList = new ArrayList<>(0);
        Collection<Double> sentiments = new ArrayList<>(0);
        Collection<String> lemmaValue = new ArrayList<>(0);
        Collection<String> coarseValue = new ArrayList<>(0);
        Collection<String> coveredText = new ArrayList<>(0);
        Collection<String> nouns = new ArrayList<>(0);
        Collection<String> verbs = new ArrayList<>(0);
        Collection<String> ddc3 = new ArrayList<>(0);
        Collection<Double> ddcScore = new ArrayList<>(0);
        Collection<String> ddcValue = new ArrayList<>(0);
        double dd = 0.0;
        double avg = 0.0;


        Collection<CategoryCoveredTagged> ddcs = JCasUtil.select(emptyJcas, CategoryCoveredTagged.class);
        for (CategoryCoveredTagged ddc : ddcs) {
            ddcScore.add(ddc.getScore());
            ddcValue.add(ddc.getValue());
            try {
                Properties p = readPropertyFile(new File("src/main/resources/ddc3-names-de.csv"), "\t");
                ddc3.add(p.getProperty(ddc.getValue().replaceAll("__label_ddc__", "")));

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

        }


        // Get all annotations we need from the cas!
        for (Annotation annotation : JCasUtil.select(emptyJcas, Annotation.class)) {

            if (annotation instanceof NamedEntity) {
                NamedEntity namedEntity = ((NamedEntity) annotation);
                if ((namedEntity.getValue().equals("PER")) || ((namedEntity.getCoveredText().equals("PER")))) {
                    per.add(namedEntity.getCoveredText());

                }
                if ((namedEntity.getValue().equals("LOC")) || ((namedEntity.getCoveredText().equals("LOC")))) {
                    loc.add(namedEntity.getCoveredText());

                }
                if ((namedEntity.getValue().equals("ORG")) || ((namedEntity.getCoveredText().equals("ORG")))) {
                    org.add(namedEntity.getCoveredText());


                }

            }

            if (annotation instanceof POS) {
                POS pos = ((POS) annotation);
                if (!(pos.getPosValue().contains("$"))) {
                    posList.add(pos.getPosValue());

                }


            }


            if (annotation instanceof Sentiment) {
                Sentiment sentiment = ((Sentiment) annotation);
                dd = dd + sentiment.getSentiment();
                sentiments.add(sentiment.getSentiment());


            }

            if (annotation instanceof Token) {
                Token token = ((Token) annotation);
                lemmaValue.add(token.getLemmaValue());
                coveredText.add(token.getCoveredText());
                coarseValue.add(token.getPos().getCoarseValue());
                if (token.getPos().getCoarseValue().contains("NOUN")) {
                    nouns.add(token.getLemmaValue());

                }
                if (token.getPos().getCoarseValue().contains("VERB")) {
                    verbs.add(token.getLemmaValue());

                }


            }


        }

        avg = dd / sentiments.size();

        List<Bson> bsons = new ArrayList<>();
        Bson bson = new Document("Persons", per);
        Bson changed = new Document("$set", bson);
        bsons.add(changed);

        Bson bson2 = new Document("Locations", loc);
        Bson changed2 = new Document("$set", bson2);
        bsons.add(changed2);

        Bson bson3 = new Document("Organisations", org);
        Bson changed3 = new Document("$set", bson3);
        bsons.add(changed3);

        Bson bson4 = new Document("POS", posList);
        Bson changed4 = new Document("$set", bson4);
        bsons.add(changed4);

        Bson bson5 = new Document("Avg-Sentiment", avg);
        Bson changed5 = new Document("$set", bson5);
        bsons.add(changed5);

        Bson bson6 = new Document("LemmaValue", lemmaValue);
        Bson changed6 = new Document("$set", bson6);
        bsons.add(changed6);

        Bson bson7 = new Document("CoveredText", coveredText);
        Bson changed7 = new Document("$set", bson7);
        bsons.add(changed7);

        Bson bson8 = new Document("CoarseValue", coarseValue);
        Bson changed8 = new Document("$set", bson8);
        bsons.add(changed8);

        Bson bson9 = new Document("Nouns", nouns);
        Bson changed9 = new Document("$set", bson9);
        bsons.add(changed9);

        Bson bson10 = new Document("Verbs", verbs);
        Bson changed10 = new Document("$set", bson10);
        bsons.add(changed10);

        Bson bson11 = new Document("DDC3", ddc3);
        Bson changed11 = new Document("$set", bson11);
        bsons.add(changed11);

        Bson bson12 = new Document("DDCValue", ddcValue);
        Bson changed12 = new Document("$set", bson12);
        bsons.add(changed12);

        Bson bson13 = new Document("DDCScore", ddcScore);
        Bson changed13 = new Document("$set", bson13);
        bsons.add(changed13);

        try {

            mongoDatabase.getCollection("testing").updateMany(d, bsons);

        } catch (Exception e) {
            //System.out.println("Dude ! MongoDB is already loaded !! ");
        }


    }


    /**
     * this Method takes all Speech and comment objects and analyses them all
     * through NLP and stores them all in mongoDB
     */
    public void nlpPipeline() {


        // Create a pipeline
        NLP engine = new NLP();
        engine.initPipeline();


        System.out.println("Starting the NLP-Engine !!!!!!!");


        protocol_copy.getInfo().getMongoSpeeches().stream().forEach(speech -> {

            // Get a speechDoc internally from the Speech_Mongo_Impl
            Document speechDoc = ((Speech_MongoDB_Impl) speech).getDoc();

            // Build an empty jcas object
            JCas speechCas = null;
            try {
                speechCas = JCasFactory.createText(((Speech_MongoDB_Impl) speech).getDoc().getString("Speech"), "de");
            } catch (UIMAException e) {
                throw new RuntimeException(e);
            }
            // Run it through the pipeline - now its analysed!
            try {
                SimplePipeline.runPipeline(speechCas, engine.getAggregateBuilder().createAggregate());
            } catch (AnalysisEngineProcessException e) {
                throw new RuntimeException(e);
            } catch (ResourceInitializationException e) {
                throw new RuntimeException(e);
            }

            // Save the analysed cas object in the database, so we don't have to do that again
            String jCasAsString = null;
            try {
                jCasAsString = serializeCasToXml(speechCas);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Document document = new Document();
            document.put("_id", speechDoc.getString("_id"));
            document.put("SpeakerID", speechDoc.getString("SpeakerID"));
            document.put("SpeechCas", jCasAsString);
            document.put("Persons", "");
            document.put("Locations", "");
            document.put("Organisations", "");
            document.put("POS", "");
            document.put("Avg-Sentiment", 0);
            document.put("LemmaValue", "");
            document.put("CoveredText", "");
            document.put("CoarseValue", "");
            document.put("Nouns", "");
            document.put("Verbs", "");
            document.put("DDC3", "");
            document.put("DDCValue", "");
            document.put("DDCScore", "");

            try {
                mongoDatabase.getCollection("SpeechNLP").insertOne(document);


            } catch (Exception e) {
                //System.out.println("Dude ! MongoDB is already loaded !! ");
            }


        });

        //iterates through the MongoDbDocuments in the given MongoDBCollection
        FindIterable findIterable2 = mongoDatabase.getCollection("SpeechNLP").find();
        MongoCursor<Document> mongoCursor2 = findIterable2.iterator();
        mongoCursor2.forEachRemaining(d -> {

            // Get the casXML from the db again and build the jcas from it again!
            String casXml = d.getString("SpeechCas");
            JCas emptyJcas = null;
            try {
                emptyJcas = JCasFactory.createJCas();
            } catch (UIMAException e) {
                throw new RuntimeException(e);
            }
            try {
                //deserializes the SpeechNLP documents
                deserializeCasFromXml(casXml, emptyJcas);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            Collection<String> per = new ArrayList<>(0);
            Collection<String> loc = new ArrayList<>(0);
            Collection<String> org = new ArrayList<>(0);
            Collection<String> posList = new ArrayList<>(0);
            Collection<Double> sentiments = new ArrayList<>(0);
            Collection<String> lemmaValue = new ArrayList<>(0);
            Collection<String> coarseValue = new ArrayList<>(0);
            Collection<String> coveredText = new ArrayList<>(0);
            Collection<String> nouns = new ArrayList<>(0);
            Collection<String> verbs = new ArrayList<>(0);
            Collection<String> ddc3 = new ArrayList<>(0);
            Collection<Double> ddcScore = new ArrayList<>(0);
            Collection<String> ddcValue = new ArrayList<>(0);
            double dd = 0.0;
            double avg = 0.0;


            Collection<CategoryCoveredTagged> ddcs = JCasUtil.select(emptyJcas, CategoryCoveredTagged.class);
            //iterates through DDCs and gets the score and value
            for (CategoryCoveredTagged ddc : ddcs) {
                ddcScore.add(ddc.getScore());
                ddcValue.add(ddc.getValue());
                try {
                    //gives the names of related numbers
                    Properties p = readPropertyFile(new File("src/main/resources/ddc3-names-de.csv"), "\t");
                    ddc3.add(p.getProperty(ddc.getValue().replaceAll("__label_ddc__", "")));

                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

            }


            // Get all annotations we need from the cas!
            for (Annotation annotation : JCasUtil.select(emptyJcas, Annotation.class)) {

                if (annotation instanceof NamedEntity) {
                    NamedEntity namedEntity = ((NamedEntity) annotation);
                    //gets NamedEntity PER
                    if ((namedEntity.getValue().equals("PER")) || ((namedEntity.getCoveredText().equals("PER")))) {
                        per.add(namedEntity.getCoveredText());

                    }
                    //gets NamedEntity LOC
                    if ((namedEntity.getValue().equals("LOC")) || ((namedEntity.getCoveredText().equals("LOC")))) {
                        loc.add(namedEntity.getCoveredText());

                    }
                    //gets NamedEntity ORG
                    if ((namedEntity.getValue().equals("ORG")) || ((namedEntity.getCoveredText().equals("ORG")))) {
                        org.add(namedEntity.getCoveredText());


                    }

                }

                if (annotation instanceof POS) {
                    POS pos = ((POS) annotation);
                    //saves pos if it has "$"
                    if (!(pos.getPosValue().contains("$"))) {
                        posList.add(pos.getPosValue());

                    }


                }


                if (annotation instanceof Sentiment) {
                    //saves the sentiment
                    Sentiment sentiment = ((Sentiment) annotation);
                    dd = dd + sentiment.getSentiment();
                    sentiments.add(sentiment.getSentiment());


                }

                if (annotation instanceof Token) {
                    //saves the token
                    Token token = ((Token) annotation);
                    //lists will be loaded
                    lemmaValue.add(token.getLemmaValue());
                    coveredText.add(token.getCoveredText());
                    coarseValue.add(token.getPos().getCoarseValue());
                    if (token.getPos().getCoarseValue().contains("NOUN")) {
                        nouns.add(token.getLemmaValue());

                    }
                    if (token.getPos().getCoarseValue().contains("VERB")) {
                        verbs.add(token.getLemmaValue());

                    }


                }


            }

            //gives us the average of sentiments
            avg = dd / sentiments.size();

            //several bsons for updating the DB
            List<Bson> bsons = new ArrayList<>();
            Bson bson = new Document("Persons", per);
            Bson changed = new Document("$set", bson);
            bsons.add(changed);

            Bson bson2 = new Document("Locations", loc);
            Bson changed2 = new Document("$set", bson2);
            bsons.add(changed2);

            Bson bson3 = new Document("Organisations", org);
            Bson changed3 = new Document("$set", bson3);
            bsons.add(changed3);

            Bson bson4 = new Document("POS", posList);
            Bson changed4 = new Document("$set", bson4);
            bsons.add(changed4);

            Bson bson5 = new Document("Avg-Sentiment", avg);
            Bson changed5 = new Document("$set", bson5);
            bsons.add(changed5);

            Bson bson6 = new Document("LemmaValue", lemmaValue);
            Bson changed6 = new Document("$set", bson6);
            bsons.add(changed6);

            Bson bson7 = new Document("CoveredText", coveredText);
            Bson changed7 = new Document("$set", bson7);
            bsons.add(changed7);

            Bson bson8 = new Document("CoarseValue", coarseValue);
            Bson changed8 = new Document("$set", bson8);
            bsons.add(changed8);

            Bson bson9 = new Document("Nouns", nouns);
            Bson changed9 = new Document("$set", bson9);
            bsons.add(changed9);

            Bson bson10 = new Document("Verbs", verbs);
            Bson changed10 = new Document("$set", bson10);
            bsons.add(changed10);

            Bson bson11 = new Document("DDC3", ddc3);
            Bson changed11 = new Document("$set", bson11);
            bsons.add(changed11);

            Bson bson12 = new Document("DDCValue", ddcValue);
            Bson changed12 = new Document("$set", bson12);
            bsons.add(changed12);

            Bson bson13 = new Document("DDCScore", ddcScore);
            Bson changed13 = new Document("$set", bson13);
            bsons.add(changed13);

            try {
                //The documents of the collection SpeechNLP will be replaced by the given bsons
                mongoDatabase.getCollection("SpeechNLP").updateMany(d, bsons);

            } catch (Exception e) {
                //System.out.println("Dude ! MongoDB is already loaded !! ");
            }


        });

        //same as by speeches...
        protocol_copy.getInfo().getMongoComments().stream().forEach(comment -> {

            // Get a speech from the database
            Document commentDoc = ((Comment_MongoDB_Impl) comment).getDoc();

            // Build an empty jcas object
            JCas commentCas = null;
            try {
                commentCas = JCasFactory.createText(((Comment_MongoDB_Impl) comment).getDoc().getString("Comment"), "de");
            } catch (UIMAException e) {
                throw new RuntimeException(e);
            }
            // Run it through the pipeline - now its analysed!
            try {
                SimplePipeline.runPipeline(commentCas, engine.getAggregateBuilder().createAggregate());
            } catch (AnalysisEngineProcessException e) {
                throw new RuntimeException(e);
            } catch (ResourceInitializationException e) {
                throw new RuntimeException(e);
            }

            // Save the analysed cas object in the database, so we don't have to do that again
            String jCasAsString = null;
            try {
                jCasAsString = serializeCasToXml(commentCas);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Document document = new Document();
            document.put("_id", commentDoc.getInteger("_id"));
            document.put("SpeakerID", commentDoc.getString("CommentatorID"));
            document.put("CommentCas", jCasAsString);
            document.put("Persons", "");
            document.put("Locations", "");
            document.put("Organisations", "");
            document.put("POS", "");
            document.put("Avg-Sentiment", 0);
            document.put("LemmaValue", "");
            document.put("CoveredText", "");
            document.put("CoarseValue", "");
            document.put("Nouns", "");
            document.put("Verbs", "");
            document.put("DDC3", "");
            document.put("DDCValue", "");
            document.put("DDCScore", "");

            try {

                mongoDatabase.getCollection("CommentNLP").insertOne(document);

            } catch (Exception e) {
                //System.out.println("Dude ! MongoDB is already loaded !! ");
            }


        });

        FindIterable findIterable4 = mongoDatabase.getCollection("CommentNLP").find();
        MongoCursor<Document> mongoCursor4 = findIterable4.iterator();
        mongoCursor4.forEachRemaining(d -> {

            // Get the casXML from the db again and build the jcas from it again!
            String casXml = d.getString("CommentCas");
            JCas emptyJcas = null;
            try {
                emptyJcas = JCasFactory.createJCas();
            } catch (UIMAException e) {
                throw new RuntimeException(e);
            }
            try {
                deserializeCasFromXml(casXml, emptyJcas);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            Collection<String> per = new ArrayList<>(0);
            Collection<String> loc = new ArrayList<>(0);
            Collection<String> org = new ArrayList<>(0);
            Collection<String> posList = new ArrayList<>(0);
            Collection<Double> sentiments = new ArrayList<>(0);
            Collection<String> lemmaValue = new ArrayList<>(0);
            Collection<String> coarseValue = new ArrayList<>(0);
            Collection<String> coveredText = new ArrayList<>(0);
            Collection<String> nouns = new ArrayList<>(0);
            Collection<String> verbs = new ArrayList<>(0);
            Collection<String> ddc3 = new ArrayList<>(0);
            Collection<Double> ddcScore = new ArrayList<>(0);
            Collection<String> ddcValue = new ArrayList<>(0);

            double dd = 0.0;
            double avg = 0.0;

            Collection<CategoryCoveredTagged> ddcs = JCasUtil.select(emptyJcas, CategoryCoveredTagged.class);
            for (CategoryCoveredTagged ddc : ddcs) {
                ddcScore.add(ddc.getScore());
                ddcValue.add(ddc.getValue());
                try {
                    Properties p = readPropertyFile(new File("src/main/resources/ddc3-names-de.csv"), "\t");
                    ddc3.add(p.getProperty(ddc.getValue().replaceAll("__label_ddc__", "")));

                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

            }


            // Get all annotations we need from the cas!
            for (Annotation annotation : JCasUtil.select(emptyJcas, Annotation.class)) {

                if (annotation instanceof NamedEntity) {
                    NamedEntity namedEntity = ((NamedEntity) annotation);
                    if ((namedEntity.getValue().equals("PER")) || ((namedEntity.getValue().equals("PER")))) {
                        per.add(namedEntity.getCoveredText());

                    }
                    if ((namedEntity.getValue().equals("LOC")) || ((namedEntity.getCoveredText().equals("LOC")))) {
                        loc.add(namedEntity.getCoveredText());

                    }
                    if ((namedEntity.getValue().equals("ORG")) || ((namedEntity.getCoveredText().equals("ORG")))) {
                        org.add(namedEntity.getCoveredText());


                    }

                }

                if (annotation instanceof POS) {
                    POS pos = ((POS) annotation);
                    if (!(pos.getPosValue().contains("$"))) {
                        posList.add(pos.getPosValue());

                    }


                }


                if (annotation instanceof Sentiment) {
                    Sentiment sentiment = ((Sentiment) annotation);
                    dd = dd + sentiment.getSentiment();
                    sentiments.add(sentiment.getSentiment());


                }

                if (annotation instanceof Token) {
                    Token token = ((Token) annotation);
                    lemmaValue.add(token.getLemmaValue());
                    coveredText.add(token.getCoveredText());
                    coarseValue.add(token.getPos().getCoarseValue());
                    if (token.getPos().getCoarseValue().contains("NOUN")) {
                        nouns.add(token.getLemmaValue());

                    }
                    if (token.getPos().getCoarseValue().contains("VERB")) {
                        verbs.add(token.getLemmaValue());

                    }


                }


            }

            avg = dd / sentiments.size();

            List<Bson> bsons = new ArrayList<>();
            Bson bson = new Document("Persons", per);
            Bson changed = new Document("$set", bson);
            bsons.add(changed);

            Bson bson2 = new Document("Locations", loc);
            Bson changed2 = new Document("$set", bson2);
            bsons.add(changed2);

            Bson bson3 = new Document("Organisations", org);
            Bson changed3 = new Document("$set", bson3);
            bsons.add(changed3);

            Bson bson4 = new Document("POS", posList);
            Bson changed4 = new Document("$set", bson4);
            bsons.add(changed4);

            Bson bson5 = new Document("Avg-Sentiment", avg);
            Bson changed5 = new Document("$set", bson5);
            bsons.add(changed5);

            Bson bson6 = new Document("LemmaValue", lemmaValue);
            Bson changed6 = new Document("$set", bson6);
            bsons.add(changed6);

            Bson bson7 = new Document("CoveredText", coveredText);
            Bson changed7 = new Document("$set", bson7);
            bsons.add(changed7);

            Bson bson8 = new Document("CoarseValue", coarseValue);
            Bson changed8 = new Document("$set", bson8);
            bsons.add(changed8);

            Bson bson9 = new Document("Nouns", nouns);
            Bson changed9 = new Document("$set", bson9);
            bsons.add(changed9);

            Bson bson10 = new Document("Verbs", verbs);
            Bson changed10 = new Document("$set", bson10);
            bsons.add(changed10);

            Bson bson11 = new Document("DDC3", ddc3);
            Bson changed11 = new Document("$set", bson11);
            bsons.add(changed11);

            Bson bson12 = new Document("DDCValue", ddcValue);
            Bson changed12 = new Document("$set", bson12);
            bsons.add(changed12);

            Bson bson13 = new Document("DDCScore", ddcScore);
            Bson changed13 = new Document("$set", bson13);
            bsons.add(changed13);

            try {

                mongoDatabase.getCollection("CommentNLP").updateMany(d, bsons);

            } catch (Exception e) {
                //System.out.println("Dude ! MongoDB is already loaded !! ");
            }


        });


    }


    /**
     * Taken from the live coding tut 08.12.22
     *
     * @return
     * @throws IOException
     */
    public static String serializeCasToXml(JCas jCas) throws IOException {
        CAS cas = jCas.getCas();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CasIOUtils.save(cas, outputStream, SerialFormat.XMI);
        return new String(outputStream.toByteArray());
    }

    /**
     * Taken from the live coding tut 08.12.22
     *
     * @return
     * @throws IOException
     */
    public static JCas deserializeCasFromXml(String xml, JCas emptyCas) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        CasIOUtils.load(inputStream, emptyCas.getCas());
        inputStream.close();
        return emptyCas;
    }

    /**
     * prints all Lemmatas in MongoDB (SpeechNLP or CommentNLP)
     *
     * @param col
     */
    public void lemmata(String col) {

        Collection<String> verbs = new ArrayList<>();
        Collection<String> nouns = new ArrayList<>();

        FindIterable findIterable = mongoDatabase.getCollection(col).find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        mongoCursor.forEachRemaining(d -> {
            verbs.addAll(d.getList("Verbs", String.class));

        });

        FindIterable findIterable2 = mongoDatabase.getCollection(col).find();
        MongoCursor<Document> mongoCursor2 = findIterable2.iterator();
        mongoCursor2.forEachRemaining(d -> {
            nouns.addAll(d.getList("Nouns", String.class));

        });

        if (col.equals("SpeechNLP")) {
            System.out.println("Nouns in Speeches: " + nouns + "\n");
            System.out.println();
            System.out.println("Verbs in Speeches: " + verbs + "\n");
        }
        if (col.equals("CommentNLP")) {
            System.out.println("Nouns in Comments : " + nouns + "\n");
            System.out.println();
            System.out.println("Verbs in Comments : " + verbs + "\n");
        }


    }

    /**
     * prints all entities in MongoDB (SpeechNLP or CommentNLP)
     *
     * @param col
     */
    public void entities(String col) {

        Collection<String> persons = new ArrayList<>();
        Collection<String> locations = new ArrayList<>();
        Collection<String> organisations = new ArrayList<>();

        FindIterable findIterable = mongoDatabase.getCollection(col).find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        mongoCursor.forEachRemaining(d -> {
            persons.addAll(d.getList("Persons", String.class));

        });

        FindIterable findIterable2 = mongoDatabase.getCollection(col).find();
        MongoCursor<Document> mongoCursor2 = findIterable2.iterator();
        mongoCursor2.forEachRemaining(d -> {
            locations.addAll(d.getList("Locations", String.class));

        });

        FindIterable findIterable3 = mongoDatabase.getCollection(col).find();
        MongoCursor<Document> mongoCursor3 = findIterable3.iterator();
        mongoCursor3.forEachRemaining(d -> {
            organisations.addAll(d.getList("Organisations", String.class));


        });

        if (col.equals("SpeechNLP")) {
            System.out.println("Persons in Speeches : \n" + persons);
            System.out.println();
            System.out.println("Locations in Speeches: \n" + locations);
            System.out.println();
            System.out.println("Organisations in Speeches: \n" + organisations);
            System.out.println();
        }
        if (col.equals("CommentNLP")) {
            System.out.println("Persons in Comments: \n" + persons);
            System.out.println();
            System.out.println("Locations Comments: \n" + locations);
            System.out.println();
            System.out.println("Organisations Comments: \n" + organisations);
            System.out.println();
        }


    }

    /**
     * prints all sentiments in MongoDB (SpeechNLP or CommentNLP)
     *
     * @param col
     */
    public void sentis(String col) {
        Collection<Double> doubles = new ArrayList<>();
        FindIterable findIterable = mongoDatabase.getCollection(col).find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        mongoCursor.forEachRemaining(d -> {
            doubles.add((Double) d.get("Avg-Sentiment"));
        });

        double dd = 0.0;
        for (double d : doubles) {
            dd = dd + d;
        }
        double avg = dd / doubles.size();

        if (col.equals("SpeechNLP")) {
            System.out.println("Average sentiment of all speeches:  " + avg);
        }
        if (col.equals("CommentNLP")) {
            System.out.println("Average sentiment of all Comments:  " + avg);
        }


    }


}
