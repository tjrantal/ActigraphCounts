%%Matlab file to test whether Java port and Brond ActigraphCounts matlab implementation give the same result
%%Spoiler, the results are not exactly the same because the java
%%implementation does not initialise filter states similarly to matlab

fclose all;
close all;
clear all;
clc;

addpath('functions');
addpath('../../../Matlab');	%Brond Matlab implementation
load('../../../Matlab/agcoefficients.mat');	%Load the filter coefficients
if exist('OCTAVE_VERSION', 'builtin') 
	pkg load signal	%Filter functions (butter) are in the signal package
	javaaddpath('../../build/libs/BrondActigraphCounts-1.0.jar')
else
	javaclasspath('../../build/libs/BrondActigraphCounts-1.0.jar'); %Add the java implementation into classpath
end
%Read a sample file recorded with a smartphone worn in front pocket. The file contains data from roughly a 25 min jog.
dataFolder = '../res/';
accFile = getFilesAndFolders([dataFolder]);
data = readLog([dataFolder accFile(1).name]);
sRate = round(1/median(diff(data.data(:,1)./1000))); %Time stamps are milliseconds
acc = data.data(:,2:4)./9.81;	%Acceleration in g
resultant = sqrt(sum(acc.^2,2));
%resample to 30 Hz
t = ([1:length(resultant)]-1)./sRate;
%t = [0:t100(end)*30]/30;
%resultant = interp1(t100,resultant100,t);
%javaAGC = javaObject('timo.jyu.ActigraphCounts',sRate);
%countsJ = javaMethod('getCounts',javaAGC);


%Calculations with matlab

%Apply aliasing filter (agfilt does not apply aliasing filter if sRate ~=
%30 Hz
%if exist('OCTAVE_VERSION', 'builtin') 
%	[b,a] = butter(4,[0.1 7]/(sRate/2));
%else
%	[b,a] = butter(4,[0.1 7]/(sRate/2),'bandpass');
%end
%aRes = filtfilt(javaMethod('getB',javaAGC),javaMethod('getA',javaAGC),resultant);
%keyboard;
if size(resultant,2) > size(resultant,1)
	resultant = resultant';
end
countsMat = agfilt(resultant,100,B,A);
difference = countsMat-countsJ;
disp(sprintf('Counts diffMax %f diffMin %f meanDiff %f',max(difference),min(difference),mean(difference)));

%keyboard;
indices = 1:sRate:length(t);

fh = figure
ah =  []; cnt = 0;
subplot(2,1,1)
plot(t,resultant,'k','linewidth',3);
title('Resultant acceleration');
xlabel('Time [s]');
ylabel('[g]');
cnt = cnt+1; ah(cnt) = gca();
subplot(2,1,2)
plot(t(indices),countsMat,'k','linewidth',3);
hold on;
plot(t(indices(1:length(countsJ))),countsJ,'r','linewidth',3);
xlabel('Time [s]');
ylabel('one second count sums [count]');
cnt = cnt+1; ah(cnt) = gca();
linkaxes(ah,'x');
waitfor(fh);
