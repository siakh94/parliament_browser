package uni.project.all;

import java.util.Set;


public interface Fraction extends Comparable<Fraction> {


    String getName();


    void addMember(Speaker speaker);


    Set<Speaker> getMembers();

}