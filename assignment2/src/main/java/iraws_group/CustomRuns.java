package iraws_group;
public class CustomRuns {
    private static final float LAMBDA_INCREMENTS = 0.05f;
    public static void main(String[] args) {

        //Dir
        for (float lambda = 0f; lambda <= 1.0f; lambda += LAMBDA_INCREMENTS) {
            System.out.println("Running dir: lambda: " + Float.toString(lambda));
            Index.main(new String[]{ "1", Integer.toString(Constants.SimilarityClasses.Dirichlet.ordinal()), Float.toString(lambda) });
            Search.main(new String[]{ "2", Integer.toString(Constants.SimilarityClasses.Dirichlet.ordinal()), Float.toString(lambda), "dir-" + Float.toString(lambda) + ".txt" });
        }

        //JM
        for (float lambda = 0f; lambda <= 1.0f; lambda += LAMBDA_INCREMENTS) {
            System.out.println("Running jm: lambda: " + Float.toString(lambda));
            Index.main(new String[]{ "1", Integer.toString(Constants.SimilarityClasses.LMJelinekMercer.ordinal()) });
            Search.main(new String[]{ "2", Integer.toString(Constants.SimilarityClasses.LMJelinekMercer.ordinal()), Float.toString(lambda), "jm-" + Float.toString(lambda) + ".txt"});
        }
    }
}