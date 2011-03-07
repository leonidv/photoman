/**
Copyright 2011 Leonid Vygovskiy (http://vygovskiy.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import groovy.io.FileType
import org.apache.ivy.util.FileUtil

/**
 * Return date from file exif. It's using metacam package.
 *
 * @param fileName of image
 * @return exif creation date
 */
def String getExifDate(String fileName) {
  def input = "metacam ${fileName}".execute().inputStream
  String date = null;
  input.eachLine {line ->
    def m = (line =~ /\s+Image Capture Date:\s.*(\d{4}:\d{2}:\d{2}).*/)
    if (m.matches()) {
      date = m[0][1]
      date = date.replace(':', "-")
    }
  }
  return date
}

/**
 * Return date that is extracted from file path.
 *
 * @param file
 * @return
 */
def String getPathDate(File file) {
  if (file.parent == null) {
    return "";
  }

  def parent = file.parentFile
  if (parent.name ==~ /\d{4}-\d{2}-\d{2}/) {
    return parent.name;
  } else {
    return getPathDate(parent)
  }
}

assert getPathDate(new File("/opt/eclipse/eclipse")) == ""
assert getPathDate(new File("photo/2011/2011-01-01/image.jpg")) == "2011-01-01"
assert getPathDate(new File("photo/2011-01-01/pef/old/image.jpg")) == "2011-01-01"

/**
 * Move images to folder with name builded on creation date information.
 *
 */
def void cleanByDate() {
  Set<File> possibleEmptyDirs = new HashSet<File>();
  new File(".").eachFileRecurse(FileType.FILES) { file ->

    if (file.name ==~ /(?i)IMGP\d+\.((JPG)|(PEF))$/) {
      def creationDate = getExifDate(file.absolutePath);
      def pathDate = getPathDate(file.absoluteFile)

      if (creationDate == pathDate) {
        return
      }

      def File dateFolder = new File(creationDate);
      if (!dateFolder.isDirectory()) {
        dateFolder.mkdirs();
      }

      def File dest = new File(dateFolder, file.name);
      println("move: ${file} → ${dest}")
      FileUtil.copy(file, dest, null)
      file.delete();

      possibleEmptyDirs << file.parentFile;
    }

  }
  println(possibleEmptyDirs)
  possibleEmptyDirs.each { File dir ->
    if(dir.listFiles().length == 0) {
      dir.deleteDir();
    }
  }
}

def void separateRaws() {
  new File(".").eachFileRecurse(FileType.FILES) { file ->
    boolean isPEF = (file ==~ /(?i).*(PEF)$/);
    boolean isInRightPlace = (file.parentFile.name == "raw")

    if (isPEF && !isInRightPlace) {
      File rawFolder = new File(file.parent,"raw")
      if (!rawFolder.isDirectory()) {
        rawFolder.mkdirs();
      }
      File dest = new File(rawFolder,file.name);
      println "move: ${file} → ${dest}"
      FileUtil.copy(file,dest,null);
      file.delete()
    }
  }
}



/** ********************* Entry point. Script execution start here     ******************************/
cleanByDate()
separateRaws()