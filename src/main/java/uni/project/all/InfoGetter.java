package uni.project.all;

import com.mongodb.client.MongoCursor;
import org.bson.BsonDocument;
import org.bson.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.util.List;
import java.util.Set;


public interface InfoGetter {


    Set<Speaker> getSpeakers();


    Set<PlenaryProtocol> getProtocols();


    void addProtocol(PlenaryProtocol protocol);


    Set<Fraction> getFractions();


    Set<Party> getParties();


    Party getParty(String party);


    Speaker getSpeaker(String name);


    Speaker getSpeaker(Node node) throws XPathExpressionException;


    Fraction getFraction(String name);


    Fraction getFraction(Node node);




    void addMongoProtocol(PlenaryProtocol protocol);
    void addMongoFraction(Fraction fraction);
    void addMongoParty(Party party);
    void addMongoSpeaker(Speaker speaker);
    void addMongoSpeech(Speech speech);
    void addMongoAgendaItem(AgendaItem ai);
    void addMongoComment(Comment comment);




    List<PlenaryProtocol> getMongoProtocols();

    List<Speaker> getMongoSpeakers();

    List<Fraction> getMongoFractions();

    List<Party> getMongoParties();

    List<Speech> getMongoSpeeches();

    List<AgendaItem> getMongoAgendaItems();

    List<Comment> getMongoComments();



}