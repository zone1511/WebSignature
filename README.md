# On-line Signature Verification for the Web
=====================================

In this project, we propose to design and implement an automatic on-line signature verification system.
The system is built as a web application; the client is a javascript/html5 interface which can be run in any modern web browser,
 on any device (computer, tablet and smartphone), using either signature by finger/stylus on touchscreen, 
 or a graphics tablet. 
The server implements the enrollment and verification algorithms on top of a database, 
using both local features from the signature (position, speed, acceleration, etc.) 
to train some continuous Hidden Markov models and global features (length, average speed, etc.)
to train some Gaussian Mixture models. 
This architecture allows to obtain a powerful mobile and cross-device system, with a high usability for both the users and the operator.

