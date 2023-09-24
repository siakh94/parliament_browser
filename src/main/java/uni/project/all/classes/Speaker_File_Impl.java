package uni.project.all.classes;


import org.w3c.dom.Node;
import uni.project.all.*;
import uni.project.parser.XMLParser;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.HashSet;
import java.util.Set;

/**
 * This class stores Speaker objects
 *
 * @author Siamak Choromkheirabadi
 */
public class Speaker_File_Impl extends Protocol_File_Impl implements Speaker {

    //variable declaration
    protected String name = "";
    protected String firstName = "";
    protected String title = "";
    protected String role = "";

    protected Set<Speech> speeches = new HashSet<>();

    private XPath xPath = XPathFactory.newInstance().newXPath();
    protected Fraction fraction;
    protected Party party;
    private String birthday;
    private String deathDate;
    private String birthPlace;
    private String sex;
    private String occupation;
    private String maritalStatus;
    private String religion;
    private String acTitle;


    /**
     * makes Speaker_File_Impl objects, which have the parameter 'info' with dataType InfoGetter
     *
     * @param info
     */
    public Speaker_File_Impl(InfoGetter info) {
        super(info);
    }


    /**
     * constructor which calls the apply method
     *
     * @param info
     * @param node
     * @throws XPathExpressionException
     */
    public Speaker_File_Impl(InfoGetter info, Node node) throws XPathExpressionException {
        super(info);
        this.setID(node.getAttributes().getNamedItem("id").getTextContent());
        apply(node);
    }

    /**
     * constructor
     */
    public Speaker_File_Impl() {

    }


    /**
     * maily adds speaker data to Speaker_File_Impl Object through XPath!
     *
     * @param spNode
     * @throws XPathExpressionException
     */
    private void apply(Node spNode) throws XPathExpressionException {

        //finds firstname
        String fiName = xPath.compile("./name/vorname").evaluate(spNode);
        if (fiName != null) {
            this.setFirstName(fiName);
        }
        String laName = xPath.compile("./name/nachname").evaluate(spNode);
        if (laName != null) {
            this.setName(laName);
        }
        String miName = xPath.compile("./name/namenszusatz").evaluate(spNode);
        if (miName != null) {
            this.setName(miName + " " + this.getName());
        }
        String title = xPath.compile("./name/titel").evaluate(spNode);
        if (title != null) {
            this.setTitle(title);
        }
        String role = xPath.compile("./name/rolle/rolle_lang").evaluate(spNode);
        if (role != null) {
            this.setRole(role);
        }
        //this ain't XPath ! finds fractions though!
        Node fracNode = XMLParser.getXMLNode(spNode, "fraktion");
        if (fracNode != null) {
            this.setFraction(this.getInfo().getFraction(fracNode));
            this.getFraction().addMember(this);
        }

    }

    /**
     * @return the party object related to this Speaker object
     */
    @Override
    public Party getParty() {
        return this.party;
    }

    /**
     * adds a party object to a specific list
     *
     * @param party
     */
    @Override
    public void setParty(Party party) {
        this.party = party;
        party.addMember(this);
    }

    /**
     * @return the fraction object related to this Speaker object
     */
    @Override
    public Fraction getFraction() {
        return this.fraction;
    }

    /**
     * sets a new fraction
     *
     * @param fraction
     */
    @Override
    public void setFraction(Fraction fraction) {
        this.fraction = fraction;
    }

    /**
     * @return the speaker's role as a string
     */
    @Override
    public String getRole() {
        return this.role;
    }

    /**
     * adds a new role to the spekaer
     *
     * @param role
     */
    @Override
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return the speaker's title
     */
    @Override
    public String getTitle() {
        return this.title;
    }

    /**
     * sets a new title
     *
     * @param title
     */
    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the speaker's name
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * sets a new name as string
     *
     * @param name
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return firstname
     */
    @Override
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * sets a new firstname
     *
     * @param firstName
     */
    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the list of speeches (as objects)
     */
    @Override
    public Set<Speech> getSpeeches() {
        return this.speeches;
    }

    /**
     * adds a new object to a specific list
     *
     * @param speech
     */
    @Override
    public void addSpeech(Speech speech) {
        this.speeches.add(speech);
    }

    /**
     * adds a list of speakers to a specific list (which all be united at the end)
     *
     * @param speeches
     */
    @Override
    public void addSpeeches(Set<Speech> speeches) {
        speeches.forEach(this::addSpeech);
    }

    /**
     * @return The related set of comments
     */
    @Override
    public Set<Comment> getComments() {
        Set<Comment> setOfComments = new HashSet<>(0);
        this.getSpeeches().stream().
                forEach(speech -> setOfComments.addAll(speech.getComments()));
        return setOfComments;
    }


    /**
     * @return the average length of speeches of a specific speaker
     */
    @Override
    public double getAvgLength() {
        double avg = 0.0;
        int sum = this.getSpeeches().stream().mapToInt(Speech::getLength).sum();
        avg = sum / this.getSpeeches().size();
        return avg;
    }

    /**
     * @return speakers' title, firstname and lastname if the raw speaker Obj is called
     */
    @Override
    public String toString() {
        return this.getTitle() + " " + this.getFirstName()
                + " " + this.getName();
    }

    /**
     * @return a boolean which indicates if the speaker is a leader
     */
    @Override
    public boolean isLeader() {
        //checks if the speaker is a leader
        boolean leader;

        leader = this.getRole().
                startsWith("Präsident") || this.
                getRole().startsWith("Vizepräsident") || this.
                getRole().toLowerCase().startsWith("alters");

        if (!leader) {
            leader = this.getName().
                    startsWith("Präsident") || this.getName().
                    startsWith("Vizepräsident");
        }

        return leader;
    }


    /**
     * @return an integer that indicates the speaker's name in hashcode
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }


    /**
     * corrects some false names on xml
     *
     * @param leader
     * @return the leader as a String
     */
    public static String edit(String leader) {

        leader = leader.replaceAll("Vizepräsident in", "Vizepräsidentin");
        leader = leader.replaceAll("Vizepräsiden t", "Vizepräsident");
        leader = leader.replaceAll(":", "");

        return leader;

    }


    /**
     * @return death date
     */
    @Override
    public String getDeath() {
        return deathDate;
    }

    /**
     * sets death date
     *
     * @param deathDate
     */
    @Override
    public void setDeath(String deathDate) {
        this.deathDate = deathDate;

    }

    /**
     * @return place of birth
     */
    @Override
    public String getBirthPlace() {
        return birthPlace;
    }

    /**
     * sets place of birth
     *
     * @param birthPlace
     */
    @Override
    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;

    }

    /**
     * @return sex of the speaker
     */
    @Override
    public String getSex() {
        return sex;
    }

    /**
     * sets sex of the speaker
     *
     * @param sex
     */
    @Override
    public void setSex(String sex) {
        this.sex = sex;

    }

    /**
     * @return the speakers occupation
     */
    @Override
    public String getOccupation() {
        return occupation;
    }

    /**
     * sets the speaker's occupation
     *
     * @param occupation
     */
    @Override
    public void setOccupation(String occupation) {

        this.occupation = occupation;
    }

    /**
     * @return the marital status as a string
     */
    @Override
    public String getMaritalStatus() {
        return maritalStatus;
    }

    /**
     * sets new marital status
     *
     * @param maritalStatus
     */
    @Override
    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;

    }

    /**
     * @return religion
     */
    @Override
    public String getReligion() {
        return religion;
    }

    /**
     * sets religion
     *
     * @param religion
     */
    @Override
    public void setReligion(String religion) {

        this.religion = religion;
    }

    /**
     * @return academic title
     */
    @Override
    public String getAcTitle() {
        return acTitle;
    }

    /**
     * sets academic title
     *
     * @param acTitle
     */
    @Override
    public void setAcTitle(String acTitle) {
        this.acTitle = acTitle;

    }

    /**
     * @return birthday
     */
    @Override
    public String getBirthday() {
        return birthday;
    }

    /**
     * sets birthday
     *
     * @param birthday
     */
    @Override
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
}
