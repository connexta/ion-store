library 'github-utils-shared-library@master'
@Library('github.com/connexta/cx-pipeline-library@master') _

pipeline {
    agent {
        node {
            label 'linux-small'
            customWorkspace "/jenkins/workspace/${JOB_NAME}/${BUILD_NUMBER}"
        }
    }
    tools {
        jdk 'jdk11'
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '15'))
        disableConcurrentBuilds()
        timestamps()
    }
    environment {
        GITHUB_USERNAME = 'connexta'
        GITHUB_REPONAME = 'ion-store'
    }
    stages {
        stage('Setup') {
            steps {
                retry(3) {
                    checkout scm
                }
                withCredentials([usernameColonPassword(credentialsId: 'cxbot', variable: 'GITHUB_TOKEN')]) {
                    script {
                        //  Clear existing status checks
                        def jsonBlob = getGithubStatusJsonBlob("pending", "${BUILD_URL}display/redirect", "ITests In Progress...", "ITests")

                        try {
                            //  Check to see if there are multiple parents for the commit. (merged)
                            sh(script: 'if [ `git cat-file -p HEAD | head -n 3 | grep parent | wc -l` -gt 1 ]; then exit 1; else exit 0; fi')
                            //  No error was thrown -> we called exit 0 -> HEAD is not a merge commit/doesn't have multiple parents
                            env.PR_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                        } catch (err) {
                            //  An error was thrown -> we called exit 1 -> HEAD is a merge commit/has multiple parents
                            env.PR_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD~1').trim()
                        }

                        postStatusToHash("${jsonBlob}", "${GITHUB_USERNAME}", "${GITHUB_REPONAME}", "${env.PR_COMMIT}", "${GITHUB_TOKEN}")
                    }
                }
            }
        }
        stage('Itests') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    dockerd {}
                    sh './gradlew test --tests *ITests'
                }
            }
        }
    }
    post {
        success {
            withCredentials([usernameColonPassword(credentialsId: 'cxbot', variable: 'GITHUB_TOKEN')]) {
                script {
                    def jsonBlob = getGithubStatusJsonBlob("success", "${BUILD_URL}display/redirect", "ITests succeeded!", "ITests")
                    postStatusToHash("${jsonBlob}", "${GITHUB_USERNAME}", "${GITHUB_REPONAME}", "${env.PR_COMMIT}", "${GITHUB_TOKEN}")
                }
            }
        }
        failure {
            withCredentials([usernameColonPassword(credentialsId: 'cxbot', variable: 'GITHUB_TOKEN')]) {
                script {
                    def jsonBlob = getGithubStatusJsonBlob("failure", "${BUILD_URL}display/redirect", "ITests Failed!", "ITests")
                    postStatusToHash("${jsonBlob}", "${GITHUB_USERNAME}", "${GITHUB_REPONAME}", "${env.PR_COMMIT}", "${GITHUB_TOKEN}")
                }
            }
        }
        unstable {
            withCredentials([usernameColonPassword(credentialsId: 'cxbot', variable: 'GITHUB_TOKEN')]) {
                script {
                    def jsonBlob = getGithubStatusJsonBlob("failure", "${BUILD_URL}display/redirect", "ITests were unstable!", "ITests")
                    postStatusToHash("${jsonBlob}", "${GITHUB_USERNAME}", "${GITHUB_REPONAME}", "${env.PR_COMMIT}", "${GITHUB_TOKEN}")
                }
            }
        }
    }
}
