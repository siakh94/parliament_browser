package uni.project.all;

public interface Protocol extends Comparable<Protocol> {


    String getID();


    void setID(String plID);


    int getElectionPeriod();


    void setElectionPeriod(int ep);


    InfoGetter getInfo();
}