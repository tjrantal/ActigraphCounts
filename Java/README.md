A Java port of Jan Brond's Matlab code to produce Actigraph counts from raw accelerometer records.

Written by Timo Rantalainen 2019 (tjrantal at gmail dot com).

This Java port is released to the public domain with a CC0-BY Creative Commons Attribution license	

Depends on the IIR java filter library https://github.com/berndporr/iirj written by Bernd Porr [http://www.berndporr.me.uk] for Butterworth filtering
Depends on Apache Math 3.6.1 library

Use gradle to build (gradle build in this folder). Gradle will download the iirj library from maven repository. Once the library is built, test with Matlab (src/matlabTest/TestActigraph.m) to confirm you get the same results from the Matlab implementation and this Java port. The first few and last seconds will likely differ between the implementations due to the Java butterworth filter zero-lag implementation differing from Matlab filtfilt function.
