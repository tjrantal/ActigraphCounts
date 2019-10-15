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
javaclasspath('../../build/libs/BrondActigraphCounts-1.0.jar'); %Add the java implementation into classpath

%Read a sample file recorded with a smartphone worn in front pocket. The file contains data from roughly a 25 min jog.
dataFolder = '../res/';
accFile = getFilesAndFolders([dataFolder]);
data = readLog([dataFolder accFile(1).name]);
sRate = round(1/median(diff(data.data(:,1)./1000))); %Time stamps are milliseconds
acc = data.data(:,2:4)./9.81;	%Acceleration in g
resultant = sqrt(sum(acc.^2,2));
javaAGC = javaObject('timo.jyu.ActigraphCounts',resultant,100);
countsJ = javaMethod('getCounts',javaAGC);


%Calculations with matlab
t = ([1:length(resultant)]-1)./sRate;
%Apply aliasing filter (agfilt does not apply aliasing filter if sRate ~=
%30 Hz
[b,a] = butter(4,[0.1 7]/(sRate/2),'bandpass');
aRes = filtfilt(b,a,resultant);

countsMat = agfilt(aRes,sRate,B,A);
indices = 1:sRate:length(t);

figure
ah =  []; cnt = 0;
subplot(2,1,1)
plot(t,resultant,'k');
title('Resultant acceleration');
xlabel('Time [s]');
ylabel('[g]');
cnt = cnt+1; ah(cnt) = gca();
subplot(2,1,2)
plot(t(indices),countsMat,'k');
hold on;
plot(t(indices(1:length(countsJ))),countsJ,'r');
xlabel('Time [s]');
ylabel('one second count sums [count]');
cnt = cnt+1; ah(cnt) = gca();
linkaxes(ah,'x');
