package uni.project.all;

import java.util.List;
import java.util.Set;


public interface AgendaItem extends Protocol {


    List<Speech> getSpeeches();


    void addSpeech(Speech sp);


    void addSpeeches(Set<Speech> sps);


    String getIndex();


    void setIndex(String indx);


    String getTitle();


    void setTitle(String title);


    PlenaryProtocol getProtocol();

}
