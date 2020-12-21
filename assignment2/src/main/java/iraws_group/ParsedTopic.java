package iraws_group;

/**
 * An object for each topic in topics.txt to be parsed into.
 */
public class ParsedTopic {
    private String num;
    private String title;
    private String desc;
    private String narr;

    public ParsedTopic() {
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getNum() {
        return this.num;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setNarr(String narr) {
        this.narr = narr;
    } 

    public String getNarr() {
        return this.narr;
    }    
}
