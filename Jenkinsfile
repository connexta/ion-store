library 'github-utils-shared-library@master'
@Library('github.com/connexta/cx-pipeline-library@master') _

pipeline {
    agent {
        node {
            label 'linux-large'
            customWorkspace "/jenkins/workspace/${JOB_NAME}/${BUILD_NUMBER}"
        }
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '15'))
        disableConcurrentBuilds()
        timestamps()
    }
    environment {
        GITHUB_USERNAME = 'connexta'
        GITHUB_REPONAME = 'multi-int-store'
    }
    stages {
        stage('Setup') {
            steps {
                withCredentials([usernameColonPassword(credentialsId: 'cxbot', variable: 'GITHUB_TOKEN')]) {
                    postCommentIfPR("Internal build has been started. Your results will be available at completion. See build progress in [legacy Jenkins UI](${BUILD_URL}) or in [Blue Ocean UI](${BUILD_URL}display/redirect).", "${GITHUB_USERNAME}", "${GITHUB_REPONAME}", "${GITHUB_TOKEN}")
                    script {
                        //Clear existing status checks
                        def jsonBlob = getGithubStatusJsonBlob("pending", "${BUILD_URL}display/redirect", "Full Build In Progress...", "CX Jenkins/Full Build")
                        postStatusToHash("${jsonBlob}", "${GITHUB_USERNAME}", "${GITHUB_REPONAME}", "${env.PR_COMMIT}", "${GITHUB_TOKEN}")
                    }
                }
            }
        }
        stage('Full Build') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    sh './gradlew build'
                }
            }
        }
    }
    post {
        success {
            withCredentials([usernameColonPassword(credentialsId: 'cxbot', variable: 'GITHUB_TOKEN')]) {
                postCommentIfPR("✅ Build success! See the job results in [legacy Jenkins UI](${BUILD_URL}) or in [Blue Ocean UI](${BUILD_URL}display/redirect).", "${GITHUB_USERNAME}", "${GITHUB_REPONAME}", "${GITHUB_TOKEN}")
                script {
                    def jsonBlob = getGithubStatusJsonBlob("success", "${BUILD_URL}display/redirect", "Full build succeeded!", "CX Jenkins/Full Build")
                    postStatusToHash("${jsonBlob}", "${GITHUB_USERNAME}", "${GITHUB_REPONAME}", "${env.PR_COMMIT}", "${GITHUB_TOKEN}")
                }
            }
        }
        failure {
            withCredentials([usernameColonPassword(credentialsId: 'cxbot', variable: 'GITHUB_TOKEN')]) {
                postCommentIfPR("❌ Build failure. See the job results in [legacy Jenkins UI](${BUILD_URL}) or in [Blue Ocean UI](${BUILD_URL}display/redirect).", "${GITHUB_USERNAME}", "${GITHUB_REPONAME}", "${GITHUB_TOKEN}")
                script {
                    def jsonBlob = getGithubStatusJsonBlob("failure", "${BUILD_URL}display/redirect", "Full Build Failed!", "CX Jenkins/Full Build")
                    postStatusToHash("${jsonBlob}", "${GITHUB_USERNAME}", "${GITHUB_REPONAME}", "${env.PR_COMMIT}", "${GITHUB_TOKEN}")
                }
            }
        }
        unstable {
            withCredentials([usernameColonPassword(credentialsId: 'cxbot', variable: 'GITHUB_TOKEN')]) {
                postCommentIfPR("⚠️ Build unstable. See the job results in [legacy Jenkins UI](${BUILD_URL}) or in [Blue Ocean UI](${BUILD_URL}display/redirect).", "${GITHUB_USERNAME}", "${GITHUB_REPONAME}", "${GITHUB_TOKEN}")
                script {
                    def jsonBlob = getGithubStatusJsonBlob("failure", "${BUILD_URL}display/redirect", "Full build was unstable!", "CX Jenkins/Full Build")
                    postStatusToHash("${jsonBlob}", "${GITHUB_USERNAME}", "${GITHUB_REPONAME}", "${env.PR_COMMIT}", "${GITHUB_TOKEN}")
                }
            }
        }
    }
}