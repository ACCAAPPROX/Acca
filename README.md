# Approximate Classifiers with Controlled Accuracy 

This repository contains a code to reproduce the experimental evaluation for "Approximate Classifiers with Controlled Accuracy".

## Run and compile: 

`javac Main.java` - compile

`java Main arg1 arg2` - run 

The parameter arg1 is `fib` or `gen` depending on the type of a classifier, arg2 is a file name containing a classifier. In the case of `gen`, the file with a classifier should be in a ClassBench format, in the case of `fib` in the following format: each line is a rule starting with IPv4 adress and followed by an action which is separated by tab. Resulting statistic will be written into the file with the name `ans_`+arg2.
 
