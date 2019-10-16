package timo.jyu;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

public class Utils{
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
