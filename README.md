# photoman (Photo manager)

## Introduction

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
        
Sometimes you delete your jpg files. In this scenario `photoman` can remove corresponding RAW files (`clean` mode).        

### Setup

You should install `groovy` and `exiftool` in your system:

    sudo apt-get install groovy exiftool

For moving files in date-named folders and separates raw files, just run `photoman` in the root directory with your images:

    groovy photoman.groovy

### Using

* default mode (without arguments) `photoman` find all files in folder and checks, that the image file is placed into folder which contains
date of file creation. It's enough smart and don't transfer your xmas photos from directory like '2020-12-25 Family xmas'.

* `clean` mode. In this mode `photoman` find and delete RAW files, which not has corresponding 'jpg' file. It's useful when
you remove `jpg` file unsuccessful photo and want also automagically remove raw file with this photo. 
To be sure of result, use `--dry-run` flag to check, what `photoman` wants to remove.    

     
License
---------------
Script is under [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)