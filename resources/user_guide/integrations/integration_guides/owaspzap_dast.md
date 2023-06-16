# OWASP ZAP Integration

This guide will help you integrate OWASP ZAP into a Jenkins Declarative CI/CD pipeline for Dynamic Application Security Testing (DAST).
Steps

1. **Install OWASP ZAP**: Install the OWASP ZAP on your Jenkins build agent by following the instructions in the official documentation.

2. **Install ZAP Jenkins Plugin**: Install the Official OWASP ZAP Jenkins Plugin from the Jenkins plugin marketplace.

3. **Configure ZAP Jenkins Plugin**: In Jenkins, navigate to Manage Jenkins > Configure System > ZAP and add the ZAP installation by specifying the path to the installed ZAP.

4. **Create a Jenkinsfile**: Create a Jenkinsfile in your project's root directory if you don't have one already. This file will define your declarative pipeline.

5. **Define stages**: Define the stages and steps for your pipeline in the Jenkinsfile. Incorporate the OWASP ZAP DAST scan.

Here's a sample Jenkinsfile demonstrating the integration of OWASP ZAP DAST services in a Jenkins Declarative CI/CD pipeline:

###### Environment Variables
```
None
```

Copy the following code and paste in either your `Jenkinsfile` or if you utilize Jenkins shared libraries create a file named `jslDynamicApplicationSecurityTesting.groovy` and paste the content below.

```groovy
#!/usr/bin/env groovy

def call(String scan_target = 'http:127.0.0.1:80') {
    jslDynamicApplicationSecurityTesting(scan_target)
}

def jslDynamicApplicationSecurityTesting(scan_target = 'http:127.0.0.1:80') {
    script {
        sh """
            docker run -dt --name owasp \
            owasp/zap2docker-stable \
            /bin/bash
        """

        sh """
            docker exec owasp \
            mkdir /zap/wrk
        """
        sh (
            script: "docker exec owasp zap-baseline.py -t $scan_target -J r_owasp_baseline.json -I"
        )
        sh """
             docker cp owasp:/zap/wrk/r_owasp_baseline.json ${WORKSPACE}/r_owasp_baseline.json
         """

        sh '''
            jq '{
                "alerts": [
                    .site // [] | .[]? | (.alerts // [])[] as $alert | ($alert.instances // [])[] | {
                        pluginid: $alert.pluginid,
                        alertRef: $alert.alertRef,
                        alert: $alert.alert,
                        name: $alert.name,
                        riskcode: $alert.riskcode,
                        confidence: $alert.confidence,
                        riskdesc: ($alert.riskdesc | split(" (")[0]),
                        desc: $alert.desc,
                        uri: .uri,
                        method: .method,
                        param: .param,
                        attack: .attack,
                        evidence: .evidence,
                        otherinfo: .otherinfo,
                        count: $alert.count,
                        solution: $alert.solution,
                        reference: $alert.reference,
                        cweid: $alert.cweid,
                        wascid: $alert.wascid,
                        sourceid: $alert.sourceid
                    }
                ]
            }' r_owasp_baseline.json > owasp_baseline.json
        '''

        archiveArtifacts artifacts: 'owasp_baseline.json', fingerprint: true

        SecuSphereSecureDispatch(
            reportType: 'owasp_zap',
            appName: env.appName,
            giturl: env.GIT_URL,
            gitBranch: env.GIT_BRANCH
        )
        sh (script: "rm -rf owasp_baseline.json r_owasp_baseline.json")
    }
}
```

Copy the following code and paste in either your `Jenkinsfile` or if you utilize Jenkins shared libraries create a file named `jslDastTestingTeardown.groovy` and paste the content below.

```groovy
#!/usr/bin/env groovy

def call() {
    jslDastTestingTeardown()
}

def jslDastTestingTeardown() {
     script {
         echo "Removing container"
         sh '''
             docker stop owasp
             docker rm owasp
         '''
     }
}
```

Copy the following stage section and paste into your declarative `Jenkinsfile`
```groovy
stage('Test Release') {
    when {
         expression {
            env.BRANCH_NAME ==~ /^release\/.*\/.*/
         }
    }
    steps {
        jslDynamicApplicationSecurityTesting("http://192.168.0.150:5080")
    }
    post {
         always {
             jslDastTestingTeardown()
         }
    }
}
```

In this pipeline, there's a stage for performing an OWASP ZAP DAST scan on a web application. The scan results can be viewed in the Jenkins build console.

Ensure that the web application you want to scan is accessible from the Jenkins build agent. You might need to adjust firewall rules or security groups to allow the scan.
