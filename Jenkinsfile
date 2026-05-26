// ShoppingSystem — CI/CD pipeline for GitHub + Docker
//
// Jenkins setup (one-time):
// 1. Install plugins: Pipeline, GitHub, Docker Pipeline, Credentials Binding, WS Cleanup
// 2. Jenkins → Manage Jenkins → Credentials → add:
//    - "github-credentials" (Username + PAT) for private repos / GHCR push
//    - "docker-registry-credentials" (Username + Token) — GitHub PAT with write:packages for GHCR
// 3. Create Pipeline job → Pipeline script from SCM → Git → your GitHub repo URL
// 4. Configure GitHub webhook: push events → Jenkins (GitHub plugin or Poll SCM)
// 5. Job → Configure → Environment or parameters: set GITHUB_OWNER to your GitHub username/org
//
// Image tags: shopping-system:<BUILD_NUMBER> and shopping-system:latest

pipeline {
    agent any

    environment {
        APP_NAME            = 'shopping-system'
        JAR_NAME            = 'ShoppingSystem-0.0.1-SNAPSHOT.jar'
        // GitHub Container Registry (change to 'docker.io' + Docker Hub repo if preferred)
        DOCKER_REGISTRY     = 'ghcr.io'
        // Set GITHUB_OWNER in the Jenkins job environment (your GitHub username or org)
        DOCKER_REPO         = "${DOCKER_REGISTRY}/${GITHUB_OWNER}/${APP_NAME}"
        DOCKER_IMAGE_TAG    = "${BUILD_NUMBER}"
        // Jenkins credential IDs — do not put secrets in this file
        DOCKER_CREDENTIALS  = 'docker-registry-credentials'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {

        stage('Checkout') {
            steps {
                // Clones the GitHub repo configured on the Jenkins job (multibranch or pipeline SCM)
                checkout scm
                sh 'chmod +x mvnw'
            }
        }

        stage('Build') {
            steps {
                echo 'Compiling and packaging (tests run in next stage)...'
                sh './mvnw clean package -DskipTests -B'
            }
            post {
                success {
                    // Archive the JAR for traceability and manual deploy
                    archiveArtifacts artifacts: "target/${JAR_NAME}", fingerprint: true
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Running unit/integration tests with a temporary MySQL container...'
                script {
                    // Ephemeral MySQL for @SpringBootTest (app expects MySQL, not H2)
                    sh '''
                        docker rm -f jenkins-mysql-test 2>/dev/null || true
                        docker run -d --name jenkins-mysql-test \
                            -e MYSQL_ROOT_PASSWORD=testpass \
                            -e MYSQL_DATABASE=shopping_db \
                            -p 3307:3306 \
                            mysql:8.4

                        echo "Waiting for MySQL to be ready..."
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
                }
            }
            post {
                always {
                    sh 'docker rm -f jenkins-mysql-test 2>/dev/null || true'
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo "Building image ${DOCKER_REPO}:${DOCKER_IMAGE_TAG}"
                script {
                    // Requires Docker on the Jenkins agent (Docker socket or Docker-in-Docker)
                    sh """
                        docker build -t ${DOCKER_REPO}:${DOCKER_IMAGE_TAG} .
                        docker tag ${DOCKER_REPO}:${DOCKER_IMAGE_TAG} ${DOCKER_REPO}:latest
                    """
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
                    sh """
                        echo "\${REGISTRY_PASS}" | docker login ${DOCKER_REGISTRY} -u "\${REGISTRY_USER}" --password-stdin
                        docker push ${DOCKER_REPO}:${DOCKER_IMAGE_TAG}
                        docker push ${DOCKER_REPO}:latest
                        docker logout ${DOCKER_REGISTRY}
                    """
                }
            }
        }

        stage('Docker Run') {
            steps {
                // Verify image exists and print deploy hint (full run needs MySQL via docker compose on target host)
                sh """
                    docker image inspect ${DOCKER_REPO}:${DOCKER_IMAGE_TAG}
                    echo "Deploy on server: BUILD_NUMBER=${BUILD_NUMBER} docker compose pull && docker compose up -d"
                """
            }
        }
    }

    post {
        always {
            // Clean workspace after every build (devops rule)
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
