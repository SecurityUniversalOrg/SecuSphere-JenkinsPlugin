@Library('security-pipeline-library')_

pipeline {

    options {
        // Build auto timeout
        timeout(time: 600, unit: 'MINUTES')
    }

    // Some global default variables
    environment {
        // Core Settings
        EMAIL_FROM = "${globalVars.EMAIL_FROM}"
        SUPPORT_EMAIL = "${globalVars.SUPPORT_EMAIL}"
        RELEASE_NUMBER = "${globalVars.RELEASE_NUMBER}"
        IMG_PULL_SECRET = "${globalVars.IMG_PULL_SECRET}"
        GIT_CREDS_ID = "${globalVars.GIT_CREDS_ID}"
        VULNMANAGER_URL = "${globalVars.VULNMANAGER_URL}"
        SONARQUBE_SERVER_URL = "${globalVars.SONARQUBE_SERVER_URL}"
        SONARQUBE_SCANNER_HOME = "${globalVars.SONARQUBE_SCANNER_HOME}"
        SONARQUBE_AUTH_TOKEN = credentials('SonarQube Global Analysis')
        SNYK_API_KEY = credentials('snyk-api-key')
        // App-specific settings
        appName = "SECUSPHERE_JP"
        SOURCE_DIR = "src"
        GIT_URL = "https://github.com/SecurityUniversalOrg/jenkins-threatbuster-plugin.git"
    }

    // In this example, all is built and run from the master
    agent any

    // Pipeline stages
    stages {
        stage('Unit Testing') {
            when {
                 expression {
                    env.BRANCH_NAME ==~ /^release\/.*\/.*/
                 }
            }
            steps {
                jslMavenUnitTesting()
            }
        }
        stage('Secret Scanning') {
            when {
                 expression {
                    env.BRANCH_NAME ==~ /^release\/.*\/.*/
                 }
            }
            steps {
                jslSecretScanning()
            }
        }
        stage('Software Composition Analysis') {
            when {
                 expression {
                    env.BRANCH_NAME ==~ /^release\/.*\/.*/
                 }
            }
            steps {
                jslJavaSoftwareCompositionAnalysis()
            }
        }
        stage('Static Application Security Testing') {
            when {
                 expression {
                    env.BRANCH_NAME ==~ /^release\/.*\/.*/
                 }
            }
            steps {
                jslStaticApplicationSecurityTesting()
            }
        }
        stage('Java Maven Build') {
            when {
                 expression {
                    env.BRANCH_NAME ==~ /^release\/.*\/.*/
                 }
            }
            steps {
                jslMavenVerify()
            }
        }
        stage('Merge Current Branch to Prod Branch') {
            when {
                expression {
                    env.BRANCH_NAME ==~ /^release\/(?!.*\/Prod).*\/.*/
                }
            }
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/' + BRANCH_NAME]],
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [],
                    submoduleCfg: [],
                    userRemoteConfigs: [
                        [credentialsId: env.GIT_CREDS_ID, url: env.GIT_URL]
                    ]
                ])
                jslGitPushToProdBranch(env.GIT_CREDS_ID, env.GIT_URL)
            }
        }
    }
}