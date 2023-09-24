package uni.project.all.classes;


import org.w3c.dom.Node;
import uni.project.all.Fraction;
import uni.project.all.Speaker;

import java.util.HashSet;
import java.util.Set;


/**
 * This class stores Fraction objects
 *
 * @author Siamak Choromkheirabadi
 */
public class Fraction_File_Impl implements Fraction {

    private String name = "";
    private Set<Speaker> members = new HashSet<>(0);


    /**
     * Constructor which creates Fraction_File_Impl objects and has
     * parameter node with type Node
     *
     * @param node
     */
    public Fraction_File_Impl(Node node) {
        apply(node);
    }

    /**
     * constructor
     */
    public Fraction_File_Impl() {
    }


    /**
     * This method saves the text content of a fraction-node
     *
     * @param node
     */
    private void apply(Node node) {
        this.name = node.getTextContent().trim();
    }

    /**
     * @return name as a String
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * adds Speaker Object to a list
     *
     * @param speaker
     */
    @Override
    public void addMember(Speaker speaker) {
        this.members.add(speaker);
    }

    /**
     * @return Fraction-members
     */
    @Override
    public Set<Speaker> getMembers() {
        return this.members;
    }


    /**
     * @param fraction the object to be compared.
     * @return
     */
    @Override
    public int compareTo(Fraction fraction) {
        return this.getName().toLowerCase().
                compareTo(fraction.getName().toLowerCase());
    }

    /**
     * @param o
     * @return a boolean which indicates the status of hashcode comparisons
     */
    @Override
    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }

    /**
     * @return an Integer indicating Fraction's name
     */
    @Override
    public int hashCode() {
        return this.getName().toLowerCase().hashCode();
    }

    /**
     * @return the name of a Fraction if a raw object is called
     */
    @Override
    public String toString() {
        return this.getName();
    }
}
