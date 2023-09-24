# Parliament_Browser_2_Montag_2

Private gitlab repository for the final project of the PRG course.
A web based Parliament Browser. 

IMPORTANT : You'll have to give a path to xml files if you want to load the program manually.
Path must be given as an argument to the mainMethode. First path must be to the XMLs & dtd-file
and second one must lead to MDB-XML file. Otherwise, the program starts downloading XMLs from the
server. Ex. : "/Users/siamak/Documents/xml/Protokolle_Bundestag_19"
  "/Users/siamak/Documents/xml/MdB-Stammdaten-data"

WARNING : For better and faster analysing it is highly recommended to extend the RAM
of the JVM (The memory given to the virtual machine) to at least 1GB. Otherwise, the parsing
process will be extremely slow especially after loading 260 XMLs.

A friendly notice : the only bug in our program is when you enter the wrong input with API
in the first menu. Be advised !

API Documentation : https://app.swaggerhub.com/apis/s5487971/2_Montag_2_API_Doc/1.0.0