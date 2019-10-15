package timo.jyu;

import uk.me.berndporr.iirj.Butterworth;	//Filtering iirj package https://github.com/berndporr/iirj

public class AliasingFilter{
	
	private int transientLength;
	private Butterworth aliasingFilter = null;
	
	/**
		//Actigraph anti-aliasing filter 4th order butterworth bandpass filter
		@param bandLimits [highPassFreq, lowPassFreq]
		@param sRate sampling rate
	*/
	public AliasingFilter(double[] bandLimits,double sRate){
		transientLength = 10* (int) Math.round(1d/bandLimits[0]*sRate);
		aliasingFilter = new Butterworth();
		aliasingFilter.bandPass(4, sRate, (bandLimits[1]-bandLimits[0])/2d+bandLimits[0],(bandLimits[1]-bandLimits[0]));
			
		
	}
	
	/**filter the signal backwards and forwards. Results in zero-lag filter with 2* original filter order
		Mirroring the start of the signal is used to initialise the filter to start both directions of the filter
	*/
	public double[] filtfilt(double[] a){
		
		//Initialise the filter state by backward filtering (use either the whole signal or transientLenght samples)
		int initBackwardSamples = a.length < transientLength ? a.length : transientLength;
		for (int i = initBackwardSamples-1; i>=0; --i){
			aliasingFilter.filter(a[i]);
		}
		double[] b = new double[a.length];
		/*Forward filter*/
		for (int i = 0; i<a.length; ++i){
			b[i] = aliasingFilter.filter(a[i]);
		}
		/*Backward filter*/
		//Initialise the filter state by backward filtering (use either the whole signal or transientLenght samples)
		aliasingFilter.reset();	//reset the state of the filter
		for (int i =b.length-initBackwardSamples; i< b.length; ++i){
			aliasingFilter.filter(b[i]);
		}
		for (int i = a.length-1; i>=0; --i){
			b[i] = aliasingFilter.filter(b[i]);
		}
		return b;
	}
	
	/**filter the signal*/
	public double[] filt(double[] a){
		double[] b = new double[a.length];
		/*Forward filter*/
		for (int i = 0; i<a.length; ++i){
			b[i] = aliasingFilter.filter(a[i]);
		}
		return b;
	}
	
}