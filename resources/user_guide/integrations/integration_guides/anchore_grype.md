# Anchore Grype Integration

To add Anchore Grype vulnerability scanning services to a Jenkins Declarative CI/CD pipeline, follow these steps:

1. **Install Grype**: Install Grype on your Jenkins build agent by following the instructions in the official documentation.

2. **Check Grype installation**: After installation, check the Grype version to ensure it has been correctly installed with grype version.

3. **Store Docker Image**: Ensure that the Docker image that you want to scan is accessible to the Jenkins build agent.

4. **Create a Jenkinsfile**: Create a Jenkinsfile in your project's root directory if you don't have one already. This file will define your declarative pipeline.

5. **Define stages**: Define the stages and steps for your pipeline in the Jenkinsfile. Incorporate the Grype vulnerability scan.

Here's a sample Jenkinsfile demonstrating the integration of Anchore Grype services in a Jenkins Declarative CI/CD pipeline:

###### Environment Variables
```
environment {
    ...
    DOCKER_REG = "Your Docker Registry"
    DOCKER_TAG = "The Docker Tag for the Build"
    K8_NAMESPACE = "The name of the Application"
}
```

Copy the following code and paste in either your `Jenkinsfile` or if you utilize Jenkins shared libraries create a file named `jslContainerSecurityScanning.groovy` and paste the content below.

```groovy
#!/usr/bin/env groovy

def call(String servicename = 'su-webapp') {
    jslContainerSecurityScanning(servicename)
}

def jslContainerSecurityScanning(servicename) {
    script {
        sh (
            script: "grype ${DOCKER_REG}/${servicename}:${DOCKER_TAG} -o json > anchore_report.json"
        )
        archiveArtifacts artifacts: 'anchore_report.json', fingerprint: true
        SecuSphereSecureDispatch(
            reportType: 'anchore',
            appName: env.appName,
            giturl: env.GIT_URL,
            gitBranch: env.GIT_BRANCH
        )
        sh (script: "rm -rf anchore_report.json")
    }
}
```

Copy the following stage section and paste into your declarative `Jenkinsfile`
```groovy
stage('Docker Container Scanning') {
    when {
         expression {
            env.BRANCH_NAME ==~ /^release\/.*\/.*/
         }
    }
    steps {
        jslContainerSecurityScanning(env.K8_NAMESPACE)
    }
}
```

This pipeline includes a stage for performing a Grype vulnerability scan on a Docker image.

Keep in mind that the Docker image must be accessible to the Jenkins build agent (i.e., it should either be available locally on the agent or accessible from a remote Docker registry that the agent can pull from). The scan results are logged to the console. You can also modify the script to handle the results as needed.