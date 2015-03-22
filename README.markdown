photoman (Photo manager)
============
Introduction
-------------

Simple script for organization images from camera by folder that names is images capture date. For example, you make
images 10 december 2011. Then folder for this images will be:

    2011-12-10.

Additionally script separates raw files (PEF) to subfolder raw. Full directory looks like this:

    |-- 2011-01-01
    |-- 2011-01-03
    |-- 2011-01-04
    |-- 2011-01-09
    |-- 2011-01-10

and each directory:

    2011-01-04
        |-- raw
        |   |-- IMGP5747.PEF
        |   |-- IMGP5748.PEF
        |-- IMGP5747.JPG
        |-- IMGP5748.JPG

Using
-------------
You have to install groovy and metacam in your system:

    sudo apt-get install groovy exiftool

For ordering files just run photoman in directory with your images:

    groovy photoman.groovy

License
---------------
Script is under [Apache License, Version 2.0]((http://www.apache.org/licenses/LICENSE-2.0.html)