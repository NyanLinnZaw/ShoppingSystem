pipeline {
    agent any

    stages {

        stage('Git Checkout') {
            steps {
                echo 'Checkout source code'

                git branch: 'main',
                    url: 'https://github.com/NyanLinnZaw/ShoppingSystem.git'
            }
        }

        stage('Build JAR') {
            steps {
                echo 'Build Spring Boot JAR'

                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Docker Compose Deploy') {
            steps {
                echo 'Deploy using Docker Compose'

                sh 'docker compose down || true'
                sh 'docker compose up -d --build'
            }
        }

        stage('Verify Deployment') {
            steps {
                echo 'Check container status'

                sh 'docker ps'

                echo 'Wait for application startup'
                sh 'sleep 20'

                echo 'Verify application'

                // Change port if needed
                sh 'curl -f http://localhost:8080 || exit 1'
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