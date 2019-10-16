package timo.jyu;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import timo.jyu.filter.ButterworthCoefficients;

public class Utils{
	//Returns 4th order band pass butterworth filter coefficients
	//http://www-users.cs.york.ac.uk/~fisher/mkfilter/
	public static double[][] getBandPassButterworthCoefficients(double[] corners, double sRate){
		ButterworthCoefficients b = new ButterworthCoefficients();
		b.butter(new String[]{"Bu","Bp","o","4","a",Double.toString(0.1/sRate),Double.toString(7d/sRate)});
		double[][] ret = new double[2][];
		ret[0] = new double[b.xcoeffs.length];
		ret[1] = new double[b.ycoeffs.length];
		//Divide the xcoeffs (b in matlab) with gain, and reverse the order of ycoeffs(a in matlab)
		for (int i = 0;i<b.xcoeffs.length;++i){
			ret[0][i] =b.xcoeffs[i]/b.gain;
			ret[1][b.ycoeffs.length-1-i] =-1d*b.ycoeffs[i];
		}
		return ret;
	}
	
	/**
		Filter without initial state (=set initial state to all zeros)
	*/
	public static double[] filter(double[] b, double[] a, double[] signal){
		return filter(b,a,signal,new double[a.length-1]);
	}
	
	/**
		Filter with initial state
		ported from https://github.com/greenm01/forc/blob/master/sandbox/filter.m
	*/
	public static double[] filter(double[] b, double[] a, double[] signal,double[] state){
		double[] output = new double[signal.length];
		for (int i = 0;i<output.length;++i){
			output[i] = state[0]+b[0]*signal[i]; //Next output does not depend on a coeffs
			//Update state vector after the current output is known
			
			/*Update state*/
			for (int j = 1; j<state.length;++j){
				/*
				w(1:(lw-1)) = w(2:lw) - a(2:lw)*y(index) + b(2:lw)*x(index);
       		 w(lw) = b(MN)*x(index) - a(MN) * y(index);
				
				*/
				state[j-1] = state[j]-a[j]*output[i]+b[j]*signal[i];
			}
			state[state.length-1]=b[state.length]*signal[i]-a[state.length]*output[i];		
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
