pipeline {
    agent {
        label 'docker'
    }
    tools {
        maven '3.9.12'
    }
    environment {
        DOCKERHUB_CREDENTIALS = credentials('docker-hub')
        DOCKERHUB_REPOSITORY = 'alxsshv/SpringToDo'
        IMAGE_VERSION_TAG = 'latest'
    }
    stages {
        stage ('Build jar') {
            when { changeRequest target: 'dev' }
            steps {
                sh 'mvn -Dmaven.test.skip clean verify '
            }
        }
        stage ('Run tests') {
            when { changeRequest target: 'dev' }
            steps {
                sh 'mvn test'
            }
        }
        stage ('Build image') {
            when { branch 'main' }
            steps {
                timeout(time: 15, unit: 'MINUTES') {
                    sh 'docker build -t $DOCKERHUB_REPOSITORY:$IMAGE_VERSION_TAG .'
                }
            }
        }
        stage ('Push to DockerHub') {
            when { branch 'main' }
            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                sh 'docker push $DOCKERHUB_REPOSITORY:$IMAGE_VERSION_TAG'
            }
        }
    }
    post {
        always {
            sh 'docker logout'
        }
    }
}