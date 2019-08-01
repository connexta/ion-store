/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
/* Default Package */
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.testing.TestResult

class TestLogger {

    static def styler = [(TestResult.ResultType.FAILURE): { msg -> "\033[31m${msg}\033[0m" },
                                     (TestResult.ResultType.SKIPPED): { msg -> "\033[33m${msg}\033[0m" },
                                     (TestResult.ResultType.SUCCESS): { msg -> "\033[32m${msg}\033[0m" }]

    static Closure configs(configArgs) {
        return {

            if(logger.isEnabled(LogLevel.INFO)) {
                return
            }

            def parallelTests = configArgs["parallel"]

            def parallel = { desc ->
                if (parallelTests) {
                    return " > ${desc}"
                }
                return "\t >"
            }

            //  Disable all events as we handle them ourselves
            testLogging {
                events = []
            }

            if (!parallelTests) {
                //  We only need to print the class name at the top

                def lastTestClass = ""

                onOutput {
                    desc ->
                        if (lastTestClass != desc.className && desc.className != null) {
                            lastTestClass = desc.className
                            def toPrint = " Starting tests for ${desc.className}: "
                            logger.lifecycle("-=" * (toPrint.length() / 2) + "-")
                            logger.lifecycle(toPrint)
                        }
                }
            }

            afterTest { desc, result ->
                logger.lifecycle("${parallel(desc.className)} ${desc.name}: ${styler[result.resultType](result.resultType)}")

                def exception = result.exception
                def extra = 0
                def intro = ""

                if(result.exception) {
                    logger.lifecycle("")
                }
                while(extra <= 0 && exception) {
                    def stack = []

                    //  If they aren"t parallel then we"ll print things out better
                    def toTake = 1
                    for (def ele : exception.getStackTrace()) {
                        if (ele.getClassName() == desc.className) {
                            break
                        }
                        toTake++
                    }

                    extra = exception.getStackTrace().length - toTake
                    stack = exception.getStackTrace().take(toTake)
                    if(extra > 0) {
                        stack += "... ${extra} more"
                    }

                    logger.lifecycle(" ${styler[result.resultType]("${intro}${exception}")}\n\t" +
                            styler[result.resultType](" ${stack.join("\n\t ")}"))

                    intro = " Caused by: "
                    exception = exception.getCause()
                }
                if(result.exception) {
                    logger.lifecycle("")
                }
            }

            afterSuite { desc, result ->
                if (!desc.parent) { // will match the outermost suite
                    def output = "Results: ${styler[result.resultType](result.resultType)} (${result.testCount} tests, " +
                            "${result.successfulTestCount} successes, ${result.failedTestCount} failures, " +
                            "${result.skippedTestCount} skipped)"
                    def startItem = "|  ", endItem = "  |"
                    def repeatLength = startItem.length() + output.length() + endItem.length()
                    logger.lifecycle("\n" + ("-" * repeatLength) + "\n" + startItem + output + endItem + "\n" + ("-" * repeatLength))
                }
            }
        }
    }
}
