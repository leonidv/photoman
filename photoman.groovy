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

class CreationDate {
    String date = "";
    String time = "";

    String toString() { "date = ${date}, time = ${time}" }

    boolean isNotExists() {
        return date.isBlank()
    }
}

/**
 * Return date from file exif. It's using metacam package.
 *
 * @param fileName of image
 * @return exif creation date
 */
CreationDate getExifDate(String fileName) {
  def result = new CreationDate()

  def input = "exiftool -CreateDate ${fileName}".execute().inputStream

  input.eachLine {line ->
    def m = (line =~ /^Create Date\s+: (\d{4}:\d{2}:\d{2}) (\d{2}:\d{2}:\d{2})$/)
    if (m.matches()) {
      def date = m[0][1]
      def time = m[0][2]
      result.date = date.replace(':', "-")
      result.time = time.replace(":", "_")
    }
  }
  return result
}


/**
 * Return date that is extracted from file path.
 *
 * @param file
 * @return
 */
String getPathDate(File file) {
  if (file == null) {
    return "";
  }

  def matcher = (file.name =~ /^(\d{4}-\d{2}-\d{2}).*/ )
  if (matcher.matches()) {
    return matcher[0][1]; // first match, first group
  } else {
    return getPathDate(file.parentFile)
  }
}

assert getPathDate(new File("/opt/eclipse/eclipse")) == ""
assert getPathDate(new File("photo/2011/2011-01-01/image.jpg")) == "2011-01-01"
assert getPathDate(new File("photo/2011-01-01/pef/old/image.jpg")) == "2011-01-01"
assert getPathDate(new File("photo/2011/2011-02-03 Rome Holidays/image.jpg")) == "2011-02-03"
assert getPathDate(new File("photo/2011-02-03 vacation in Russia/pef/old/image.jpg")) == "2011-02-03"
assert getPathDate(new File("./2015-01-31").absoluteFile) == "2015-01-31"


/**
 * Move images to folder with name built on creation date information.
 *
 */
void seperateByDate(boolean skipWithDate) {
  Set<File> possibleEmptyDirs = new HashSet<File>();

  new File(".").eachFile(FileType.DIRECTORIES) { dir ->
      def pathDate = getPathDate(dir.absoluteFile)
      if (!pathDate.isEmpty() && skipWithDate) {
          println ("already with date ${dir.absolutePath}")
          return
      }
      dir.eachFileRecurse(FileType.FILES) { file ->
          def matcher = (file.name =~ /(?i).*?\d+\.(JPG|PEF|MOV|ARW)$/)
          println(file.getAbsoluteFile())
          if (matcher.matches()) {
              String fileExtension = matcher[0][1]
              try {
                  def creationDate = getExifDate(file.absolutePath)
                  println("${file.absolutePath} creationDate: ${creationDate}")
                  if (creationDate.date == pathDate) {
                      return
                  }


                  File dateFolder = new File(creationDate.date)
                  println "${file.absolutePath} creationDatedate: ${creationDate} folder: ${dateFolder} "
                  if (!dateFolder.isDirectory()) {
                      dateFolder.mkdirs();
                  }

                  File dest;
                  if (fileExtension.toLowerCase() == "mov") {
                      dest = new File(dateFolder, creationDate.date + " " + creationDate.time + "." + fileExtension)
                  } else {
                      dest = new File(dateFolder, file.name)
                  }

                  println("move: ${file} → ${dest}")
                  file.renameTo(dest)
                  possibleEmptyDirs << file.parentFile
              } catch (Exception ex) {
                  ex.printStackTrace(System.err)
                  println(ex.message)
              };
          }

      }
  }

  possibleEmptyDirs.each { File dir ->
    if(dir.listFiles().length == 0) {
      dir.deleteDir();
    }
  }
}

boolean isRaw(File file) { return (file ==~ /(?i).*(PEF|ARW)$/) }

String baseName(File file) { file.name.split("\\.")[-2] }

boolean rawInRightPlace(File file) {file.parentFile.name == "raw"}

void separateRaws() {
  new File(".").eachFileRecurse(FileType.FILES) { file ->
    boolean isPEF = isRaw(file);
    boolean isInRightPlace = rawInRightPlace(file)

    if (isPEF && !isInRightPlace) {
      File rawFolder = new File(file.parent,"raw")
      if (!rawFolder.isDirectory()) {
        rawFolder.mkdirs();
      }
      File dest = new File(rawFolder,file.name);
      println "move: ${file} → ${dest}"
      file.renameTo(dest)
    }
  }
}

/**
 * Remove RAW files that don't has couple of JPG.
 */
void cleanRaw(boolean dryRun) {
 new File(".").eachFileRecurse(FileType.FILES) { file ->
   if (isRaw(file) && rawInRightPlace(file)) {
     String baseName = baseName(file)
     File jpegFile = new File(baseName+".JPG",file.parentFile.parentFile)
     if (!jpegFile.isFile()) {
         println("Remove " + file.absoluteFile.canonicalPath)
         if (!dryRun) {
             file.delete()
         }
     }
   }
 }
}

/** ********************* Entry point. Script execution start here     ***************************** */

if (args.length == 0) {
    println("clean: deleterRAW without related JPEG. Use --dry-run for testing. \n")
    println("without arguments separates by date and raw")
}

if (args.contains("clean")) {
    def dryRun = args.contains("--dry-run")
    cleanRaw(dryRun)

} else {
  seperateByDate(!args.contains("--all"))
  separateRaws()
}
