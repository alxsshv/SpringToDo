pipeline {
    agent {
        label 'docker'
    }
    tools {
        maven '3.9.12'
    }
    stages {
        stage('checkout') {
            when {
                expression { return env.CHANGE_TARGET == 'dev' && env.CHANGE_ID != null }
            }
            steps {
                checkout scm
            }
        }
        stage ('build') {
            when {
                expression { return env.CHANGE_TARGET == 'dev' && env.CHANGE_ID != null }
            }
            steps {
                    sh 'mvn -Dmaven.test.skip clean verify '
            }
        }
        stage ('tests') {
            when {
                expression { return env.CHANGE_TARGET == 'dev' && env.CHANGE_ID != null }
            }
            steps {
                sh 'mvn test'
            }
        }
    }
}