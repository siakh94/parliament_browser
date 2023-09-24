package uni.project.parser;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * This class helps parsing XML files by breaking the Nodes and
 * their children.
 *
 * @author Siamak Choromkheirabadi
 */
public class XMLParser {


    /**
     * iterates nodes and their children to find the wanted node with a specific name
     *
     * @param node
     * @param nName
     * @return
     */
    public static List<Node> getXMLNodes(Node node, String nName) {
        //iteration instead of recursion
        ArrayList<Node> seek = new ArrayList<>(0);
        Node child = node;
        Node child_2 = node;
        Node child_3 = node;
        Node child_4 = node;
        Node child_5 = node;
        Node child_6 = node;
        //adds the node to the list, if the name directly matches at the beginning
        if (node.getNodeName().equals(nName)) {
            seek.add(node);
        }


        //searches through children and checks if the wanted node is among children
        if (node.hasChildNodes()) {
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                child = node.getChildNodes().item(i);
                if (child.getNodeName().equals(nName)) {
                    seek.add(child);

                    //if children have children !
                } else if (child.hasChildNodes()) {
                    for (int a = 0; a < child.getChildNodes().getLength(); a++) {
                        child_2 = child.getChildNodes().item(a);
                        if (child_2.getNodeName().equals(nName)) {
                            seek.add(child_2);


                            //and so on...!
                        } else if (child_2.hasChildNodes()) {
                            for (int b = 0; b < child_2.getChildNodes().getLength(); b++) {
                                child_3 = child_2.getChildNodes().item(b);
                                if (child_3.getNodeName().equals(nName)) {
                                    seek.add(child_3);

                                } else if (child_3.hasChildNodes()) {
                                    for (int c = 0; c < child_3.getChildNodes().getLength(); c++) {
                                        child_4 = child_3.getChildNodes().item(c);
                                        if (child_4.getNodeName().equals(nName)) {
                                            seek.add(child_4);


                                        } else if (child_4.hasChildNodes()) {
                                            for (int d = 0; d < child_4.getChildNodes().getLength(); d++) {
                                                child_5 = child_4.getChildNodes().item(d);
                                                if (child_5.getNodeName().equals(nName)) {
                                                    seek.add(child_5);

                                                } else if (child_5.hasChildNodes()) {
                                                    for (int e = 0; e < child_5.getChildNodes().getLength(); e++) {
                                                        child_6 = child_5.getChildNodes().item(e);
                                                        if (child_6.getNodeName().equals(nName)) {
                                                            seek.add(child_6);

                                                        }

                                                    }

                                                }

                                            }


                                        }


                                    }


                                }

                            }


                        }

                    }


                }


            }

        }


        return seek;
    }


    /**
     * this method breaks a node as much as needed until it reaches the wanted child node
     * with the desired name
     *
     * @param node
     * @param nName
     * @return a single node
     */
    //gives us only one node from a (possibly) list of nodes with a specific name
    public static Node getXMLNode(Node node, String nName) {
        List<Node> nodeGroup = getXMLNodes(node, nName);
        if (nodeGroup.size() > 0) {
            return nodeGroup.stream().findFirst().get();
        }
        return null;
    }
}
