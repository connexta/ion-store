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
    triggers {
        cron(env.BRANCH_NAME == "master" ? "H H(21-23) * * *" : "")
    }
    environment {
        GITHUB_USERNAME = 'connexta'
        GITHUB_REPONAME = 'multi-int-store'
        ORG_GRADLE_PROJECT_dependencyCheckUrlModified = 'http://10.101.190.7:10179/nvdcve-1.0-modified.json.gz'
        ORG_GRADLE_PROJECT_dependencyCheckUrlBase = 'http://10.101.190.7:10179/nvdcve-1.0-%d.json.gz'
    }
    stages {
        stage('Setup') {
            steps {
                retry(3) {
                    checkout scm
                }
                withCredentials([usernameColonPassword(credentialsId: 'cxbot', variable: 'GITHUB_TOKEN')]) {
                    postCommentIfPR("Internal build has been started. Your results will be available at completion. See build progress in [legacy Jenkins UI](${BUILD_URL}) or in [Blue Ocean UI](${BUILD_URL}display/redirect).", "${GITHUB_USERNAME}", "${GITHUB_REPONAME}", "${GITHUB_TOKEN}")
                    script {
                        //  Clear existing status checks
                        def jsonBlob = getGithubStatusJsonBlob("pending", "${BUILD_URL}display/redirect", "Full Build In Progress...", "CX Jenkins/Full Build")

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
        stage('Full Build') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    dockerd {}
                    sh './gradlew build'
                }
            }
        }
        stage('Security Analysis - OWASP') {
            steps {
                timeout(time: 25, unit: 'MINUTES') {
                    sh './gradlew dependencyCheckAnalyze --info'
                }
            }
        }
        stage('Quality Analysis - SonarCloud') {
            when {
                allOf {
                    expression { env.CHANGE_ID == null }
                    branch 'master'
                }
            }
            steps {
                timeout(time: 25, unit: 'MINUTES') {
                    withCredentials([string(credentialsId: 'SonarQubeGithubToken', variable: 'SONARQUBE_GITHUB_TOKEN'), string(credentialsId: 'cxbot-sonarcloud', variable: 'SONAR_TOKEN')]) {
                        sh './gradlew sonarqube -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN -Dsonar.organization=cx -Dsonar.projectKey=multi-int-store'
                    }
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
