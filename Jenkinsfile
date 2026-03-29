pipeline {
    agent {
        label 'docker'
    }
    tools {
        maven '3.9.12'
    }
    when {
        allOf {
            expression { return env.CHANGE_TARGET == 'dev' }
            expression { return env.CHANGE_ID != null }
        }
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        stage ('build') {
            steps {
                sh 'mvn -Dmaven.test.skip clean verify '
            }
        }
        stage ('tests') {
            steps {
                sh 'mvn test'
            }
        }
    }
}