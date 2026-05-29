pipeline {
    agent any

    triggers {
        pollSCM('* * * * *')
    }

    environment {
        APP_URL = 'http://localhost:8082/api/products'
    }

//     stages {
//         stage('Git Checkout') {
//             steps {
//                 echo 'Checkout source code'
//                 git branch: 'main',
//                     credentialsId: 'github-credentials',
//                     url: 'https://github.com/NyanLinnZaw/ShoppingSystem.git'
//             }
//         }

        stage('Verify Tools') {
            steps {
                bat 'git --version'
                bat 'java -version'
                bat 'docker --version'
                bat 'docker compose version'
            }
        }

        stage('Build Spring Boot JAR') {
            steps {
                bat 'mvnw.cmd clean package -DskipTests -B'
            }
        }

        stage('Docker Compose Deploy') {
            steps {
                bat 'docker compose down'
                bat 'docker compose up -d --build'
            }
        }

        stage('Verify Deployment') {
            steps {
                bat 'ping 127.0.0.1 -n 25 > nul'
                bat 'docker ps'
                bat '''
                    powershell -NoProfile -Command ^
                        "try { Invoke-WebRequest -Uri '%APP_URL%' -UseBasicParsing | Out-Null; exit 0 } catch { exit 1 }"
                '''
            }
        }
    }

    post {
        success {
            echo '========================================'
            echo 'Pipeline completed successfully.'
            echo 'Backend URL: http://localhost:8082'
            echo '========================================'
        }
        failure {
            echo '========================================'
            echo 'Pipeline failed.'
            echo '========================================'
            bat 'docker ps -a'
            bat 'docker compose logs backend'
            bat 'docker compose logs mysql'
        }
    }
}
