pipeline {

    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Run tests') {
            steps {
                sh 'chmod +x run-tests.sh'
                sh './run-tests.sh'
            }
        }

        stage('Allure Report') {
            steps {
                allure results: [[path: 'allure-results']]
            }
        }
    }
}