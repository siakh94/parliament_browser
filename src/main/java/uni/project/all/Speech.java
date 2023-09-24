package uni.project.all;

import java.util.List;


public interface Speech extends Protocol {


    AgendaItem getAgendaItem();


    List<Comment> getComments();



    String getText();


    PlenaryProtocol getProtocol();


    Speaker getSpeaker();


    void setSpeaker(Speaker speaker);


    int getLength();


    List<Speech> getSpeeches();


    void addText(Comment text);



}
