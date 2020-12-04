package iraws_group;

public class Constants {
    public static final String INDEX_FLAG = "-i";
    public static final String SEARCH_FLAG = "-s";

    public static final String REL_TOPICS_LOC = "data//topics.txt";

    public static final String OPEN_TOP_TAG = "<top>";
    public static final String NUM_TAG = "<num>";
    public static final String TITLE_TAG = "<title>";
    public static final String DESC_TAG = "<desc>";
    public static final String NARR_TAG = "<narr>";
    public static final String CLOSE_TOP_TAG = "</top>";

    public static enum Tag {
        None,
        Open,
        Num,
        Title,
        Desc,
        Narr,
        Close
    }
}
