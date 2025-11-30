pipeline {
    agent any

    tools {
        jdk "jdk17"
        maven "maven"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build App') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Start App') {
            steps {
                bat '''
                echo Starting Spring Boot...
                start "" /B mvn spring-boot:run > app.log 2>&1
                ping -n 10 127.0.0.1 >nul
                '''
            }
        }

        stage('Run UI Tests') {
            steps {
                bat 'mvn test'
            }
        }

        stage('Archive Reports') {
            steps {
                archiveArtifacts artifacts: 'test-output/**/*', allowEmptyArchive: true
                archiveArtifacts artifacts: 'target/surefire-reports/**/*', allowEmptyArchive: true
                archiveArtifacts artifact
            }
        }
    }
}
