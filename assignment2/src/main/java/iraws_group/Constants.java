package iraws_group;

public class Constants {

    public static final String REL_TOPICS_LOC = "data//topics.txt";
    public static final String LAT_LOC = "data//latimes";
    public static final String FBIS_LOC = "data//fbis";
    public static final String FT_LOC = "data//ft";
    public static final String FR_LOC = "data//fr94";
    public static final String INDEX_LOC = "index";
    public static final String RESULTS_LOC = "results//results.txt";

    /*
     *  All doc tags
     */ 

    public static final String DOC_OPEN = "<DOC>";
    public static final String DOC_CLOSE = "</DOC>";
    public static final String DOC_NO_OPEN = "<DOCNO>";
    public static final String DOC_NO_CLOSE = "</DOCNO>";
    public static final String TEXT_OPEN = "<TEXT>";
    public static final String TEXT_CLOSE = "</TEXT>";
    public static final String BYLINE_OPEN = "<BYLINE>";
    public static final String BYLINE_CLOSE = "</BYLINE>";
    public static final String DATE_OPEN = "<DATE>";
    public static final String DATE_CLOSE = "</DATE>";
    public static final String HEADLINE_OPEN = "<HEADLINE>";
    public static final String HEADLINE_CLOSE = "</HEADLINE>";

    /*
     * FBIS tags
     */
    public static final String FBIS_DATE_OPEN = "<DATE1>";
    public static final String FBIS_DATE_CLOSE = "</DATE1>";
    public static final String FBIS_HEADLINE_OPEN = "<H3>";
    public static final String FBIS_HEADLINE_CLOSE = "</TI></H3>";

    /*
     *  Query tags
     */

    public static final String OPEN_TOP_TAG = "<top>";
    public static final String NUM_TAG = "<num>";
    public static final String TITLE_TAG = "<title>";
    public static final String DESC_TAG = "<desc>";
    public static final String NARR_TAG = "<narr>";
    public static final String CLOSE_TOP_TAG = "</top>";

    public static enum QueryTag {
        None,
        Open,
        Num,
        Title,
        Desc,
        Narr,
        Close
    }

    public static enum DocTag {
        None,
        ID,
        DATE,
        HEADLINE,
        BYLINE,
        TEXT
    }
}
