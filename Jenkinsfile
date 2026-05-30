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
                      "$url = '%APP_URL%'; ^
                       for ($i = 0; $i -lt 24; $i++) { ^
                         try { ^
                           $r = Invoke-WebRequest -Uri $url -UseBasicParsing; ^
                           if ($r.StatusCode -eq 200) { Write-Host \"API ready\"; exit 0 } ^
                         } catch { Write-Host \"Attempt $($i + 1)/24: not ready yet\" } ^
                         Start-Sleep -Seconds 5 ^
                       }; ^
                       Write-Host \"API not ready after 120s\"; exit 1"
                '''
            }
        }
    }

    post {
        success {
            echo '========================================'
            echo '  DEPLOYMENT SUCCESSFUL'
            echo '========================================'
            echo 'Backend URL: http://localhost:8082'
        }
        failure {
            echo '========================================'
            echo '  DEPLOYMENT FAILED'
            echo '========================================'
            bat 'docker ps -a'
            bat 'docker compose logs backend'
            bat 'docker compose logs mysql'
        }
    }
}
