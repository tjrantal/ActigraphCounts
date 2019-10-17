function counts = agfilt(data,filesf,B,A)

deadband = 0.068;

sf = 30;
peakThreshold = 2.13;
adcResolution = 0.0164;
integN = 10;
gain = 0.965;

if (filesf>sf)
    dataf = resample(data,sf,filesf);
else
    %Aliasing Filter
    [B2,A2] = butter(4,[0.01 7]./(sf/2));
    dataf = filtfilt(B2,A2,data);
end

S = size(dataf);

B = B * gain;

for n=1:S(2)
    
    fx8up = filter(B,A,dataf(:,n));
    fx810 = downsample(fx8up,3);
	
    fx8 = pptrunc(downsample(fx8up,3),peakThreshold);
    
    counts(:,n) = runsum(floor(trunc(abs(fx8),deadband)./adcResolution),integN,0);
	
	javaAGC = javaObject('timo.jyu.ActigraphCounts',30);
	%javaMethod('setB',javaAGC,B2);
	%javaMethod('setA',javaAGC,A2);

	%aFilt =javaMethod('getAFiltered',javaAGC,B2,A2,dataf(:,n));
	%down30 = javaMethod('getInterpolated',javaAGC,dataf(:,n),100);
	agFilt = javaMethod('getAGFiltered',javaAGC,dataf(:,n));
	
	figure
	subplot(2,1,1)
	plot(fx8up,'k','linewidth',3)
	hold on;
	plot(agFilt,'r','linewidth',3)
	keyboard;
	
	difference = fx8up-agFilt;
	disp(sprintf('agFilt diffMax %f diffMin %f meanDiff %f',max(difference),min(difference),mean(difference)));
	
	down_10 = javaMethod('down10',javaAGC,fx8up);
	difference = fx810-down_10;
	disp(sprintf('Down 10 diffMax %f diffMin %f meanDiff %f',max(difference),min(difference),mean(difference)));
	
	
	trunc8bit =javaMethod('pptruncDeadband8bit',javaAGC,down_10,peakThreshold,deadband,adcResolution);
	countsJ = javaMethod('calcCounts',javaAGC,trunc8bit);

	difference = counts(:,n)-countsJ;
disp(sprintf('Counts diffMax %f diffMin %f meanDiff %f',max(difference),min(difference),mean(difference)));

	
	keyboard;
    
end
    