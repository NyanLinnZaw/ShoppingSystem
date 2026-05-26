// ShoppingSystem — CI/CD pipeline for GitHub + Docker
//
// Jenkins setup (one-time):
// 1. Install plugins: Pipeline, Git, Docker Pipeline, Credentials Binding, WS Cleanup
// 2. Jenkins → Manage Jenkins → Credentials → add:
//    - "github-credentials" (Username + PAT) for private repos / GHCR push
//    - "docker-registry-credentials" (Username + Token) — GitHub PAT with write:packages for GHCR
// 3. Create Pipeline job → Pipeline script from SCM → Git → your GitHub repo URL
// 4. Builds are triggered by Poll SCM (see triggers below) — no GitHub webhook required
// 5. Job → Configure → Environment: set GITHUB_OWNER to your GitHub username/org
// 6. Windows agent: install Git, Docker Desktop, JDK 17; ensure docker and mvnw.cmd are on PATH
//
// Image tags: shopping-system:<BUILD_NUMBER> and shopping-system:latest

pipeline {
    agent any

    environment {
        APP_NAME            = 'shopping-system'
        JAR_NAME            = 'ShoppingSystem-0.0.1-SNAPSHOT.jar'
        DOCKER_REGISTRY     = 'ghcr.io'
        DOCKER_REPO         = "${DOCKER_REGISTRY}/NyanLinnZaw/${APP_NAME}"
        DOCKER_IMAGE_TAG    = "${BUILD_NUMBER}"
        DOCKER_CREDENTIALS  = 'docker-registry-credentials'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                script {
                    if (isUnix()) {
                        sh 'chmod +x mvnw'
                    }
                }
            }
        }

        stage('Build') {
            steps {
                echo 'Compiling and packaging (tests run in next stage)...'
                script {
                    if (isUnix()) {
                        sh './mvnw clean package -DskipTests -B'
                    } else {
                        bat 'mvnw.cmd clean package -DskipTests -B'
                    }
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: "target/${JAR_NAME}", fingerprint: true
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Running tests with a temporary MySQL container...'
                script {
                    if (isUnix()) {
                        sh '''
                            docker rm -f jenkins-mysql-test 2>/dev/null || true
                            docker run -d --name jenkins-mysql-test \
                                -e MYSQL_ROOT_PASSWORD=testpass \
                                -e MYSQL_DATABASE=shopping_db \
                                -p 3307:3306 \
                                mysql:8.4
                            echo "Waiting for MySQL..."
                            for i in $(seq 1 30); do
                                if docker exec jenkins-mysql-test mysqladmin ping -h localhost -uroot -ptestpass --silent; then
                                    echo "MySQL is up"
                                    break
                                fi
                                sleep 2
                            done
                            ./mvnw test -B \
                                -Dspring.datasource.url=jdbc:mysql://localhost:3307/shopping_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC \
                                -Dspring.datasource.username=root \
                                -Dspring.datasource.password=testpass
                        '''
                    } else {
                        bat '''
                            docker rm -f jenkins-mysql-test 2>nul
                            docker run -d --name jenkins-mysql-test -e MYSQL_ROOT_PASSWORD=testpass -e MYSQL_DATABASE=shopping_db -p 3307:3306 mysql:8.4
                            echo Waiting for MySQL...
                            ping -n 31 127.0.0.1 >nul
                            mvnw.cmd test -B "-Dspring.datasource.url=jdbc:mysql://localhost:3307/shopping_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" -Dspring.datasource.username=root -Dspring.datasource.password=testpass
                        '''
                    }
                }
            }
            post {
                always {
                    script {
                        if (isUnix()) {
                            sh 'docker rm -f jenkins-mysql-test 2>/dev/null || true'
                        } else {
                            bat 'docker rm -f jenkins-mysql-test 2>nul'
                        }
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo "Building image ${DOCKER_REPO}:${DOCKER_IMAGE_TAG}"
                script {
                    if (isUnix()) {
                        sh """
                            docker build -t ${DOCKER_REPO}:${DOCKER_IMAGE_TAG} .
                            docker tag ${DOCKER_REPO}:${DOCKER_IMAGE_TAG} ${DOCKER_REPO}:latest
                        """
                    } else {
                        bat """
                            docker build -t ${DOCKER_REPO}:${DOCKER_IMAGE_TAG} .
                            docker tag ${DOCKER_REPO}:${DOCKER_IMAGE_TAG} ${DOCKER_REPO}:latest
                        """
                    }
                }
            }
        }

        stage('Docker Push') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                }
            }
            steps {
                echo "Pushing ${DOCKER_REPO}:${DOCKER_IMAGE_TAG} to registry..."
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKER_CREDENTIALS}",
                    usernameVariable: 'REGISTRY_USER',
                    passwordVariable: 'REGISTRY_PASS'
                )]) {
                    script {
                        if (isUnix()) {
                            sh """
                                echo "\${REGISTRY_PASS}" | docker login ${DOCKER_REGISTRY} -u "\${REGISTRY_USER}" --password-stdin
                                docker push ${DOCKER_REPO}:${DOCKER_IMAGE_TAG}
                                docker push ${DOCKER_REPO}:latest
                                docker logout ${DOCKER_REGISTRY}
                            """
                        } else {
                            bat """
                                @echo off
                                echo %REGISTRY_PASS%| docker login ${DOCKER_REGISTRY} -u %REGISTRY_USER% --password-stdin
                                docker push ${DOCKER_REPO}:${DOCKER_IMAGE_TAG}
                                docker push ${DOCKER_REPO}:latest
                                docker logout ${DOCKER_REGISTRY}
                            """
                        }
                    }
                }
            }
        }

        stage('Docker Run') {
            steps {
                script {
                    if (isUnix()) {
                        sh """
                            docker image inspect ${DOCKER_REPO}:${DOCKER_IMAGE_TAG}
                            echo Deploy: BUILD_NUMBER=${BUILD_NUMBER} docker compose pull && docker compose up -d
                        """
                    } else {
                        bat """
                            docker image inspect ${DOCKER_REPO}:${DOCKER_IMAGE_TAG}
                            echo Deploy: set BUILD_NUMBER=${BUILD_NUMBER} then docker compose pull ^&^& docker compose up -d
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs(deleteDirs: true, patterns: [[pattern: 'target', type: 'INCLUDE']])
        }
        success {
            echo "Pipeline succeeded — image: ${DOCKER_REPO}:${DOCKER_IMAGE_TAG}"
        }
        failure {
            echo 'Pipeline failed — check console output for Build / Test / Docker stages.'
        }
    }
}
