// Windows Jenkins agent: uses bat + mvnw.cmd (no sh/bash required)
// Linux agent: uses sh + ./mvnw automatically via isUnix()

pipeline {
    agent any

    triggers {
        pollSCM('H/5 * * * *')
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

//         stage('Git Test') {
//             steps {
//                 echo 'Testing Git access...'
//
//                 script {
//                     if (isUnix()) {
//                         sh 'git --version'
//                         sh 'git ls-remote https://github.com/NyanLinnZaw/ShoppingSystem.git'
//                     } else {
//                         bat 'git --version'
//                         bat 'git ls-remote https://github.com/NyanLinnZaw/ShoppingSystem.git'
//                     }
//                 }
//             }
//         }

        stage('Build JAR') {
            steps {
                echo 'Build Spring Boot JAR'
                script {
                    if (isUnix()) {
                        sh 'chmod +x mvnw'
                        sh './mvnw clean package -DskipTests -B'
                    } else {
                        bat 'mvnw.cmd clean package -DskipTests -B'
                    }
                }
            }
        }

        stage('Docker Compose Deploy') {
            steps {
                echo 'Deploy using Docker Compose'
                script {
                    if (isUnix()) {
                        sh 'docker compose down || true'
                        sh 'docker compose up -d --build'
                    } else {
                        bat 'wsl docker compose down || true'
                        bat 'wsl docker compose up -d --build'
                    }
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                echo 'Check container status'
                script {
                    if (isUnix()) {
                        sh 'docker ps'
                        sh 'sleep 20'
                        sh 'curl -f http://localhost:8082/api/products'
                    } else {
                        bat 'docker ps'
                        bat 'ping -n 21 127.0.0.1 >nul'
                        bat 'curl -f http://localhost:8082/api/products'
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment successful'
        }
        failure {
            echo 'Deployment failed'
        }
    }
}
