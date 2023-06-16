# Terrascan Integration

This guide provides instructions to integrate Terrascan into a Jenkins Declarative CI/CD pipeline.
Steps

1. **Install Terrascan**: Install the Terrascan CLI on your Jenkins build agent by following the instructions in the official documentation.

2. **Check Terrascan installation**: After installation, verify Terrascan's successful installation by running terrascan version.

3. **Access IaC files**: Ensure that your Infrastructure-as-Code (IaC) files, which you want to scan, are accessible to the Jenkins build agent.

4. **Create a Jenkinsfile**: Create a Jenkinsfile in your project's root directory if you don't have one already. This file will define your declarative pipeline.

5. **Define stages**: Define the stages and steps for your pipeline in the Jenkinsfile. Incorporate the Terrascan security scan.

Here's a sample Jenkinsfile demonstrating the integration of Terrascan services in a Jenkins Declarative CI/CD pipeline:

###### Environment Variables
```
None
```

Copy the following code and paste in either your `Jenkinsfile` or if you utilize Jenkins shared libraries create a file named `jslInfrastructureAsCodeAnalysis.groovy` and paste the content below.

```groovy
#!/usr/bin/env groovy

def call() {
    jslInfrastructureAsCodeAnalysis()
}

def jslInfrastructureAsCodeAnalysis() {
    script {
        sh (
            script: "/opt/terrascan scan -o json > terrascan_out.json || true"
        )
        archiveArtifacts artifacts: 'terrascan_out.json', fingerprint: true
        SecuSphereSecureDispatch(
            reportType: 'terrascan',
            appName: env.appName,
            giturl: env.GIT_URL,
            gitBranch: env.GIT_BRANCH
        )
        sh (script: "rm -rf terrascan_out.json")
    }
}
```

Copy the following stage section and paste into your declarative `Jenkinsfile`
```groovy
stage('Infrastructure-as-Code Security Testing') {
    when {
         expression {
            env.BRANCH_NAME ==~ /^release\/.*\/.*/
         }
    }
    steps {
        jslInfrastructureAsCodeAnalysis()
    }
}
```


This pipeline includes a stage for performing a Terrascan security scan on a directory of IaC files. Replace 'yourIaCDir' with the actual directory path of your IaC files. The scan results are logged to the console. You can also modify the script to handle the results as needed.

Make sure that your IaC files are either available locally on the agent or accessible from a remote location that the agent can reach. Terrascan supports scanning for a variety of IaC tools, including Terraform, Kubernetes, Helm, and Kustomize.