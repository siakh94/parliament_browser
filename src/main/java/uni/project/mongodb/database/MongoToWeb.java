package uni.project.mongodb.database;


import com.google.gson.Gson;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import freemarker.template.Configuration;
import org.bson.Document;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;
import uni.project.all.Comment;
import uni.project.all.InfoGetter;
import uni.project.all.Speech;
import uni.project.all.classes.InfoGetter_File_Impl;
import uni.project.all.classes.mongoclasses.Comment_MongoDB_NLP;
import uni.project.all.classes.mongoclasses.Speech_MongoDB_NLP;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static spark.Spark.get;

public class MongoToWeb {
    private MongoDBConnectionHandler db;
    private InfoGetter info;

    public MongoToWeb(MongoDBConnectionHandler db, InfoGetter info) {
        this.info = info;
        this.db = db;
        init();

    }


    private static Configuration configuration = Configuration.getDefaultConfiguration();


    private void init() {


        if (((InfoGetter_File_Impl) info).getNLPSpeeches().size() < 3) {

            FindIterable nlpFindIterable1 = db.getMongoCollection("SpeechNLP").find();
            MongoCursor<Document> nlpMongoCursor1 = nlpFindIterable1.iterator();
            nlpMongoCursor1.forEachRemaining(document -> {
                Speech analyzedSpeeches = new Speech_MongoDB_NLP(document);
                ((InfoGetter_File_Impl) info).addNLPSpeech(analyzedSpeeches);

            });
        }


        if (((InfoGetter_File_Impl) info).getNLPComments().size() < 3) {

            FindIterable nlpFindIterable2 = db.getMongoCollection("CommentNLP").find();
            MongoCursor<Document> nlpMongoCursor2 = nlpFindIterable2.iterator();
            nlpMongoCursor2.forEachRemaining(document -> {
                Comment analyzedComments = new Comment_MongoDB_NLP(document);
                ((InfoGetter_File_Impl) info).addNLPComment(analyzedComments);

            });
        }



        ArrayList<String> speakers = new ArrayList<>();

        ((InfoGetter_File_Impl) info).getMongoSpeakers().stream().forEach(speaker -> {
            speakers.add(speaker.getName());

        });


        Map<Integer, Double> commentSentiment = new HashMap<>();

        ((InfoGetter_File_Impl) info).getNLPComments().stream().forEach(c -> {

            commentSentiment.
                    put(((Comment_MongoDB_NLP) c).
                            getDoc().get("_id", Integer.class), ((Comment_MongoDB_NLP) c).getDoc().
                            get("Avg-Sentiment", Double.class));

        });

        Map<String, Double> speechSentiment = new HashMap<>();
        Collection<String> pers = new ArrayList<>();
        Collection<String> locs = new ArrayList<>();
        Collection<String> orgs = new ArrayList<>();

        ((InfoGetter_File_Impl) info).getNLPSpeeches().stream().forEach(s -> {

            speechSentiment.
                    put(((Speech_MongoDB_NLP) s).
                            getDoc().get("_id", String.class), ((Speech_MongoDB_NLP) s).getDoc().
                            get("Avg-Sentiment", Double.class));

            pers.addAll(((Speech_MongoDB_NLP) s).
                    getDoc().getList("Persons", String.class));

            locs.addAll(((Speech_MongoDB_NLP) s).
                    getDoc().getList("Locations", String.class));

            orgs.addAll(((Speech_MongoDB_NLP) s).
                    getDoc().getList("Organisations", String.class));

        });


        System.out.println("transferring Data to Web ! ");


        // Set the folder for our template files
        try {
            configuration.setDirectoryForTemplateLoading(new File("templates/"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        //URL for all speeches
        get("/speech", (request, response) -> {

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("title", "All Speeches : ");
            attributes.put("speeches", ((InfoGetter_File_Impl) info).getMongoSpeeches().toString());


            return new ModelAndView(attributes, "index.ftl");
        }, new FreeMarkerEngine(configuration));

        get("/speakers/count", (request, response) -> {
            Gson gson = new Gson();
            List<Document> docs = new ArrayList<>();

            Map<String, Long> countSpeakers;


            countSpeakers = speakers.stream().
                    collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            Map<String, Long> result = countSpeakers.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(3)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            result.forEach((name, count) -> {
                Document doc = new Document();
                doc.put("name", name);
                doc.put("count", count);
                docs.add(doc);
            });


            return gson.toJson(docs);
        });


        //URL for all comments
        get("/comment", (request, response) -> {

            Map<String, Object> attributes_comment = new HashMap<>();
            attributes_comment.put("title", "All Comments : ");
            attributes_comment.put("comments", ((InfoGetter_File_Impl) info).getMongoComments().toString());

            return new ModelAndView(attributes_comment, "index_comment.ftl");
        }, new FreeMarkerEngine(configuration));


        //URL for all speeches-sentiments
        get("/speechSentiment", (request, response) -> {

            Map<String, Object> attributes_sentiment = new HashMap<>();
            attributes_sentiment.put("title", "All Speeches-Sentiments : ");

            return new ModelAndView(attributes_sentiment, "speech_sentiment.ftl");
        }, new FreeMarkerEngine(configuration));

        get("/speech/sentiment", (request, response) -> {
            Gson gson = new Gson();
            List<Document> docs = new ArrayList<>();

            speechSentiment.forEach((id, sentiment) -> {
                Document doc = new Document();
                doc.put("name", id);
                doc.put("count", sentiment);
                docs.add(doc);
            });


            return gson.toJson(docs);
        });


        //URL for all comments-sentiments
        get("/commentSentiment", (request, response) -> {

            Map<String, Object> attributes_sentiment = new HashMap<>();
            attributes_sentiment.put("title", "All Comment-Sentiments : ");

            return new ModelAndView(attributes_sentiment, "comment_sentiment.ftl");
        }, new FreeMarkerEngine(configuration));


        get("/comment/sentiment", (request, response) -> {
            Gson gson = new Gson();
            List<Document> docs = new ArrayList<>();

            commentSentiment.forEach((id, sentiment) -> {
                Document doc = new Document();
                doc.put("name", id);
                doc.put("count", sentiment);
                docs.add(doc);
            });


            return gson.toJson(docs);
        });


        //URL for all namedEntities
        get("/namedEntities", (request, response) -> {

            Map<String, Object> attributes_sentiment = new HashMap<>();
            attributes_sentiment.put("title", "All NamedEntities : ");

            return new ModelAndView(attributes_sentiment, "namedEntities.ftl");
        }, new FreeMarkerEngine(configuration));


        get("/entitiesPer", (request, response) -> {
            Gson gson = new Gson();
            List<Document> docs = new ArrayList<>();

            Map<String, Long> countEntities = new HashMap<>();
            countEntities = pers.stream().
                    collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            Map<String, Long> result = countEntities.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            result.forEach((name, count) -> {
                Document doc = new Document();
                doc.put("name", name);
                doc.put("count", count);
                docs.add(doc);
            });


            return gson.toJson(docs);
        });

        get("/entitiesLoc", (request, response) -> {
            Gson gson = new Gson();
            List<Document> docs = new ArrayList<>();

            Map<String, Long> countEntities = new HashMap<>();
            countEntities = locs.stream().
                    collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            Map<String, Long> result = countEntities.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            result.forEach((name, count) -> {
                Document doc = new Document();
                doc.put("name", name);
                doc.put("count", count);
                docs.add(doc);
            });


            return gson.toJson(docs);
        });

        get("/entitiesOrg", (request, response) -> {
            Gson gson = new Gson();
            List<Document> docs = new ArrayList<>();

            Map<String, Long> countEntities = new HashMap<>();
            countEntities = orgs.stream().
                    collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            Map<String, Long> result = countEntities.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            result.forEach((name, count) -> {
                Document doc = new Document();
                doc.put("name", name);
                doc.put("count", count);
                docs.add(doc);
            });


            return gson.toJson(docs);
        });


    }


}
