# Snyk Integration

To add Snyk Software Composition Analysis (SCA) services to a Jenkins Declarative CI/CD pipeline, follow these steps:

1. **Install Snyk CLI**: Install the Snyk CLI on your Jenkins build agent by following the instructions in the [official documentation](https://support.snyk.io/hc/en-us/articles/360003812458-Install-the-Snyk-CLI).

2. **Acquire API key**: Sign up for a Snyk account and obtain an API key for authentication from the [Snyk dashboard](https://app.snyk.io/).

3. **Store API key securely**: Store the API key securely as a Jenkins credential. This will allow you to use the key in your pipeline without exposing it in your code.

4. **Create a Jenkinsfile**: Create a Jenkinsfile in your project's root directory if you don't have one already. This file will define your declarative pipeline.

5. **Define stages**: Define the stages and steps for your pipeline in the Jenkinsfile. Incorporate the Snyk SCA.

Here's a sample Jenkinsfile demonstrating the integration of Snyk SCA services in a Jenkins Declarative CI/CD pipeline:


###### Environment Variables
```
environment {
    ...
    SNYK_API_KEY = credentials('snyk-api-key')
}
```

Copy the following code and paste in either your `Jenkinsfile` or if you utilize Jenkins shared libraries create a file named `jslJavaSoftwareCompositionAnalysis.groovy` and paste the content below.

```groovy
#!/usr/bin/env groovy

def call() {
    jslJavaSoftwareCompositionAnalysis()
}

def jslJavaSoftwareCompositionAnalysis() {
    script {
        // Authenticate with Snyk
        sh "snyk auth ${SNYK_API_KEY}"
        sh (
            script: "snyk test --json-file-output=snyk_sca.json --all-projects --continue-on-error || true"
        )
        archiveArtifacts artifacts: 'snyk_sca.json', fingerprint: true
        sh (
            script: "mvn package"
        )
        archiveArtifacts artifacts: 'target/bom.json', fingerprint: true
        SecuSphereSecureDispatch(
            reportType: 'snyk_sca',
            appName: env.appName,
            giturl: env.GIT_URL,
            gitBranch: env.GIT_BRANCH
        )
        sh (script: "rm -rf snyk_sca.json")
    }
}
```

Copy the following stage section and paste into your declarative `Jenkinsfile`
```groovy
stage('Software Composition Analysis') {
    when {
         expression {
            env.BRANCH_NAME ==~ /^release\/.*\/.*/
         }
    }
    steps {
        jslPythonSoftwareCompositionAnalysis()
    }
}
```

In this example, the pipeline has a separate stage for Snyk SCA. The pipeline authenticates with Snyk using the stored API key, tests the project for vulnerabilities.