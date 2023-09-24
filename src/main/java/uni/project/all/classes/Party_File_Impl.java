package uni.project.all.classes;


import uni.project.all.Party;
import uni.project.all.Speaker;

import java.util.HashSet;
import java.util.Set;


/**
 * This class stores party objects
 *
 * @author Siamak Choromkheirabadi
 */
public class Party_File_Impl implements Party {

    //variable declaration
    private String partyName = "";
    private Set<Speaker> partySpeakers = new HashSet<>(0);


    /**
     * Constructor which creates Party_File_Impl and has parameter partyName with type String
     *
     * @param partyName
     */
    public Party_File_Impl(String partyName) {
        this.setName(partyName);
    }

    /**
     * constructor
     */
    public Party_File_Impl() {

    }

    /**
     * @return the party-name
     */
    @Override
    public String getName() {
        return this.partyName;
    }

    /**
     * adds a name to the current party object
     *
     * @param name
     */
    @Override
    public void setName(String name) {
        this.partyName = name;
    }

    /**
     * @return a list of PartyMembers (speaker objects) as a set
     */
    @Override
    public Set<Speaker> getMembers() {
        return this.partySpeakers;
    }

    /**
     * adds a Speaker object to a specific list
     *
     * @param m
     */
    @Override
    public void addMember(Speaker m) {
        this.partySpeakers.add(m);
    }

    /**
     * adds speakerList (plural!) to a specific list
     *
     * @param ms
     */
    @Override
    public void addMembers(Set<Speaker> ms) {
        this.partySpeakers.addAll(ms);
    }

    /**
     * @param party the object to be compared.
     * @return an Integer which indicates the comparison between the two party names
     */
    @Override
    public int compareTo(Party party) {
        return this.getName().compareTo(party.getName());
    }

    /**
     * @param o
     * @return a boolean which checks if the hashCodes are equal
     */
    @Override
    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }

    /**
     * @return an integer which indicates the party name in hashcode
     */
    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    /**
     * @return party name as a string if the raw object is called
     */
    @Override
    public String toString() {
        return this.getName();
    }
}
