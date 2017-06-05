package kmeanslsh;

import java.util.ArrayList;
import java.util.Collections;

public class NmiCalculator {
    public static double calculateNmi(DataPoint[] dataPoints) {
        ArrayList<Integer> trueClass = new ArrayList<>(dataPoints.length);
        ArrayList<Integer> predictedClass = new ArrayList<>(dataPoints.length);
        
        for(DataPoint dp : dataPoints) {
            trueClass.add(dp._trueClassification);
            predictedClass.add(dp.getCluster()._id);
        }

        double nmi = NMI(trueClass, predictedClass);
        return nmi;
    }

    // https://gist.github.com/perdacherMartin/76689fdf2c950fbeba6b013d09906de4
    public static double NMI(ArrayList<Integer> one, ArrayList<Integer> two){
		if(one.size()!=two.size()){
			throw new IllegalArgumentException("Sizes don't match!");
		}
		int maxone = Collections.max(one);
		int maxtwo = Collections.max(two);

		double[][] count = new double[maxone+1][maxtwo+1];
		//System.out.println(count[1][2]);
		for(int i=0;i<one.size();i++){
			count[one.get(i)][two.get(i)]++;
		}
		//i<maxone=R
		//j<maxtwo=C
		double[] bj = new double[maxtwo+1];
		double[] ai = new double[maxone+1];

		for(int m=0;m<(maxtwo+1);m++){
			for(int l=0;l<(maxone+1);l++){
				bj[m]=bj[m]+count[l][m];
			}
		}
		for(int m=0;m<(maxone+1);m++){
			for(int l=0;l<(maxtwo+1);l++){
				ai[m]=ai[m]+count[m][l];
			}
		}

		double N=0;
		for(int i=0;i<ai.length;i++){
			N=N+ai[i];
		}
		double HU = 0;
		for(int l=0;l<ai.length;l++){
			double c=0;
			c=(ai[l]/N);
			if(c>0){
				HU=HU-c*Math.log(c);
			}
		}

		double HV = 0;
		for(int l=0;l<bj.length;l++){
			double c=0;
			c=(bj[l]/N);
			if(c>0){
				HV=HV-c*Math.log(c);
			}
		}
		double HUstrichV=0;
		for(int i=0;i<(maxone+1);i++){
			for(int j=0;j<(maxtwo+1);j++){
				if(count[i][j]>0){
					HUstrichV=HUstrichV-count[i][j]/N*Math.log(((count[i][j])/(bj[j])));
				}
			}
		}
		double IUV = HU-HUstrichV;
		double reto = IUV/(Math.max(HU, HV));

		return reto;
	}
}
