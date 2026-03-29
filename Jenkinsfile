pipeline {
    agent {
        label 'agent1'
    }
    tools {
        maven '3.9.12'
        docker '29.3.1'
    }
    stages {
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