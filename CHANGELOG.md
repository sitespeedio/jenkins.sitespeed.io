# sitespeed.io Jenkins plugin changelog

version 1.0 BETA 8
----------------------
* We now support sitespeed.io 3.X! Built in support for Graphite & WebPageTest.


version 1.0 BETA 7
----------------------
* Translate Jenkins ENV variables in arguments sent to the sitespeed scripts
* Added better check for the sitespeed home dir (check that the script exist & is executable)
* Added support for testing in multiple browsers (sitespeed.io 2.5)  
* Needs sitespeed.io version 2.5 to run correctly (when fetching browser timings)

version 1.0 BETA 6
----------------------
* Copy environment variables to the bash runner, fixes problem with Xvfb

version 1.0 BETA 5
----------------------
* If homedir lack of a path separator, add it.
* More doc about that your Jenkins user need right priveleges to run the sitespeed script.

version 1.0 BETA 4
----------------------
* Fixed hardcoded project name (note the project name can't contain spaces right now)

version 1.0 BETA 3
----------------------
* Code cleanup
* Help documentation cleanup

version 1.0 BETA 2 
----------------------
* Fix for Graphite key when reporting number of hosts and sending the host with the rest of the assets data

version 1.0 BETA 1 
------------------------
* First (beta) release!
