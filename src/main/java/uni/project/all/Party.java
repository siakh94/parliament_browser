package uni.project.all;

import java.util.Set;


public interface Party extends Comparable<Party> {


    String getName();


    void setName(String name);


    Set<Speaker> getMembers();


    void addMember(Speaker m);


    void addMembers(Set<Speaker> ms);


}