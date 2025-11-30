pipeline {
    agent any
    
    tools {
        maven 'Maven-3.8'
        jdk 'JDK-17'
    }
    
    environment {
        BASE_URL = 'http://localhost:8080'
        BROWSER = 'chrome'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code...'
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                echo 'Building project...'
                sh 'mvn clean compile'
            }
        }
        
        stage('Start Application') {
            steps {
                echo 'Starting Spring Boot application...'
                sh '''
                    mvn spring-boot:run &
                    sleep 30
                    curl -f http://localhost:8080 || exit 1
                '''
            }
        }
        
        stage('Run Tests') {
            steps {
                echo 'Running automation tests...'
                sh 'mvn test -DsuiteXmlFile=src/test/resources/testng.xml'
            }
            post {
                always {
                    // Archive test results
                    archiveArtifacts artifacts: 'test-output/reports/*.html', fingerprint: true
                    archiveArtifacts artifacts: 'test-output/screenshots/*.png', allowEmptyArchive: true
                    archiveArtifacts artifacts: 'test-output/logs/*.log', allowEmptyArchive: true
                    
                    // Publish test results
                    publishTestResults testResultsPattern: 'test-output/testng-results.xml'
                }
            }
        }
        
        stage('Generate Reports') {
            steps {
                echo 'Generating test reports...'
                script {
                    def reports = sh(
                        script: 'find test-output/reports -name "*.html" -type f',
                        returnStdout: true
                    ).trim()
                    
                    if (reports) {
                        echo "Test reports generated: ${reports}"
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up...'
            sh 'pkill -f spring-boot:run || true'
        }
        success {
            echo '✅ Pipeline succeeded!'
            // Có thể thêm email notification ở đây
        }
        failure {
            echo '❌ Pipeline failed!'
            // Có thể thêm email notification ở đây
        }
        unstable {
            echo '⚠️ Pipeline unstable!'
        }
    }
}

