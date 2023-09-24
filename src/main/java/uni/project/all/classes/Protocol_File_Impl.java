package uni.project.all.classes;


import uni.project.all.InfoGetter;
import uni.project.all.Protocol;

/**
 * This is the head of our class and most classes somehow depend on it for making objects
 *
 * @author Siamak Choromkheirabadi
 */
public class Protocol_File_Impl implements Protocol {

    //variable declaration
    private String id = "";
    private int electionPeriode = -1;

    protected InfoGetter info;


    /**
     * Constructor which creates Protocol_File_Impl objects with parameter 'info'
     *
     * @param info
     */
    public Protocol_File_Impl(InfoGetter info) {
        this.info = info;
    }

    /**
     * constructor
     */
    public Protocol_File_Impl() {
    }

    /**
     * @return ProtocolID
     */
    @Override
    public String getID() {
        return this.id;
    }

    /**
     * adds a new id to the protocol
     *
     * @param plID
     */
    @Override
    public void setID(String plID) {
        this.id = plID;
    }

    /**
     * @return the protocols electionPeriod
     */
    @Override
    public int getElectionPeriod() {
        return this.electionPeriode;
    }

    /**
     * adds a new election period to the protocol
     *
     * @param ep
     */
    @Override
    public void setElectionPeriod(int ep) {
        this.electionPeriode = ep;
    }

    /**
     * @return the InfoGetter object
     */
    @Override
    public InfoGetter getInfo() {
        return this.info;
    }

    /**
     * @param protocol the object to be compared.
     * @return an integer indicating the comparison of the given protocolIDs
     */
    @Override
    public int compareTo(Protocol protocol) {
        return getID().compareTo(protocol.getID());
    }

    /**
     * @param o
     * @return a boolean which checks if the hashcode are equal
     */
    @Override
    public boolean equals(Object o) {
        return o.hashCode() == this.hashCode();
    }

    /**
     * @return an integer indicating the protocols ID which is processes through hashCode()
     */
    @Override
    public int hashCode() {
        return getID().hashCode();
    }
}
