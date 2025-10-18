pipeline {
  agent any
  environment {
    SONAR_HOST_URL = "http://sonarqube:9000"
    SONAR_TOKEN = credentials('sonar-token')
  }
  stages {
    stage('Checkout') { steps { checkout scm } }
    stage('Build') { steps { sh 'mvn -B -DskipTests clean package' } }
    stage('Sonar') {
      steps {
        sh "mvn sonar:sonar -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_TOKEN} -Dsonar.projectKey=${env.JOB_NAME}"
      }
    }
    stage('Quality Gate') {
      steps {
        timeout(time: 5, unit: 'MINUTES') {
          script { def qg = waitForQualityGate(); if (qg.status != 'OK') { error "Quality Gate failed: ${qg.status}" } }
        }
      }
    }
  }
}
