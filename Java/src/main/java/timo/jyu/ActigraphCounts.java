package timo.jyu;

/*
	Java port of Jan Brønd's Actigraph counts from raw accelerations
	Written by Timo Rantalainen tjrantal at gmail dot com 2019
	This Java port released to the public domain with a CC0-BY Creative Commons Attribution license	
	
	Depends on the IIR filter library https://github.com/berndporr/iirj written by Bernd Porr [http://www.berndporr.me.uk] for Butterworth filtering
*/

public class ActigraphCounts{
	//Coeffs from R code
	private static final double[] A = new double[]{1,-4.1637,7.5712,-7.9805,5.385,-2.4636,0.89238,0.06361,-1.3481,2.4734,-2.9257,2.9298,-2.7816,2.4777,-1.6847,0.46483,0.46565,-0.67312,0.4162,-0.13832,0.019852};
	private double[] B = new double[]{0.049109,-0.12284,0.14356,-0.11269,0.053804,-0.02023,0.0063778,0.018513,-0.038154,0.048727,-0.052577,0.047847,-0.046015,0.036283,-0.012977,-0.0046262,0.012835,-0.0093762,0.0034485,-0.00080972,-0.00019623};
	public static final double adcResolution = 0.0164;
	public static final double gain = 0.965;
	public static final double deadband = 0.068;
	public static final double maxTrunc = 2.13;
	public static final int integN = 10;
	double[] counts = null;
	double[][] afCoeffs = null;
		
	public ActigraphCounts(double sRate){
		//AliasingFilter alf = new AliasingFilter(new double[]{0.1, 7d},sRate);
		afCoeffs =  Utils.getBandPassButterworthCoefficients(new double[]{0.1,7d}, sRate);
		for (int i=0;i<B.length;++i){B[i]*=gain;}	//Apply ag filter gain on B coeffs		
	}
	
	/*
		aFilt = getAFiltered(afCoeffs[0],afCoeffs[1],resultant)
		down30 = getInterpolated(t,aFilt,sRate)
		agFilt = Utils.filter(B,A,down30)
		down_10 = down10(agFilt)
		trunc8bit = pptruncDeadband8bit(down_10,maxTrunc,deadband,adcResolution)
		counts = calcCounts(trunc8bit);
		
	*/
	
	public double[] getCounts(double[] a){
		counts = calcCounts(a);
		return counts;
	}
	
	public void setA(double[] a){
		afCoeffs[1] = new double[a.length];
		for (int i = 0; i<a.length; ++i){afCoeffs[1][i] = a[i];}
	}
	
	public void setB(double[] a){
		afCoeffs[0] = new double[a.length];
		for (int i = 0; i<a.length; ++i){afCoeffs[0][i] = a[i];}
	}
	
	public double[] getA(){
		return afCoeffs[1];
	}
	
	public double[] getB(){
		return afCoeffs[0];
	}
	
	public double[] getCounts(){
		return counts;
	}
	
	private double[] getT(int length, double sRate){
		double[] t = new double[length];
		for (int i = 0; i< length; ++i){
			t[i] = ((double) i)/sRate;
		}
		return t;
	}
	
	public double[] getInterpolated(double[] afiltered,double sRate){
		//Resample the signal to 30 Hz
		double[] t = getT(afiltered.length,sRate);
		double[] intT = getT((int) (Math.floor(t[t.length-1]*30d))+1,30d);
		return Utils.interp(t,afiltered,intT);
	}
	
	public double[] getAFiltered(double[] resultant){
		return Utils.filtfilt(afCoeffs[0],afCoeffs[1],resultant);
	}
	
		public double[] getAGFiltered(double[] down30){
		return Utils.filter(B,A,down30);
	}
	
	//AG counts are calculated based on 10 Hz data. Downsample 30 Hz to 10 Hz
	//By taking every 3rd sample
	public double[] down10(double[] a){
		double[] b = new double[(int) (Math.floor(((double)a.length)/3d))];
		for (int i = 0; i< b.length; ++i){
			b[i] = a[3*i];
		}
		return b;
	}
	
	public double[] pptruncDeadband8bit(double[] a, double maxMagnitude, double dbMagnitude,double adb){
		double[] b = new double[a.length];
		for (int i = 0; i< b.length; ++i){
			b[i] = Math.abs(a[i]) <=maxMagnitude ? a[i] : Math.signum(a[i])*maxMagnitude;	//Truncate max val
			b[i] = Math.abs(b[i]) >=dbMagnitude ? Math.abs(b[i]) : 0d;	//Apply deadband and rectify
			b[i] = Math.floor(b[i]/adb);	//8-bit values
		}
		return b;
	}
	
	//Calculate 10 sample integrals
	public double[] calcCounts(double[] a){
		double[] b = new double[(int) (Math.ceil(((double) a.length)/((double) integN)))];
		for (int i = 0;i<b.length;++i){
			for (int j = 0;j<integN;++j){
				if (integN*i+j <a.length){
					b[i]+=a[integN*i+j];
				}
			}
		}
		return b;	
	}
}
