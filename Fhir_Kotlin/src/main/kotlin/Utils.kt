import org.apache.commons.io.FileUtils

fun String.readFile(): String {
    return FileUtils.readFileToString(FileUtils.getFile(this), "UTF-8")
}