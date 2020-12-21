package iraws_group;
/**
 * JelinekMercer smoothing (JM) and Dirichlet Smoothing (DM) can be passed a value lambda (0 < lambda <= 1)
 * where a small lambda value increases the importance of individual term weightings in a query and a large
 * lambda decreases individual weightings, but increases the importance of matching terms. (Note - In literature
 * this weighting is assigned the variable mu rather than lambda, where mu takes into account the )
 * 
 * To test different lambda values, 
 */
public class CustomRuns {
    private static final float LAMBDA_START = 0.05f;
    private static final float LAMBDA_FINISH = 1.0f;
    private static final float LAMBDA_INCREMENTS = 0.05f;

    private static final float MU_START = 1000f;
    private static final float MU_FINISH = 2000f;
    private static final float MU_INCREMENTS = 100f;
    public static void main(String[] args) {

        //JM
        for (float lambda = LAMBDA_INCREMENTS; lambda <= 1.0f; lambda += LAMBDA_INCREMENTS) {
            //System.out.println("Running jm: lambda: " + Float.toString(lambda));
            //Index.main(new String[]{ "1", Integer.toString(Constants.SimilarityClasses.LMJelinekMercer.ordinal()), Float.toString(lambda)});
            //Search.main(new String[]{ "2", Integer.toString(Constants.SimilarityClasses.LMJelinekMercer.ordinal()), Float.toString(lambda), "jm-" + Float.toString(lambda) + ".txt"});
        }

        //JM
        for (float mu = MU_START; mu <= MU_FINISH; mu += MU_INCREMENTS) {
            System.out.println("Running dirichlet: mu: " + Float.toString(mu));
            Index.main(new String[]{ "1", Integer.toString(Constants.SimilarityClasses.Dirichlet.ordinal()), Float.toString(mu)});
            Search.main(new String[]{ "2", Integer.toString(Constants.SimilarityClasses.Dirichlet.ordinal()), Float.toString(mu), "dir-" + Float.toString(mu) + ".txt"});
        }
    }
}
