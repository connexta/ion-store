/*
 * Copyright (c) 2019 Connexta, LLC
 *
 * Released under the GNU Lesser General Public License version 3; see
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */
/* Default Package */
class SpotlessConfig {

    static Closure getMisc() {
        return {
            target "**/*.md", "**/.gitignore"

            trimTrailingWhitespace()
            indentWithSpaces(4)
            endWithNewline()

            //  Replace single quotes with double tickets
            replace "Consistent quotations", "${(char) 39}", "\""

        }
    }

    static Closure getJava(File license) {
        return {
            licenseHeaderFile license
            removeUnusedImports()
            googleJavaFormat()
            trimTrailingWhitespace()
        }
    }

    static Closure getGroovy(File license) {
        return {
            target "**/*.gradle", "**/*.groovy"

            licenseHeaderFile(license, "/\\* Build Script \\*/|/\\* Default Package \\*/")
            trimTrailingWhitespace()
            indentWithSpaces(4)
            endWithNewline()

            //  Replace single quotes with double tickets
            replace "Consistent quotations", "${(char) 39}", "\""
        }
    }
}
