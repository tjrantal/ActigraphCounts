package timo.jyu;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import timo.jyu.filter.ButterworthCoefficients;

public class Utils{
	//Returns 4th order band pass butterworth filter coefficients
	//http://www-users.cs.york.ac.uk/~fisher/mkfilter/
	public static double[][] getBandPassButterworthCoefficients(double[] corners, double sRate){
		ButterworthCoefficients b = new ButterworthCoefficients();
		b.butter(new String[]{"Bu","Bp","o","4","a",Double.toString((0.1/sRate),Double.toString((7d/sRate)});
		double[][] ret = new double[2][];
		ret[0] = new double[b.xcoeffs.length];
		ret[1] = new double[b.ycoeffs.length];
		//Divide the xcoeffs (b in matlab) with gain, and reverse the order of ycoeffs(a in matlab)
		for (int i = 0;i<b.xcoeffs.length;++i){
			ret[0][i] =b.xcoeffs[i]/b.gain;
			ret[1][b.ycoeffs.length-1-i] =b.ycoeffs[i];
		}
		return ret;
	}

	public static double[] filter(double[] b, double[] a, double[] signal){
		double[] output = new double[signal.length];
		for (int i = 0;i<output.length;++i){
			output[i] = 0;
			/*Sum b coeff*/
			for (int j = 0; j<b.length;++j){
				if (i-j >= 0){
					output[i]+=b[j]/a[0]*signal[i-j];
				}
			}			
			/*Sum a coeff*/
			for (int j = 1; j<a.length;++j){
				if (i-j >= 0){
						output[i]-=a[j]/a[0]*output[i-j];
				}
			}
		}
		return output;
	}

	public static double[] interp(double[] x, double[] y, double[] xx){
		double[] yy = new double[xx.length];
		PolynomialSplineFunction interpolator = (new LinearInterpolator()).interpolate(x, y);
		
		for (int i = 0;i<yy.length;++i){
			yy[i] = interpolator.value(xx[i]);
		}
		return yy;
	}
	
}
