package uni.project.all;

import java.util.Set;


public interface Speaker extends Protocol {


    Party getParty();


    void setParty(Party party);


    Fraction getFraction();


    void setFraction(Fraction fraction);


    String getRole();


    void setRole(String role);

    String getBirthday();

    void setBirthday(String birthday);


    String getTitle();


    void setTitle(String title);


    String getName();


    void setName(String name);


    String getFirstName();


    void setFirstName(String firstName);


    Set<Speech> getSpeeches();


    void addSpeech(Speech speech);


    void addSpeeches(Set<Speech> speeches);


    Set<Comment> getComments();


    double getAvgLength();


    boolean isLeader();

    String getDeath();

    void setDeath(String birthday);
    String getBirthPlace();

    void setBirthPlace(String birthday);
    String getSex();

    void setSex(String birthday);
    String getOccupation();

    void setOccupation(String birthday);
    String getMaritalStatus();

    void setMaritalStatus(String birthday);
    String getReligion();

    void setReligion(String birthday);
    String getAcTitle();

    void setAcTitle(String birthday);

}