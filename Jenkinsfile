// ShoppingSystem — Jenkins CI/CD (Windows + Linux)
//
// Jenkins one-time setup:
// 1. Manage Jenkins → Tools → Git → Add:
//      Name: Default
//      Path: C:\Users\nyanlinnzaw\AppData\Local\Programs\Git\cmd\git.exe
// 2. Credentials → github-credentials (Username + GitHub PAT)
// 3. Optional: add Git/Docker to System PATH and restart Jenkins service
// 4. Poll SCM enabled below (no GitHub webhook)
//
// Windows: Jenkins *service* does not inherit your user PATH — see Prepare Tools stage.

pipeline {
    agent any

    environment {
        WIN_GIT    = 'C:\\Users\\nyanlinnzaw\\AppData\\Local\\Programs\\Git\\cmd'
        WIN_DOCKER = 'C:\\Program Files\\Docker\\Docker\\resources\\bin'
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    stages {

        stage('Prepare Tools') {
            steps {
                script {
                    if (!isUnix()) {
                        env.PATH = "${WIN_GIT};${WIN_DOCKER};${env.PATH}"
                        bat """
                            @echo off
                            echo === Tool check ===
                            echo PATH=%PATH%
                            git --version
                            docker --version
                        """
                    } else {
                        sh 'git --version && docker --version'
                    }
                }
            }
        }

        stage('Git Checkout') {
            steps {
                echo 'Checkout source code'
                git branch: 'main',
                    credentialsId: 'github-credentials',
                    url: 'https://github.com/NyanLinnZaw/ShoppingSystem.git'
            }
        }

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
                        bat 'docker compose down 2>nul'
                        bat 'docker compose up -d --build'
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
            echo 'Deployment successful — http://localhost:8082'
        }
        failure {
            echo 'Deployment failed — check Prepare Tools (git/docker) then Build/Docker stages.'
        }
    }
}
