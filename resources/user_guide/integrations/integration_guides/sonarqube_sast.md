# SonarQube Integration

To integrate SonarQube SAST into a Jenkins declarative CI/CD pipeline and generate an output JSON file with the most recent scan results, follow these steps:

1. **Install SonarQube Scanner for Jenkins**: In your Jenkins instance, go to "Manage Jenkins" > "Manage Plugins" > "Available" tab, then search for and install the "SonarQube Scanner" plugin. Restart Jenkins if needed.

2. **Configure SonarQube Scanner**: In Jenkins, go to "Manage Jenkins" > "Global Tool Configuration" > "SonarQube Scanner", and add a new SonarQube Scanner installation. Provide a name and the version of the scanner you want to use.

3. **Configure SonarQube server**: In Jenkins, go to "Manage Jenkins" > "Configure System" > "SonarQube servers", and add your SonarQube server information, including the server URL and authentication token.

4. **Create a Jenkinsfile**: If you don't have one already, create a Jenkinsfile in your project's root directory. This file will define your declarative pipeline.

5. **Define stages**: Define the stages and steps for your pipeline in the Jenkinsfile. Incorporate the SonarQube scan stage.

Add a post-build step to download JSON report: After the scan is completed, use the SonarQube API to download the JSON report of the most recent scan.

Here's a sample Jenkinsfile that demonstrates how to integrate SonarQube SAST and generate an output JSON file with the most recent scan results:


###### Environment Variables
```
environment {
    ...
    SONARQUBE_SERVER_URL = 'https://your-sonarqube-server-url'
    SONARQUBE_AUTH_TOKEN = credentials('sonarqube-auth-token')
}
```


Copy the following code and paste in either your `Jenkinsfile` or if you utilize Jenkins shared libraries create a file named `jslStaticApplicationSecurityTesting.groovy` and paste the content below.

```groovy
#!/usr/bin/env groovy

def call() {
    jslStaticApplicationSecurityTesting()
}

def jslStaticApplicationSecurityTesting() {
    withSonarQubeEnv('SonarScanner') {
        script {
            def scannerHome = tool "${env.SONARQUBE_SCANNER_HOME}"
            sh "${scannerHome}/bin/sonar-scanner"
        }
    }
    script {
        def projectKey = env.appName
        def outputFileName = 'sonarqube_sast.json'

        // Wait for a while to ensure SonarQube analysis is completed
        sleep(time: 30, unit: 'SECONDS')

        // Retrieve the most recent analysis ID
        def analysisId = sh(
            script: 'curl -s -u ' + env.SONARQUBE_AUTH_TOKEN + ': "' + env.SONARQUBE_SERVER_URL + '/api/project_analyses/search?project=' + projectKey + '&ps=1" | jq ".analyses[0].key" -r',
            returnStdout: true
        ).trim()

        // Retrieve the most recent analysis date
        def analysisDate = sh(
            script: 'curl -s -u ' + env.SONARQUBE_AUTH_TOKEN + ': "' + env.SONARQUBE_SERVER_URL + '/api/project_analyses/search?project=' + projectKey + '&ps=1" | jq ".analyses[0].date" -r',
            returnStdout: true
        ).trim()

        // Download the JSON report
        sh 'curl -s -u ' + env.SONARQUBE_AUTH_TOKEN + ': "' + env.SONARQUBE_SERVER_URL + '/api/issues/search?projects=' + projectKey + '&types=VULNERABILITY&ps=500&resolved=false&additionalFields=_all" -o ' + outputFileName
        archiveArtifacts artifacts: outputFileName, fingerprint: true
        SecuSphereSecureDispatch(
            reportType: 'sonarqube_sast',
            appName: env.appName,
            giturl: env.GIT_URL,
            gitBranch: env.GIT_BRANCH
        )
        sh (script: "rm -rf sonarqube_sast.json")
    }
}
```

Copy the following stage section and paste into your declarative `Jenkinsfile`
```groovy
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
```


In this example, make sure to replace your-sonarqube-server-url, Your SonarQube Server, and your-sonarqube-project-key with the appropriate values for your SonarQube instance and project. 

After running the SonarQube analysis, the script waits for 30 seconds to ensure the analysis is completed before proceeding to download the JSON report. You can adjust the waiting time as needed for your specific use case.
