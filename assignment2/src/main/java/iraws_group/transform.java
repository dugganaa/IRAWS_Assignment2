import java.io.*;


public class transform {
    public static void main(String[] args) {
        createBothTitleAndDescQuery.create();
    }
}

class createOnlyDescQuery {
    public static void create() {
        String file_path = "src/resources/topics";
        try {
            int count = 1;
            boolean readable = false;
            BufferedReader in = new BufferedReader(new FileReader(file_path));
            String str;
            String content = "";
            while ((str = in.readLine()) != null) {
                if (str.equals("")) {
                    if (readable) {
                        String temp = ".I " + count + "\n" + ".W\n" + content + "\n";
                        count++;
                        String fileName = "src/resources/queries_onlyDesc.qry";
                        OutputStream outputStream = new FileOutputStream(fileName, true);
                        byte[] b = temp.getBytes();
                        for (int i = 0; i < b.length; i++) {
                            outputStream.write(b[i]);
                        }
                        readable = false;
                        content = "";
                    } else {
                        continue;
                    }
                } else {
                    if (str.contains("<desc>")) {
                        readable = true;
                    } else {
                        if (readable) {
                            content += str;
                        } else {
                            continue;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class createOnlyTitleQuery {
    public static void create() {
        String file_path = "src/resources/topics";
        try {
            int count = 1;
            BufferedReader in = new BufferedReader(new FileReader(file_path));
            String str;
            while ((str = in.readLine()) != null) {
                if (str.contains("<title>")) {
                    int len = "<title>".length();
                    String content = str.substring(len);
                    String temp = ".I " + count + "\n" + ".W\n" + content + "\n";
                    count++;
                    String fileName = "src/resources/queries_onlyTitle.qry";
                    OutputStream outputStream = new FileOutputStream(fileName, true);
                    byte[] b = temp.getBytes();
                    for (int i = 0; i < b.length; i++) {
                        outputStream.write(b[i]);
                    }
                } else {
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class createBothTitleAndDescQuery {
    public static void create() {
        String file_path = "src/resources/topics";
        try {
            int count = 1;
            boolean readable = false;
            BufferedReader in = new BufferedReader(new FileReader(file_path));
            String str;
            String content = "";
            while ((str = in.readLine()) != null) {
                if (str.equals("")) {
                    if (readable) {
                        String temp = ".I " + count + "\n" + ".W\n" + content + "\n";
                        count++;
                        String fileName = "src/resources/queries_TitleAndDesc.qry";
                        OutputStream outputStream = new FileOutputStream(fileName, true);
                        byte[] b = temp.getBytes();
                        for (int i = 0; i < b.length; i++) {
                            outputStream.write(b[i]);
                        }
                        readable = false;
                        content = "";
                    } else {
                        continue;
                    }
                } else {
                    if(str.contains("<title>")){
                        int len = "<title>".length();
                        String title_content = "title:"+str.substring(len)+"\n";
                        content+=title_content;
                    }else{
                        if (str.contains("<desc>")) {
                            readable = true;
                        } else {
                            if (readable) {
                                content += str;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
