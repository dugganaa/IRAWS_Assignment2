package iraws_group;
/**
 * JelinekMercer smoothing (JM) is a linear interpolation smoothing method for query likelihood ranking. It 
 * takes a value lambda, 0 < λ <= 1, which adjusts how each term in the query affects the score for each document. 
 * A smaller lambda increases the importance of each term's individual frequency, while a large lambda value places
 * more importance on matching the terms in a query. Dirichlet Smoothing (DIR) also has a weighting value, μ, which
 * has a similar affect on scoring as λ has on JM. 
 * 
 * To test different weighting values, a new index must be created each iteration. This can take quite a while,
 * especially when other code adjustments/improvements are to be done. CustomRuns.java runs iterations of JM and DIR,
 * queries these indexes and creates a results file, so can be run on the AWS instance in the background while changes continue
 * to be made 
 */
public class CustomRuns {
    private static final float LAMBDA_START = 0.05f;
    private static final float LAMBDA_FINISH = 1.0f;
    private static final float LAMBDA_INCREMENTS = 0.05f;

    private static final float MU_START = 500f;
    private static final float MU_FINISH = 900f;
    private static final float MU_INCREMENTS = 100f;
    public static void main(String[] args) {

        //JM
        for (float lambda = LAMBDA_INCREMENTS; lambda <= 1.0f; lambda += LAMBDA_INCREMENTS) {
            System.out.println("Running JM with lambda: " + Float.toString(lambda));
            Index.main(new String[]{ "1", Integer.toString(Constants.SimilarityClasses.LMJelinekMercer.ordinal()), Float.toString(lambda)});
            Search.main(new String[]{ "2", Integer.toString(Constants.SimilarityClasses.LMJelinekMercer.ordinal()), Float.toString(lambda), "jm-" + Float.toString(lambda) + ".txt"});
        }

        //Dirichlet
        for (float mu = MU_START; mu <= MU_FINISH; mu += MU_INCREMENTS) {
            System.out.println("Running Dirichlet with mu: " + Float.toString(mu));
            Index.main(new String[]{ "1", Integer.toString(Constants.SimilarityClasses.Dirichlet.ordinal()), Float.toString(mu)});
            Search.main(new String[]{ "2", Integer.toString(Constants.SimilarityClasses.Dirichlet.ordinal()), Float.toString(mu), "dir-" + Float.toString(mu) + ".txt"});
        }
    }
}
