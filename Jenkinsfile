pipeline {
    agent {
        label 'agent1'
    }
    tools {
        maven '3.9.12'
    }
    stages {
        stage ('build') {
            steps {
                sh 'mvn clean verify'
            }
        }
        stage ('tests') {
            steps {
                sh 'mvn tests'
            }
        }
    }
}