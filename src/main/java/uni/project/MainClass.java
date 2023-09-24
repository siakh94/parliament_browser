package uni.project;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uni.project.all.*;
import uni.project.all.classes.InfoGetter_File_Impl;
import uni.project.all.classes.PlenaryProtocol_File_Impl;
import uni.project.all.classes.mongoclasses.*;
import uni.project.mongodb.database.SparkAPI;
import uni.project.parser.FileImport;
import uni.project.parser.XMLParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * @author Siamak Choromkheirabadi
 * This is our MainClass which includes menus and options
 * that are neccessary for both user and program.
 */
public class MainClass {

    //new Object of InfoGetter_File_Impl(), which references InfoGetter, will be created
    private InfoGetter info = new InfoGetter_File_Impl();


    /**
     * This is our main method which is necessary for starting the program and
     * in this case includes file-path as arguments in order to import XMLs.
     * The initial start menu is in this method.
     *
     * @param args
     */
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {

        MainClass spareObj = new MainClass();
        List<File> xmlFiles = new ArrayList<>(0);
        List<File> baseData = new ArrayList<>(0);

        Scanner scanner = new Scanner(System.in);
        boolean b = true;
        //infinite loop 'till stopped
        while (b) {
            // this is the start menu
            try {
                //the menu will be repeated as long as the user enters false input
                System.out.println("WELCOME !\n");
                System.out.println("\nChoose an Option to Continue : (enter the related index please!) \n");

                System.out.println("(1) :   API (Without XML-Analysis; Very Quick !)  ");
                System.out.println("(2) :   Download & Parse all XMLs (Takes Too Much Time !)  ");
                System.out.println("(3) :   Quit  \n");

                String option = scanner.nextLine();
                //Quits program if 3 is selected
                if (Objects.equals(option, "3")) {
                    b = false;
                }
                //Starts API if 3 is selected
                if (Objects.equals(option, "1")) {
                    spareObj.web();
                    b = false;

                }
                //Starts parsing XMLs if 2 is entered
                if (Objects.equals(option, "2")) {

                    //Checks if main method has arguments (path to xml-files)
                    if (args.length >= 2) {
                        //Checks if the path is leading to an actual file
                        if (FileImport.fileChecker(args[0]) == true) {
                            try {
                                System.out.println("Your given path is valid !\n");
                                xmlFiles = FileImport.importXML(args[0]);
                                spareObj.importFile(xmlFiles);
                                baseData = FileImport.importXML(args[1]);
                                spareObj.other(baseData.get(0));
                                spareObj.start();
                                b = false;

                            } catch (Exception e) {
                                System.out.println("Your given path is not correct !");
                            }
                        } else {
                            System.out.println("\nNo manuel path available ! Trying to download from Server !\n");
                            //downloads dtd-file
                            FileImport.dtd();
                            //downloads XMLs from server
                            xmlFiles = FileImport.xmlWeb();
                            //passes XMLs to the importFile function for further work
                            spareObj.importFile(xmlFiles);
                            //downloads MDBXMLFile
                            baseData = FileImport.unzipFile();
                            //passes the MDB to the other function for further work
                            spareObj.other(baseData.get(0));
                            spareObj.start();
                            b = false;
                        }
                    } else {
                        System.out.println("\nNo manuel path available ! Trying to download from Server !\n");
                        FileImport.dtd();
                        xmlFiles = FileImport.xmlWeb();
                        spareObj.importFile(xmlFiles);
                        baseData = FileImport.unzipFile();
                        spareObj.other(baseData.get(0));
                        spareObj.start();
                        b = false;
                    }

                }
            }
            //catches all kinds of errors and avoiding crash
            catch (Exception e) {
                System.out.println("False Input (either is port incorrect or syntax) !");
            }
        }

    }

    /**
     * API is accessible through this method. (Acts like a bridge)
     *
     * @throws IOException
     */
    public void web() throws IOException {
        //API obj
        SparkAPI api = new SparkAPI(((InfoGetter_File_Impl) info).connection());
        //runs api...
        api.init();
    }

    /**
     * This method imports only one XML instead of the whole stuff on web.
     * It's useful especially when there's a new protocol on the Bundestag-Web.
     *
     * @param singleXML
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static void singleImport(File singleXML) throws IOException, ParserConfigurationException, SAXException {
        List<File> files = new ArrayList<>(0);
        //downloads dtd File.
        FileImport.dtd();
        //downloads Stammdaten-XML-File.
        File dataBase = FileImport.unzipFile().get(0);
        //deletes file after terminaring the program.
        dataBase.deleteOnExit();
        File xml = singleXML;
        xml.deleteOnExit();
        files.add(xml);
        //MainClass obj
        MainClass mainObj = new MainClass();
        mainObj.importFile(files);
        mainObj.other(dataBase);

    }

    /**
     * Menu for MongoDB operations
     *
     * @return a number as String
     */
    public String menu() {

        Scanner scanner = new Scanner(System.in);
        System.out.println("\nChoose an Option : (enter the related index please!): \n");
        System.out.println("(1) :   Update Document ");
        System.out.println("(2) :   Delete Document ");
        System.out.println("(3) :   Aggregate & Count Document ");
        String m = scanner.nextLine();

        return m;
    }

    /**
     * Menu for choosing a specific document
     *
     * @return a number as String
     */
    public String menu2() {

        Scanner scanner = new Scanner(System.in);
        System.out.println("\nChoose an Option : (enter the related index please!): \n");
        System.out.println("(1) :   AgendaItems  ");
        System.out.println("(2) :   Comments  ");
        System.out.println("(3) :   Fractions  ");
        System.out.println("(4) :   Parties  ");
        System.out.println("(5) :   Protocols  ");
        System.out.println("(6) :   Speakers  ");
        System.out.println("(7) :   Speeches  ");
        String m = scanner.nextLine();

        return m;
    }


    /**
     * This method brings the analysed data from XMLs and MongoDB into the program.
     * Multiple could be shown a many questions may also be asked !
     */
    public void start() {

        //Only the first few codes will be documented because the rest is
        //actually the same thing !!


        Scanner scanner = new Scanner(System.in);
        boolean b = true;
        //infinite loop 'till break
        while (b) {
            try {
                System.out.println("\nChoose an Option : (enter the related index please!) \n");

                System.out.println("(1) :   MongoDB  ");
                System.out.println("(2) :   Start NLP  ");
                System.out.println("(3) :   Quit  ");
                System.out.println("(4) :   Create MongoDB-Doc  ");
                System.out.println("(5) :   Read MongoDB-Doc  ");

                String option11 = scanner.nextLine();
                //Quits program if 3 is selected
                if (Objects.equals(option11, "3")) {
                    b = false;
                }

                if (Objects.equals(option11, "1")) {
                    System.out.println("\n What kind of operation do you wanna perform ? \n");

                    String s = menu();


                    String opt = menu2();


                    if (Objects.equals(opt, "1")) {
                        //Loops through MongoAgendaItems and shows all their info
                        this.info.getMongoAgendaItems().stream().forEach(aa -> {
                            System.out.println(aa.toString());
                        });

                        System.out.println("You can get help choosing the desired field by checking" +
                                "the Infos above !");

                        System.out.println("\nEnter the Field that you wanna work with : " +
                                "(enter the related index please!)  \n");

                        System.out.println("(1) :   Title  ");
                        System.out.println("(2) :   Protocol's ID  ");
                        String i = scanner.nextLine();


                        if (Objects.equals(i, "1")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<AgendaItem> count = new ArrayList<>(0);
                                //Loops through AgendaItems and checks if the found item is equal to the written stuff
                                info.getMongoAgendaItems().stream().forEach(agendaItem -> {
                                    if (Objects.equals(((AgendaItem_MongoDB_Impl) agendaItem).getTitle(), currentValue)) {
                                        //adds to the list for the count #aggregation.
                                        count.add(agendaItem);
                                        System.out.println(((AgendaItem_MongoDB_Impl) agendaItem).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                //Updates both internally and in the DB
                                ((InfoGetter_File_Impl) info).connection().update("AgendaItems", "Title", currentValue, newValue);

                                info.getMongoAgendaItems().stream().forEach(agendaItem -> {
                                    ((AgendaItem_MongoDB_Impl) agendaItem).setTitle(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("AgendaItems", "Title", currentValue);
                                //Deletes the selected item both internally and in db.
                                info.getMongoAgendaItems().stream().forEach(agendaItem -> {
                                    ((AgendaItem_MongoDB_Impl) agendaItem).setTitle(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "2")) {

                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<AgendaItem> count = new ArrayList<>(0);

                                info.getMongoAgendaItems().stream().forEach(agendaItem -> {
                                    if (Objects.equals(((AgendaItem_MongoDB_Impl) agendaItem).getProtocolIndex(), currentValue)) {
                                        count.add(agendaItem);
                                        System.out.println(((AgendaItem_MongoDB_Impl) agendaItem).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("AgendaItems", "Protocol's Index", currentValue, newValue);

                                info.getMongoAgendaItems().stream().forEach(agendaItem -> {
                                    ((AgendaItem_MongoDB_Impl) agendaItem).setProtocolIndex(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("AgendaItems", "Protocol's Index", currentValue);

                                info.getMongoAgendaItems().stream().forEach(agendaItem -> {
                                    ((AgendaItem_MongoDB_Impl) agendaItem).setProtocolIndex(currentValue, null);
                                });

                            }
                        }


                    }


                    if (Objects.equals(opt, "2")) {

                        this.info.getMongoComments().stream().forEach(aa -> {
                            System.out.println(aa.toString());
                        });

                        System.out.println("You can get help choosing the desired field by checking" +
                                "the Infos above !");

                        System.out.println("\nEnter the Field that you wanna work with : " +
                                "(enter the related index please!)  \n");

                        System.out.println("(1) :   Commentator  ");
                        System.out.println("(2) :   SpeechID  ");
                        String i = scanner.nextLine();


                        if (Objects.equals(i, "1")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Comment> count = new ArrayList<>(0);

                                info.getMongoComments().stream().forEach(c -> {
                                    if (Objects.equals(((Comment_MongoDB_Impl) c).getCommentator(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Comment_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Comments", "Commentator", currentValue, newValue);

                                info.getMongoComments().stream().forEach(c -> {
                                    ((Comment_MongoDB_Impl) c).setCommentator(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Comments", "Commentator", currentValue);

                                info.getMongoComments().stream().forEach(c -> {
                                    ((Comment_MongoDB_Impl) c).setCommentator(currentValue, null);
                                });

                            }

                        }
                        if (Objects.equals(i, "2")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Comment> count = new ArrayList<>(0);

                                info.getMongoComments().stream().forEach(c -> {
                                    if (Objects.equals(((Comment_MongoDB_Impl) c).getSpeechID(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Comment_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Comments", "SpeechID", currentValue, newValue);

                                info.getMongoComments().stream().forEach(c -> {
                                    ((Comment_MongoDB_Impl) c).setSpeechID(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Comments", "SpeechID", currentValue);

                                info.getMongoComments().stream().forEach(c -> {
                                    ((Comment_MongoDB_Impl) c).setSpeechID(currentValue, null);
                                });

                            }

                        }


                    }


                    if (Objects.equals(opt, "3")) {

                        this.info.getMongoFractions().stream().forEach(aa -> {
                            System.out.println(aa.toString());
                        });

                        System.out.println("You can get help choosing the desired field by checking" +
                                "the Infos above !");

                        System.out.println("\nEnter the Field that you wanna work with : " +
                                "(enter the related index please!)  \n");

                        System.out.println("(1) :   Fraction  ");
                        String i = scanner.nextLine();


                        if (Objects.equals(i, "1")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Fraction> count = new ArrayList<>(0);

                                info.getMongoFractions().stream().forEach(f -> {
                                    if (Objects.equals(((Fraction_MongoDB_Impl) f).getFraction(), currentValue)) {
                                        count.add(f);
                                        System.out.println(((Fraction_MongoDB_Impl) f).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Fractions", "Fraction", currentValue, newValue);

                                info.getMongoFractions().stream().forEach(agendaItem -> {
                                    ((Fraction_MongoDB_Impl) agendaItem).setFraction(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Fractions", "Fraction", currentValue);

                                info.getMongoFractions().stream().forEach(f -> {
                                    ((Fraction_MongoDB_Impl) f).setFraction(currentValue, null);
                                });

                            }

                        }

                        if (Objects.equals(i, "2")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Fraction> count = new ArrayList<>(0);

                                info.getMongoFractions().stream().forEach(c -> {
                                    if (Objects.equals(((Fraction_MongoDB_Impl) c).getFractionMembers(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Fraction_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }


                        }

                    }


                    if (Objects.equals(opt, "4")) {

                        this.info.getMongoParties().stream().forEach(aa -> {
                            System.out.println(aa.toString());
                        });

                        System.out.println("You can get help choosing the desired field by checking" +
                                "the Infos above !");

                        System.out.println("\nEnter the Field that you wanna work with : " +
                                "(enter the related index please!)  \n");

                        System.out.println("(1) :   Party  ");
                        String i = scanner.nextLine();


                        if (Objects.equals(i, "1")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Party> count = new ArrayList<>(0);

                                info.getMongoParties().stream().forEach(p -> {
                                    if (Objects.equals(((Party_MongoDB_Impl) p).getParty(), currentValue)) {
                                        count.add(p);
                                        System.out.println(((Party_MongoDB_Impl) p).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Parties", "Party", currentValue, newValue);

                                info.getMongoParties().stream().forEach(p -> {
                                    ((Party_MongoDB_Impl) p).setParty(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Parties", "Party", currentValue);

                                info.getMongoParties().stream().forEach(agendaItem -> {
                                    ((Party_MongoDB_Impl) agendaItem).setParty(currentValue, null);
                                });

                            }

                        }

                        if (Objects.equals(i, "2")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Party> count = new ArrayList<>(0);

                                info.getMongoParties().stream().forEach(c -> {
                                    if (Objects.equals(((Party_MongoDB_Impl) c).getPartyMembers(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Party_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }


                        }

                    }


                    if (Objects.equals(opt, "5")) {

                        this.info.getMongoProtocols().stream().forEach(aa -> {
                            System.out.println(aa.toString());
                        });

                        System.out.println("You can get help choosing the desired field by checking" +
                                "the Infos above !");

                        System.out.println("\nEnter the Field that you wanna work with : " +
                                "(enter the related index please!)  \n");

                        System.out.println("(1) :   Title  ");
                        System.out.println("(2) :   Date  ");
                        System.out.println("(3) :   ElectionPeriod  ");
                        System.out.println("(4) :   Starttime  ");
                        System.out.println("(5) :   Endtime  ");
                        System.out.println("(6) :   Index  ");
                        System.out.println("(7) :   Place  ");
                        String i = scanner.nextLine();


                        if (Objects.equals(i, "1")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<PlenaryProtocol> count = new ArrayList<>(0);

                                info.getMongoProtocols().stream().forEach(p -> {
                                    if (Objects.equals(((PlenaryProtocol_MongoDB_Impl) p).getTitle(), currentValue)) {
                                        count.add(p);
                                        System.out.println(((PlenaryProtocol_MongoDB_Impl) p).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Protocols", "Title", currentValue, newValue);

                                info.getMongoProtocols().stream().forEach(p -> {
                                    ((PlenaryProtocol_MongoDB_Impl) p).setTitle(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Protocols", "Title", currentValue);

                                info.getMongoProtocols().stream().forEach(p -> {
                                    ((PlenaryProtocol_MongoDB_Impl) p).setTitle(currentValue, null);
                                });

                            }

                        }

                        if (Objects.equals(i, "2")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<PlenaryProtocol> count = new ArrayList<>(0);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    if (Objects.equals(((PlenaryProtocol_MongoDB_Impl) c).getDate(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((PlenaryProtocol_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Protocols", "Date", currentValue, newValue);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    ((PlenaryProtocol_MongoDB_Impl) c).setDate(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Protocols", "Date", currentValue);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    ((PlenaryProtocol_MongoDB_Impl) c).setDate(currentValue, null);
                                });

                            }

                        }

                        if (Objects.equals(i, "3")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<PlenaryProtocol> count = new ArrayList<>(0);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    if (Objects.equals(((PlenaryProtocol_MongoDB_Impl) c).getElectionPeriod(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((PlenaryProtocol_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Protocols", "ElectionPeriod", currentValue, newValue);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    ((PlenaryProtocol_MongoDB_Impl) c).setElectionPeriod(Integer.parseInt(currentValue), Integer.parseInt(newValue));
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Protocols", "ElectionPeriod", currentValue);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    ((PlenaryProtocol_MongoDB_Impl) c).setElectionPeriod(Integer.parseInt(currentValue), null);
                                });

                            }

                        }

                        if (Objects.equals(i, "4")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<PlenaryProtocol> count = new ArrayList<>(0);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    if (Objects.equals(((PlenaryProtocol_MongoDB_Impl) c).getBeginTime(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((PlenaryProtocol_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Protocols", "Starttime", currentValue, newValue);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    ((PlenaryProtocol_MongoDB_Impl) c).setBeginTime(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Protocols", "Starttime", currentValue);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    ((PlenaryProtocol_MongoDB_Impl) c).setBeginTime(currentValue, null);
                                });

                            }

                        }

                        if (Objects.equals(i, "5")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<PlenaryProtocol> count = new ArrayList<>(0);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    if (Objects.equals(((PlenaryProtocol_MongoDB_Impl) c).getEndTime(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((PlenaryProtocol_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Protocols", "Endtime", currentValue, newValue);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    ((PlenaryProtocol_MongoDB_Impl) c).setEndTime(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Protocols", "Endtime", currentValue);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    ((PlenaryProtocol_MongoDB_Impl) c).setEndTime(currentValue, null);
                                });

                            }

                        }

                        if (Objects.equals(i, "6")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<PlenaryProtocol> count = new ArrayList<>(0);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    if (Objects.equals(((PlenaryProtocol_MongoDB_Impl) c).getIndex(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((PlenaryProtocol_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Protocols", "Index", currentValue, newValue);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    ((PlenaryProtocol_MongoDB_Impl) c).setIndex(Integer.parseInt(currentValue), Integer.parseInt(newValue));
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Protocols", "Index", currentValue);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    ((PlenaryProtocol_MongoDB_Impl) c).setIndex(Integer.parseInt(currentValue), null);
                                });

                            }

                        }

                        if (Objects.equals(i, "7")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<PlenaryProtocol> count = new ArrayList<>(0);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    if (Objects.equals(((PlenaryProtocol_MongoDB_Impl) c).getPlace(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((PlenaryProtocol_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Protocols", "Place", currentValue, newValue);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    ((PlenaryProtocol_MongoDB_Impl) c).setPlace(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Protocols", "Place", currentValue);

                                info.getMongoProtocols().stream().forEach(c -> {
                                    ((PlenaryProtocol_MongoDB_Impl) c).setPlace(currentValue, null);
                                });

                            }

                        }

                    }


                    if (Objects.equals(opt, "6")) {

                        this.info.getMongoSpeakers().stream().forEach(aa -> {
                            System.out.println(aa.toString());
                        });

                        System.out.println("You can get help choosing the desired field by checking" +
                                "the Infos above !");

                        System.out.println("\nEnter the Field that you wanna work with : " +
                                "(enter the related index please!)  \n");

                        System.out.println("(1) :   Title  ");
                        System.out.println("(2) :   FirstName  ");
                        System.out.println("(3) :   LastName  ");
                        System.out.println("(4) :   Fraction  ");
                        System.out.println("(5) :   Party  ");
                        System.out.println("(6) :   Role  ");
                        System.out.println("(7) :   Leadership  ");
                        System.out.println("(8) :   Birthday  ");
                        System.out.println("(9) :   Death  ");
                        System.out.println("(10) :  PlaceOfBirth  ");
                        System.out.println("(11) :  SEX  ");
                        System.out.println("(12) :  MaritalStatus  ");
                        System.out.println("(13) :  Religion  ");
                        System.out.println("(14) :  AcademicTitle  ");
                        System.out.println("(15) :  Occupation  ");
                        System.out.println("(16) :  Speaker's Speech-Length  ");
                        System.out.println("(17) :   SpeakerID  ");
                        String i = scanner.nextLine();


                        if (Objects.equals(i, "1")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(speaker -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) speaker).getTitle(), currentValue)) {
                                        count.add(speaker);
                                        System.out.println(((Speaker_MongoDB_Impl) speaker).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "Title", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(speaker -> {
                                    ((Speaker_MongoDB_Impl) speaker).setTitle(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "Title", currentValue);

                                info.getMongoSpeakers().stream().forEach(speaker -> {
                                    ((Speaker_MongoDB_Impl) speaker).setTitle(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "2")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) c).getFirstName(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speaker_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "FirstName", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setFirstName(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "FirstName", currentValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setFirstName(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "3")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) c).getName(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speaker_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "LastName", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setLastName(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "LastName", currentValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setLastName(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "4")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) c).getFraction(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speaker_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "Fraction", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setFraction(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "Fraction", currentValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setFraction(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "5")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) c).getParty(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speaker_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "Party", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setParty(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "Party", currentValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setParty(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "6")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) c).getRole(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speaker_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "Role", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setRole(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "Role", currentValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setRole(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "7")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) c).isLeader(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speaker_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "IsLeader", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setLeader(currentValue.isEmpty(), newValue.isEmpty());
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "IsLeader", currentValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setLeader(currentValue.isEmpty(), null);
                                });

                            }

                        }


                        if (Objects.equals(i, "8")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) c).getBirthday(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speaker_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "Birthday", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setBirthday(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "Birthday", currentValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setBirthday(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "9")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) c).getDeath(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speaker_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "Death", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setDeath(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "Death", currentValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setDeath(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "10")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) c).getBirthPlace(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speaker_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "PlaceOfBirth", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setBirthPlace(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "PlaceOfBirth", currentValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setBirthPlace(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "11")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) c).getSex(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speaker_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "SEX", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setSex(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "SEX", currentValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setSex(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "12")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) c).getMaritalStatus(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speaker_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "MaritalStatus", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setMaritalStatus(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "MaritalStatus", currentValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setMaritalStatus(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "13")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) c).getReligion(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speaker_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "Religion", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setReligion(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "Religion", currentValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setReligion(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "14")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) c).getAcTitle(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speaker_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "AcademicTitle", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setAcademicTitle(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "AcademicTitle", currentValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setAcademicTitle(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "15")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) c).getOccupation(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speaker_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "Occupation", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setOccupation(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "Occupation", currentValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setOccupation(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "16")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) c).getAvgLength(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speaker_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "Speaker's Speech-Length", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setAvgLength(Double.parseDouble(currentValue), Double.parseDouble(newValue));
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "Speaker's Speech-Length", currentValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setAvgLength(Double.parseDouble(currentValue), null);
                                });

                            }

                        }


                        if (Objects.equals(i, "17")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speaker> count = new ArrayList<>(0);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    if (Objects.equals(((Speaker_MongoDB_Impl) c).getID(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speaker_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speakers", "_id", currentValue, newValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setID(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speakers", "_id", currentValue);

                                info.getMongoSpeakers().stream().forEach(c -> {
                                    ((Speaker_MongoDB_Impl) c).setID(currentValue, null);
                                });

                            }

                        }


                    }


                    if (Objects.equals(opt, "7")) {

                        this.info.getMongoSpeeches().stream().forEach(aa -> {
                            System.out.println(aa.toString());
                        });

                        System.out.println("You can get help choosing the desired field by checking" +
                                "the Infos above !");

                        System.out.println("\nEnter the Field that you wanna work with : " +
                                "(enter the related index please!)  \n");

                        System.out.println("(1) :   Speaker's Firstname  ");
                        System.out.println("(2) :   Speaker's Lastname  ");
                        System.out.println("(3) :   ProtocolIndex  ");
                        System.out.println("(4) :   SpeechID  ");
                        String i = scanner.nextLine();


                        if (Objects.equals(i, "1")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speech> count = new ArrayList<>(0);

                                info.getMongoSpeeches().stream().forEach(speech -> {
                                    if (Objects.equals(((Speech_MongoDB_Impl) speech).getSFirstname(), currentValue)) {
                                        count.add(speech);
                                        System.out.println(((Speech_MongoDB_Impl) speech).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speeches", "Speaker's Firstname", currentValue, newValue);

                                info.getMongoSpeeches().stream().forEach(speech -> {
                                    ((Speech_MongoDB_Impl) speech).setFirstname(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speeches", "Speaker's Firstname", currentValue);

                                info.getMongoSpeeches().stream().forEach(speech -> {
                                    ((Speech_MongoDB_Impl) speech).setFirstname(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "2")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speech> count = new ArrayList<>(0);

                                info.getMongoSpeeches().stream().forEach(c -> {
                                    if (Objects.equals(((Speech_MongoDB_Impl) c).getSLastname(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speech_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speeches", "Speaker's Lastname", currentValue, newValue);

                                info.getMongoSpeeches().stream().forEach(c -> {
                                    ((Speech_MongoDB_Impl) c).setLastname(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speeches", "Speaker's Lastname", currentValue);

                                info.getMongoSpeeches().stream().forEach(c -> {
                                    ((Speech_MongoDB_Impl) c).setLastname(currentValue, null);
                                });

                            }

                        }


                        if (Objects.equals(i, "3")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speech> count = new ArrayList<>(0);

                                info.getMongoSpeeches().stream().forEach(c -> {
                                    if (Objects.equals(((Speech_MongoDB_Impl) c).getProtocolIndex(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speech_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speeches", "ProtocolIndex", currentValue, newValue);

                                info.getMongoSpeeches().stream().forEach(c -> {
                                    ((Speech_MongoDB_Impl) c).setProtocolIndex(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speeches", "ProtocolIndex", currentValue);

                                info.getMongoSpeeches().stream().forEach(c -> {
                                    ((Speech_MongoDB_Impl) c).setProtocolIndex(currentValue, null);
                                });

                            }

                        }

                        if (Objects.equals(i, "4")) {
                            System.out.println("\nEnter the wanted Field's name : " +
                                    "(You can use the info above! \n");

                            String currentValue = scanner.nextLine();

                            if (Objects.equals(s, "3")) {
                                List<Speech> count = new ArrayList<>(0);

                                info.getMongoSpeeches().stream().forEach(c -> {
                                    if (Objects.equals(((Speech_MongoDB_Impl) c).getSpeechID(), currentValue)) {
                                        count.add(c);
                                        System.out.println(((Speech_MongoDB_Impl) c).toString());
                                    }
                                });
                                System.out.println("Total Number of Selected Value : " + count.size());
                            }

                            if (Objects.equals(s, "1")) {
                                System.out.println("\nEnter the new Field's name : \n");
                                String newValue = scanner.nextLine();
                                ((InfoGetter_File_Impl) info).connection().update("Speeches", "_id", currentValue, newValue);

                                info.getMongoSpeeches().stream().forEach(c -> {
                                    ((Speech_MongoDB_Impl) c).setSpeechID(currentValue, newValue);
                                });

                                System.out.println("Document was successfully updated both internally and in MongoDB !");

                            }

                            if (Objects.equals(s, "2")) {
                                ((InfoGetter_File_Impl) info).connection().delete("Speeches", "_id", currentValue);

                                info.getMongoSpeeches().stream().forEach(c -> {
                                    ((Speech_MongoDB_Impl) c).setSpeechID(currentValue, null);
                                });

                            }

                        }


                    }


                }

                if (Objects.equals(option11, "2")) {

                    try {
                        //starts the mighty NLP-Engine !!!
                        ((InfoGetter_File_Impl) info).connection().nlpPipeline();

                    } catch (Exception e) {
                        //Catches all kinds errors !
                        System.out.println("Dude ! NLP is already loaded !! ");
                    }

                }

                //Creates new collections and documents both internally and in the DB.
                if (Objects.equals(option11, "4")) {
                    boolean d = true;
                    String field;
                    String value;
                    String done;
                    System.out.println("Enter the collection you wanna add : ");
                    String collection = scanner.nextLine();
                    while (d) {
                        System.out.println("Enter the field you wanna add : ");
                        field = scanner.nextLine();
                        System.out.println("Enter the value you wanna add : ");
                        value = scanner.nextLine();
                        ((InfoGetter_File_Impl) info).connection().create(collection, field, value);
                        System.out.println("Are you done ? (yes/no)");
                        done = scanner.nextLine();

                        //breaks if yes
                        if (Objects.equals(done, "yes")) {
                            d = false;
                        }


                    }

                }


                //Shows all the data in a selected collection.
                if (Objects.equals(option11, "5")) {

                    String a = menu2();

                    if (Objects.equals(a, "1")) {
                        //shows all AgendaItems
                        this.info.getMongoAgendaItems().stream().forEach(c -> {
                            System.out.println(c.toString());
                        });
                    }
                    if (Objects.equals(a, "2")) {
                        //shows all comments
                        this.info.getMongoComments().stream().forEach(c -> {
                            System.out.println(c.toString());
                        });

                    }
                    if (Objects.equals(a, "3")) {
                        //shows all fractions
                        this.info.getMongoFractions().stream().forEach(c -> {
                            System.out.println(c.toString());
                        });

                    }
                    if (Objects.equals(a, "4")) {
                        //shows all parties
                        this.info.getMongoParties().stream().forEach(c -> {
                            System.out.println(c.toString());
                        });

                    }
                    if (Objects.equals(a, "5")) {
                        //shows all protocols
                        this.info.getMongoProtocols().stream().forEach(c -> {
                            System.out.println(c.toString());
                        });

                    }
                    if (Objects.equals(a, "6")) {
                        //shows all speakers
                        this.info.getMongoSpeakers().stream().forEach(c -> {
                            System.out.println(c.toString());
                        });

                    }
                    if (Objects.equals(a, "7")) {
                        //shows all speeches
                        this.info.getMongoSpeeches().stream().forEach(c -> {
                            System.out.println(c.toString());
                        });

                    }


                }


                //checks for false inputs
            } catch (Exception e) {
                System.out.println("False Input!");
            }


        }

    }


    /**
     * This method gets downloaded (or internally added) XML-Files from
     * FileImport class and starts parsing and analysing them.
     *
     * @param files
     * @throws IOException
     */
    public void importFile(List<File> files) throws IOException {
        //MongoDB will be connected to java, Photos and their MetaData start to download.
        ((InfoGetter_File_Impl) info).connection().PhotoAndData();
        System.out.println("Starting to import XMLs ...");

        //Each file will be individually analysed and its data would be stored in related classes.
        files.
                stream().forEach(f -> {
                    //PlenaryProtocol obj
                    PlenaryProtocol protocol = new PlenaryProtocol_File_Impl(this.info, f);
                    System.out.println(protocol.getTitle() + " Imported !");
                    //deletes the already parsed file.
                    f.delete();

                    try {
                        //The completed Object 'info' is ready to be used as a mediator (so we can access other classes)
                        ((InfoGetter_File_Impl) info).mongoDirection(protocol);

                        //checks if the MongoDB is already loaded or not.
                    } catch (Exception e) {
                        System.out.println("Dude ! MongoDB is already loaded !! ");
                    }


                    //emptying already copied objects for better performance
                    protocol.getAgendaItems().stream().forEach(agendaItem -> {
                        agendaItem.getSpeeches().stream().forEach(speech -> {
                            speech.getComments().clear();
                            speech = null;
                        });
                        agendaItem = null;
                    });
                    protocol = null;
                    info.getSpeakers().stream().forEach(speaker -> {
                        speaker.getComments().clear();
                    });


                });


        try {
            //all fraction-Objects will be given to allFractions method in order to be uploaded to DB
            info.getFractions().stream().forEach(fraction -> {
                ((InfoGetter_File_Impl) this.info).allFractions(fraction);
            });

        } catch (Exception e) {
            System.out.println("Dude ! MongoDB is already loaded !! ");
        }


        System.out.println("Loading " + ((InfoGetter_File_Impl) this.info).getMongoProtocols().size() +
                " Plenary Protocols");


    }


    /**
     * This Method gets the MDB (Stammdaten-XMLFile) and analyses the base data on it.
     * It eventually passes the taken nodes and Strings to the related Classes so they can be stored.
     *
     * @param file
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public void other(File file) throws ParserConfigurationException, IOException, SAXException {


        //uses the MDB-XML file to avoid anomalies in xml files relating to 'parties'


        DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();

        DocumentBuilder dBuilder = dbFac.newDocumentBuilder();

        //new Document on the MDB-XML file is being created
        Document doc2 = dBuilder.parse(file);

        //every node with tag_name "MDB" will be listed
        NodeList nodes2 = doc2.getElementsByTagName("MDB");

        for (int a = 0; a < nodes2.getLength(); a++) {

            //MDB-nodes are being iterated
            Node n = nodes2.item(a);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                //XMLParser method is called to find the Node with the name "ID"
                Node nodeID = XMLParser.getXMLNode(e, "ID");

                //a list of speaker objects, which have IDs, will be through info-Object created (ID is needed!)
                List<Speaker> lisSpeakers = info.getSpeakers().
                        stream().filter(speaker -> {
                            assert nodeID != null;
                            return speaker.getID().
                                    equalsIgnoreCase(nodeID.getTextContent());
                        }).
                        collect(Collectors.toList());
                if (lisSpeakers.size() == 1) {
                    //if the list has only one element, the node "PARTEI-KURZ" will be searched
                    Node parteiKurz = XMLParser.getXMLNode(n, "PARTEI_KURZ");
                    Node nodeBD = XMLParser.getXMLNode(e, "GEBURTSDATUM");
                    Node nodeDD = XMLParser.getXMLNode(e, "STERBEDATUM");
                    Node nodePB = XMLParser.getXMLNode(e, "GEBURTSORT");
                    Node nodeSex = XMLParser.getXMLNode(e, "GESCHLECHT");
                    Node nodeOcu = XMLParser.getXMLNode(e, "BERUF");
                    Node nodeMS = XMLParser.getXMLNode(e, "FAMILIENSTAND");
                    Node nodeRel = XMLParser.getXMLNode(e, "RELIGION");
                    Node nodeAcTitle = XMLParser.getXMLNode(e, "AKAD_TITEL");

                    if (parteiKurz != null) {
                        //and the missing party will be added to the party-list
                        Party party = info.getParty(parteiKurz.getTextContent());
                        lisSpeakers.get(0).setParty(party);
                    }
                    if (nodeBD != null) {
                        //It's very obvious what's being taken !
                        String birthday = nodeBD.getTextContent();
                        lisSpeakers.get(0).setBirthday(birthday);
                    }
                    if (nodeDD != null) {
                        String deathDate = nodeDD.getTextContent();
                        lisSpeakers.get(0).setDeath(deathDate);
                    }
                    if (nodePB != null) {
                        String birthPlace = nodePB.getTextContent();
                        lisSpeakers.get(0).setBirthPlace(birthPlace);
                    }
                    if (nodeSex != null) {
                        String sex = nodeSex.getTextContent();
                        lisSpeakers.get(0).setSex(sex);
                    }
                    if (nodeOcu != null) {
                        String occupation = nodeOcu.getTextContent();
                        lisSpeakers.get(0).setOccupation(occupation);
                    }
                    if (nodeMS != null) {
                        String maritalStatus = nodeMS.getTextContent();
                        lisSpeakers.get(0).setMaritalStatus(maritalStatus);
                    }
                    if (nodeRel != null) {
                        String religion = nodeRel.getTextContent();
                        lisSpeakers.get(0).setReligion(religion);
                    }
                    if (nodeAcTitle != null) {
                        String acTitle = nodeAcTitle.getTextContent();
                        lisSpeakers.get(0).setAcTitle(acTitle);
                    }

                }

            }
            file.delete();


        }
        try {
            //All parsed parties will be passed to allSpeakers method for further actions
            this.info.getParties().stream().forEach(party -> {
                ((InfoGetter_File_Impl) this.info).allParties(party);
            });

        } catch (Exception e) {
            System.out.println("Dude ! MongoDB is already loaded !! ");
        }


        try {
            //All parsed Speakers will be passed to allParties method for further actions

            this.info.getSpeakers().stream().forEach(speaker -> {

                ((InfoGetter_File_Impl) this.info).allSpeakers(speaker);
                speaker.getSpeeches().clear();
                speaker = null;
            });

        } catch (Exception e) {
            System.out.println("Dude ! MongoDB is already loaded !! ");
        }


    }

}