pipeline {
    agent any

    triggers {
        pollSCM('H/5 * * * *')
    }

    environment {
        APP_URL = 'http://localhost:8082/api/products'
    }

    stages {

        stage('Git Checkout') {
            steps {
                echo 'Checkout source code'

                git branch: 'main',
                    credentialsId: 'github-credentials',
                    url: 'https://github.com/NyanLinnZaw/ShoppingSystem.git'
            }
        }

        stage('Verify Tools') {
            steps {
                echo 'Checking required tools...'

                bat 'git --version'
                bat 'java -version'
                bat 'docker --version'
                bat 'docker compose version'
            }
        }

        stage('Build Spring Boot JAR') {
            steps {
                echo 'Building Spring Boot application...'

                bat 'mvnw.cmd clean package -DskipTests -B'
            }
        }

        stage('Docker Compose Deploy') {
            steps {
                echo 'Stopping old containers...'

                bat 'docker compose down'

                echo 'Building and starting containers...'

                bat 'docker compose up -d --build'
            }
        }

        stage('Verify Deployment') {
            steps {
                echo 'Waiting for application startup...'

                bat 'ping 127.0.0.1 -n 25 > nul'

                echo 'Checking running containers...'

                bat 'docker ps'

                echo 'Testing backend API...'

                bat '''
                powershell -Command ^
                "try { ^
                    $response = Invoke-WebRequest -Uri %APP_URL% -UseBasicParsing; ^
                    Write-Host 'Application is running successfully'; ^
                    exit 0 ^
                } catch { ^
                    Write-Host 'Application check failed'; ^
                    exit 1 ^
                }"
                '''
            }
        }
    }

    post {

        success {
            echo '========================================'
            echo 'Deployment completed successfully'
            echo 'Backend URL: http://localhost:8082'
            echo '========================================'
        }

        failure {
            echo '========================================'
            echo 'Deployment failed'
            echo 'Checking Docker container logs...'
            echo '========================================'

            bat 'docker ps -a'

            bat 'docker logs shopping-backend'

            bat 'docker logs shopping-mysql'
        }
    }
}