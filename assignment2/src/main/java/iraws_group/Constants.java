package iraws_group;

public class Constants {

    public static final String[] STOPS = {"a","an", "and", "are", "as", "at", "be", "but", "by","for", "if", "in",
            "into", "is", "it","no", "not", "of", "on", "or", "such","that", "the", "their", "then", "there", "these","they", 
            "this", "to", "was", "will", "with"};
    

    /**
     * Locations of resources
     */
    public static final String REL_TOPICS_LOC = "data//topics.txt";
    public static final String LAT_LOC = "data//latimes";
    public static final String FBIS_LOC = "data//fbis";
    public static final String FT_LOC = "data//ft";
    public static final String FR_LOC = "data//fr94";
    public static final String INDEX_LOC = "index";
    public static final String RESULTS_LOC = "results//results.txt";
    public static final String RESULTS_FOLDER_LOC = "results";
    public static final String DEFAULT_LOGS_FOLDER = "logs";

    /**
     * Query Tags
     */
    public static final String OPEN_TOP_TAG = "<top>";
    public static final String NUM_TAG = "<num>";
    public static final String TITLE_TAG = "<title>";
    public static final String DESC_TAG = "<desc>";
    public static final String NARR_TAG = "<narr>";
    public static final String CLOSE_TOP_TAG = "</top>";

    public static enum Corpora {
        LAT,
        FBIS,
        FTL,
        FR
    }

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
        DOCNO,
        DATE,
        HEADLINE,
        AUTHOR,
        TEXT
    }

    public static enum SimilarityClasses {
		BM25,
        VSM,
		Dirichlet,
		LMJelinekMercer
	}
}
