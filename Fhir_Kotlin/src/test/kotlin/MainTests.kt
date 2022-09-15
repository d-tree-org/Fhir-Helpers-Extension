import org.junit.Test

class MainTests  {
    @Test
    fun verifyWorkingQuestionnaire() {
        val path = "src/test/resources/sample_questionnaire.json"
        verifyQuestionnaire(path)
    }
}
