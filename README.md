# On-line Signature Verification System for the Web

## Description

In this project, we propose to design and implement an automatic on-line signature verification system, which is able to be used on any mobile device.

The system is built as a web application; the client is a javascript/html5 interface which can be run in any modern web browser,
 on any device (computer, tablet and smartphone), using either signature by finger/stylus on touchscreen, 
 or a graphics tablet. 
 
The server implements the enrollment and verification algorithms on top of a database, 
using both local features from the signature (position, speed, acceleration, etc.) 
to train some continuous Hidden Markov models and global features (length, average speed, etc.)
to train some Gaussian Mixture models. 

This architecture allows to obtain a powerful mobile and cross-device system, with a high usability for both the users and the operator.

## Dependencies

This project is using [my fork of the Jahmm library](https://github.com/aubry74/Jahmm), which improves the original library to support Multivariate Gaussian Mixtures in order to model the emission probabilities in Hidden Markov Models. (Only single multivariate gaussian or one dimensional mixtures are supported in the original library).

## About

This project was my master semester project at EPFL. If you have any questions, contact me at aubry.cholleton@epfl.ch.



