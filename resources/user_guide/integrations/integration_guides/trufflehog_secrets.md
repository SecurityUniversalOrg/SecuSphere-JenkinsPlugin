# Trufflehog Integration

To add Trufflehog Secret Scanning services to a Jenkins Declarative CI/CD pipeline, follow these steps:

1. **Install Trufflehog**: Trufflehog is a Python-based tool, so it can be installed with pip. Install Trufflehog on your Jenkins build agent by running `pip install truffleHog` or follow the instructions in the [official documentation](https://github.com/dxa4481/truffleHog).

2. **Configure Trufflehog**: There's no need for API keys with Trufflehog, but you may need to configure certain aspects of it, like the entropy checks, depending on the requirements of your project.

3. **Create a Jenkinsfile**: Create a Jenkinsfile in your project's root directory if you don't have one already. This file will define your declarative pipeline.

4. **Define stages**: Define the stages and steps for your pipeline in the Jenkinsfile. Incorporate the Trufflehog Secret Scanning.

Here's a sample Jenkinsfile demonstrating the integration of Trufflehog Secret Scanning services in a Jenkins Declarative CI/CD pipeline:

###### Environment Variables
```
None
```

Copy the following code and paste in either your `Jenkinsfile` or if you utilize Jenkins shared libraries create a file named `jslSecretScanning.groovy` and paste the content below.

```groovy
#!/usr/bin/env groovy

def call() {
    jslSecretScanning()
}

def jslSecretScanning() {
    script {
        // Execute the trufflehog command
        sh (
            script: "trufflehog filesystem -j --no-update ${SOURCE_DIR} ./ci_cd > secret_scan_raw.json"
        )

        // Format the report to a JSON list
        sh (
            script: '''
                #!/bin/sh

                input_file="secret_scan_raw.json"
                output_file="secret_scan.json"
                log_file="script.log"

                # Initialize the output JSON file with the opening master dictionary and findings key
                echo '{"findings": [' > $output_file

                # Process the input file line by line
                first_line=true
                while IFS= read -r line; do
                  # Check if the line starts with the specified nested format
                  if echo "$line" | grep -q '^{"SourceMetadata":{"Data":{"Filesystem":{"file"'; then
                    # If this is not the first line, add a comma to separate the JSON objects
                    if [ "$first_line" = false ]; then
                      echo ',' >> $output_file
                    else
                      first_line=false
                    fi
                    echo "$line" >> $output_file
                  fi
                done < "$input_file" > $log_file 2>&1

                # Close the JSON array and master dictionary
                echo ']}' >> $output_file
                if [ $? -ne 0 ]; then
                  tail -n 1 $log_file
                fi
            ''',
            returnStdout: false
        )
        archiveArtifacts artifacts: 'secret_scan.json', fingerprint: true
        SecuSphereSecureDispatch(
            reportType: 'trufflehog',
            appName: env.appName,
            giturl: env.GIT_URL,
            gitBranch: env.GIT_BRANCH
        )
        // Remove both 'secret_scan_raw.json' and 'secret_scan.json'
        sh (script: "rm -rf secret_scan_raw.json secret_scan.json")
    }
}
```

Copy the following stage section and paste into your declarative `Jenkinsfile`
```groovy
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
```

In this example, the pipeline has a separate stage for Trufflehog Secret Scanning. The pipeline runs Trufflehog on the checked-out codebase. If Trufflehog finds anything, it will output the secrets to the console and return a non-zero exit code, causing the pipeline to fail.

Please ensure to review the output of the scanning stage and handle any identified issues appropriately to prevent potential security risks.
