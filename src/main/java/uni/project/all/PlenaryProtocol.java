package uni.project.all;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Set;


public interface PlenaryProtocol extends Protocol {


    int getIndex();


    void setIndex(int indx);


    String getDate();


    void setDate(String date);


    String getBeginTime();


    void setBeginTime(String bt);


    String getEndTime();


    void setEndTime(String et);


    String getTitle();


    void setTitle(String title);


    List<AgendaItem> getAgendaItems();


    void addAgendaItem(AgendaItem item);


    void addAgendaItems(Set<AgendaItem> items);


    String getPlace();


    void setPlace(String place);


    Set<Speaker> getSpeakers();


    Set<Speaker> getSpeakers(Party ps);


    Set<Speaker> getSpeakers(Fraction fs);


    Set<Speaker> getLeaders();


}