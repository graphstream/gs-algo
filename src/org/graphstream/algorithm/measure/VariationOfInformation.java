package org.graphstream.algorithm.measure;

public class VariationOfInformation extends NormalizedMutualInformation {

	public VariationOfInformation(String marker) {
		super(marker);
	}

	public VariationOfInformation(String marker, String referenceMarker) {
		super(marker, referenceMarker);
	}

	@Override
	/**
	 * B.Karrer, E.Levina and M.E.J.Newman, 
	 * RobustnessofCommunity Structure in Networks, 
	 * Physical Review E (Statistical, Nonlinear, and Soft Matter Physics), 
	 * vol. 77, no. 4, 2008.
	 */
	public void compute() {
		// Get the updated confusion matrix
		int[][] N = confusionMatrix();

		// Get the arrays of the rows and columns sums
		int[] N_A = new int[referenceCommunities.size()];
		int[] N_B = new int[communities.size()];
		for (int i = 0; i < N_A.length; i++) {
			int ttl = 0;
			for (int j = 0; j < N_B.length; j++)
				ttl += N[i][j];
			N_A[i] = ttl;
		}
		for (int j = 0; j < N_B.length; j++) {
			int ttl = 0;
			for (int i = 0; i < N_A.length; i++)
				ttl += N[i][j];
			N_A[j] = ttl;
		}

		// Get the total nodes number
		float n = graph.getNodeCount();

		/*
		 * Let's go and compute the NMI
		 */
		float voi = 0;

		for (int i = 0; i < N_A.length; i++)
			for (int j = 0; j < N_B.length; j++)
				voi += N[i][j]
						* (Math.log((float) N[i][j] / (float) N_B[j]) + Math
								.log((float) N[i][j] / (float) N_A[i]));
		M = (-1 / n) * voi;

	}

}
