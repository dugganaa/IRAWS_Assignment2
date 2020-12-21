package iraws_group;


import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import iraws_group.Constants;
import iraws_group.ModifiableTokenizer;
import org.apache.lucene.analysis.Analyzer;

import java.util.*;
import java.io.File;
import java.io.BufferedReader;
import java.util.Scanner;
import java.io.StringReader;

public class Main{

    /*
     * Args:
     * arg[0]: Index/Search
     * arg[1]: LSM function (optional)
     * arg[2]: lambda (optional)
     * arg[3]: Custom results file name (optional)
     */
    public static void main(String[] args) {
        if (args.length < 1 || (Integer.parseInt(args[0]) != 1 && Integer.parseInt(args[0]) != 2)) {
            System.out.println("Please specifiy index (1) or search (2)");
        }
        else if (Integer.parseInt(args[0]) == 1){
            System.out.println("Hello I am index");
            Index.main(args);
        } else if (Integer.parseInt(args[0]) == 2){
            System.out.println("Hello i am search");
            Search.main(args);
        }
    }
}
