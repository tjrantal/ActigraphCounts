package timo.jyu;

/*
	Java port of Jan Brønd's Actigraph counts from raw accelerations
	Written by Timo Rantalainen tjrantal at gmail dot com 2019
	This Java port released to the public domain with a CC0-BY Creative Commons Attribution license	
	
	Depends on the IIR filter library https://github.com/berndporr/iirj written by Bernd Porr [http://www.berndporr.me.uk] for Butterworth filtering
*/

public class ActigraphCounts{
	//Coeffs from R code
	private double[] A = new double[]{1,-4.1637,7.5712,-7.9805,5.385,-2.4636,0.89238,0.06361,-1.3481,2.4734,-2.9257,2.9298,-2.7816,2.4777,-1.6847,0.46483,0.46565,-0.67312,0.4162,-0.13832,0.019852};
	private double[] B = new double[]{0.049109,-0.12284,0.14356,-0.11269,0.053804,-0.02023,0.0063778,0.018513,-0.038154,0.048727,-0.052577,0.047847,-0.046015,0.036283,-0.012977,-0.0046262,0.012835,-0.0093762,0.0034485,-0.00080972,-0.00019623};
	private double adcResolution = 0.0164;
	private double gain = 0.965;
	private double deadband = 0.068;
	private double maxTrunc = 2.13;
	private int integN = 10;
	double[] counts = null;
	double[][] afCoeffs = null;
		
	public ActigraphCounts(double[] resultant, double sRate){
		//AliasingFilter alf = new AliasingFilter(new double[]{0.1, 7d},sRate);
		afCoeffs =  Utils.getBandPassButterworthCoefficients(new double[]{0.1,7d}, sRate);
		//Apply ag filter gain on B coeffs
		for (int i=0;i<B.length;++i){B[i]*=gain;}		
		//Resample the signal to 30 Hz
		double[] t = getT(resultant.length,sRate);
		double[] intT = getT((int) (Math.floor(t[t.length-1]*30d))+1,30d);
		//Apply anti aliasing, down sample, etc 
		//counts = calcCounts(pptruncDeadband8bit(down10(Utils.filter(B,A,Utils.interp(t,alf.filtfilt(resultant),intT))),maxTrunc,deadband,adcResolution));
		counts = calcCounts(pptruncDeadband8bit(down10(Utils.filter(B,A,Utils.interp(t,Utils.filtfilt(afCoeffs[0],afCoeffs[1],resultant),intT))),maxTrunc,deadband,adcResolution));
		
		/*
		double[] antialiased = alf.filtfilt(resultant);
		//Resample the signal to 30 Hz
		double[] t = getT(antialiased.length,sRate);
		double[] intT = getT((int) (Math.floor(t[t.length-1]*30d))+1,30d);
		double[] downSampled = Utils.interp(t,antialiased,intT);
		
		//Apply ag filter gain on B coeffs
		for (int i=0;i<B.length;++i){B[i]*=gain;}		
		//Apply actigraph filter on the 30 Hz data		
		double[] agFiltered = Utils.filter(B,A,downSampled);
		double[] down10Sampled = down10(agFiltered);	//Downsample to 10 Hz
		double[] truncated = pptruncDeadband8bit(down10Sampled,maxTrunc,deadband,adcResolution);	//Truncate to 2.13 g
		counts = calcCounts(truncated);
		*/
		
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
	
	//AG counts are calculated based on 10 Hz data. Downsample 30 Hz to 10 Hz
	//By taking every 3rd sample
	private double[] down10(double[] a){
		double[] b = new double[(int) (Math.floor(((double)a.length)/3d))];
		for (int i = 0; i< b.length; ++i){
			b[i] = a[3*i];
		}
		return b;
	}
	
	private double[] pptruncDeadband8bit(double[] a, double maxMagnitude, double dbMagnitude,double adb){
		double[] b = new double[a.length];
		for (int i = 0; i< b.length; ++i){
			b[i] = Math.abs(a[i]) <=maxMagnitude ? a[i] : Math.signum(a[i])*maxMagnitude;	//Truncate max val
			b[i] = Math.abs(b[i]) >=dbMagnitude ? Math.abs(b[i]) : 0d;	//Apply deadband and rectify
			b[i] = Math.floor(b[i]/adb);	//8-bit values
		}
		return b;
	}
	
	//Calculate 10 sample integrals
	private double[] calcCounts(double[] a){
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
