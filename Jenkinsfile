pipeline {
    agent {
        label 'docker'
    }
    tools {
        maven '3.9.12'
    }
    stages {
        stage('checkout') {
            when { changeRequest target: 'dev' }
            steps {
                checkout scm
            }
        }
        stage ('build') {
            when { changeRequest target: 'dev' }
            steps {
                    sh 'mvn -Dmaven.test.skip clean verify '
            }
        }
        stage ('tests') {
            when { changeRequest target: 'dev' }
            steps {
                sh 'mvn test'
            }
        }
    }
}