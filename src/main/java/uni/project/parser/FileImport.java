package uni.project.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.texttechnologylab.utilities.helper.FileUtils;
import org.texttechnologylab.utilities.helper.TempFileHandler;
import uni.project.MainClass;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This Class helps to import the necessary files (such as XMLs).
 *
 * @author Siamak Choromkheirabadi
 */
public class FileImport {


    /**
     * downloads the dtd-file and ads it to a folder called xml
     *
     * @throws IOException
     */
    public static void dtd() throws IOException {
        //link to dtd-file
        URL url = new URL("https://www.bundestag.de/resource/blob/575720/70d7f2af6e4bebd9a550d9dc4bc03900/dbtplenarprotokoll-data.dtd");
        InputStream inputStream = url.openStream();
        //it will be stored in a folder called xml
        FileOutputStream fileOutputStream = new FileOutputStream("xml/dbtplenarprotokoll.dtd");
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, length);
        }
        File file = new File("xml/dbtplenarprotokoll.dtd");
        //file will be deleted if program terminates
        file.deleteOnExit();


        fileOutputStream.close();
        inputStream.close();
        System.out.println("dtd File has been successfully downloaded !");

    }


    /**
     * downloads all XMLs from the BundesTag Website and adds them to a specific list
     *
     * @return
     * @throws IOException
     */
    public static List<File> xmlWeb() throws IOException {


        int h = 0;
        int offSet = 0;

        List<File> xmlFiles = new ArrayList<>(0);
        String uri = "";


        for (int j = 0; j < 2; j++) {
            if (j == 0) {
                offSet = 80;
                //link to wp20
                uri = "https://www.bundestag.de/ajax/filterlist/de/services/opendata/866354-866354?limit=10&noFilterSet=true&offset=0";

            }
            if (j == 1) {
                offSet = 230;
                //link to wp19
                uri = "https://www.bundestag.de/ajax/filterlist/de/services/opendata/543410-543410?limit=10&noFilterSet=true&offset=0";

            }


            for (int i = 0; i <= offSet; i = i + 10) {
                //this whole operation is similar to MPs() function so no more comments !
                Document document = Jsoup.connect(uri).timeout(10000).get();
                Elements body = document.select("tbody");
                uri = uri.replaceAll(("offset=" + i), "offset=" + (i + 10));

                for (Element e : body.select("tr")) {
                    h++;
                    String s = "https://www.bundestag.de" + (e.select("a.bt-link-dokument").attr("href"));

                    URL url = new URL(s);
                    InputStream inputStream = url.openStream();
                    FileOutputStream fileOutputStream = new FileOutputStream("xml/Protocol " + h + ".xml");
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                    //a new file will be created internally
                    File file = new File("xml/Protocol " + h + ".xml");
                    //adds the file if it ends with XML or xml
                    if ((file.getName().endsWith("xml")) || ((file.getName().endsWith("XML")))) {
                        xmlFiles.add(file);
                        //file.deleteOnExit();
                    }
                    //System.out.println(xmlFiles);
                    System.out.println("Downloaded XMLs : " + xmlFiles.size());
                    fileOutputStream.close();
                    inputStream.close();

                }

            }
        }


        return xmlFiles;
    }


    /**
     * This method has been taken from the sample solution.
     * it downloads the MDBXML in zip format and unzips it and adds it to a list
     *
     * @return
     * @throws IOException
     */
    public static List<File> unzipFile() throws IOException {

        File zippedFile = null;

        zippedFile = FileUtils.downloadFile("https://www.bundestag.de/resource/blob/472878/4d360eba29319547ed7fce385335a326/MdB-Stammdaten-data.zip");

        ZipInputStream zis = new ZipInputStream(new FileInputStream(zippedFile));
        ZipEntry zipEntry = zis.getNextEntry();
        byte[] buffer = new byte[1024];

        List<File> list = new ArrayList<>(0);

        while (zipEntry != null) {
            File tFile = TempFileHandler.getTempFileName(zipEntry.getName());
            tFile.deleteOnExit();

            FileOutputStream fos = new FileOutputStream(tFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            if ((tFile.getName().endsWith("xml")) || ((tFile.getName().endsWith("XML")))) {

                list.add(tFile);

            }

            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
        System.out.println("MDB File has been successfully downloaded !");

        return list;
    }


    /**
     * This method imports our xml files from a directory whose path is
     * given through the args of main method. It only
     * imports XML files.
     *
     * @param path
     * @return
     */
    public static List<File> importXML(String path) {

        String suffix = "xml";
        File address = new File(path);
        File[] files = new File[0];
        List<File> xmlStuff = new ArrayList<>(0);
        //if the path leads to a folder, files within it will be stored in  a list
        if (address.isDirectory()) {
            files = address.listFiles();
        }
        for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
            if ((files[i].getName().endsWith(suffix)) || (files[i].getName().endsWith("XML"))) {
                xmlStuff.add(files[i]);
            }
        }
        //if the path leads directly to XML file, stores it.
        if (address.isFile()) {
            if ((address.getName().endsWith("XML")) || ((address.getName().endsWith(suffix)))) {
                xmlStuff.add(address);
            }
        }

        return xmlStuff;
    }


    /**
     * thsi method checks if the given path is actually a file (or directory)
     *
     * @param path
     * @return
     */
    public static boolean fileChecker(String path) {

        File address = new File(path);
        if (address.isFile() && (address.getName().endsWith("xml") || address.getName().endsWith("XML"))) {
            return true;

        }
        if (((address.listFiles()).length > 2)) {
            return true;

        } else {
            return false;

        }

    }

    /**
     * This method downloads all the MP-photos from the BundesTag web and adds them
     * to a document and documents to a list
     *
     * @return
     * @throws IOException
     */
    public static Set<org.bson.Document> MPs() throws IOException {

        Set<org.bson.Document> docs = new HashSet<>();
        //link of photos in BundesTag-server
        String uri = "https://www.bundestag.de/ajax/filterlist/de/abgeordnete/biografien/862712-862712?limit=20&noFilterSet=true&offset=0";
        System.out.println("Starting to import photos and their MetaData");

        for (int i = 0; i <= 720; i = i + 20) {

            System.out.println((i + 20) + " Photos downloaded !");
            //Jsoup object; timeout after 10 sec...
            org.jsoup.nodes.Document document = Jsoup.connect(uri).timeout(10000).get();
            //changes offset so all the photos could be downloaded
            uri = uri.replaceAll(("offset=" + i), "offset=" + (i + 20));
            //finds node "body" in JavaScript
            Elements elements = document.select("body");
            //iterates through nodes with the given name
            for (Element e : elements.select("div.col-xs-4.col-sm-3.col-md-2.bt-slide")) {

                org.bson.Document doc = new org.bson.Document();
                //link to photo
                String photoLink = "https://www.bundestag.de" + e.select("div.bt-bild-standard  img").attr("data-img-md-retina");
                //name of the selected mp
                String name = e.select("div.bt-slide-content  a").attr("title");
                //id of the selected mp
                String id = e.select("div.bt-slide-content  a").attr("data-id");
                //link to metadata
                String metaData = "https://www.bundestag.de" + e.select("div.bt-slide-content  a").attr("href");
                //fraction of the mp
                String frac = e.select("p.bt-person-fraktion").text();

                //adds stuff to the bson document
                doc.put("_id", id);
                doc.put("PhotoLink", photoLink);
                doc.put("FullName", name);
                doc.put("MetaDataLink", metaData);
                doc.put("Fraction", frac);
                doc.put("SpeakerID", "");

                docs.add(doc);


            }

        }


        return docs;
    }


}
