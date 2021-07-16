pipeline {
    agent any 

     environment {
        AWS_ACCESS_KEY_ID     = credentials('jenkins-aws-secret-key-id')
        AWS_SECRET_ACCESS_KEY = credentials('jenkins-aws-secret-access-key')
        ARTIFACT_NAME = 'kamtar-transport-api-0.0.3-SNAPSHOT.jar'
        AWS_S3_BUCKET = 'cicd-gitlab'
        AWS_EB_APP_NAME = 'kamtar-transport-api-0.0.3-SNAPSHOT'
        AWS_EB_ENVIRONMENT = 'kamtar-transport-api-0.0.3-SNAPSHOT-env'
        AWS_EB_APP_VERSION = "${BUILD_ID}"
    }      

    stages {
        stage('Publish') {
            steps {
                bat 'mvn clean install'
            }
            post {
                success {
                    archiveArtifacts 'target/*.jar'
                    bat 'aws configure set region eu-central-1'
                    bat 'aws s3 cp ./target/kamtar-transport-api-0.0.3-SNAPSHOT.jar s3://cicd-gitlab/backend/kamtar-transport-api-0.0.3-SNAPSHOT.jar'
                }
            }
        }
    }
}
