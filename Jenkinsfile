pipeline {
    // Sử dụng Maven Docker Agent với Java 21
    agent {
        docker {
            image 'maven:3.9.6-eclipse-temurin-21'
            args '-v /var/jenkins_home/.m2:/root/.m2 --network vegana-net'
        }
    }

    environment {
        // MySQL connection settings (qua network nội bộ)
        MYSQL_HOST = "mysql"
        MYSQL_PORT = "3306"
        MYSQL_USER = "root"
        MYSQL_PASS = "123456"
        MYSQL_DATABASE = "vegana_store"
        
        // Application settings
        APP_PORT = "8080"
        BASE_URL = "http://localhost:8080"
        
        // Selenium Grid settings (qua network nội bộ)
        SELENIUM_HUB_URL = "http://selenium-hub:4444"
        
        // Test environment
        GITHUB_ACTIONS = "true"
    }

    options {
        timeout(time: 40, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        ansiColor('xterm')
    }

    stages {
        /* ============================================
           STAGE 1: CHECKOUT CODE
           ============================================ */
        stage('🔍 Checkout Code') {
            steps {
                echo '📥 Checking out code from repository...'
                checkout scm
                sh 'git rev-parse HEAD > .git/commit-id'
                sh 'cat .git/commit-id'
            }
        }

        /* ============================================
           STAGE 2: INSTALL MYSQL CLIENT
           Cài mysql-client trong Maven Docker Agent
           ============================================ */
        stage('📦 Install MySQL Client') {
            steps {
                echo '🔧 Installing MySQL client in Maven agent...'
                sh '''
                    apt-get update -qq
                    apt-get install -y -qq default-mysql-client
                    mysql --version
                    echo "✅ MySQL client installed!"
                '''
            }
        }

        /* ============================================
           STAGE 3: WAIT FOR MYSQL
           Chờ MySQL sẵn sàng qua network nội bộ
           ============================================ */
        stage('🐬 Wait for MySQL') {
            steps {
                echo '⏳ Waiting for MySQL to be ready...'
                sh '''
                    echo "Checking MySQL connection via network..."
                    export MYSQL_PWD=${MYSQL_PASS}
                    for i in $(seq 1 30); do
                        if mysqladmin ping -h ${MYSQL_HOST} -u${MYSQL_USER} --silent 2>/dev/null; then
                            echo "✅ MySQL is ready!"
                            unset MYSQL_PWD
                            exit 0
                        fi
                        echo "Waiting for MySQL ($i/30)..."
                        sleep 2
                    done
                    echo "❌ MySQL did not start!"
                    unset MYSQL_PWD
                    exit 1
                '''
            }
        }

        /* ============================================
           STAGE 4: SETUP DATABASE
           Tạo database và import schema qua network
           ============================================ */
        stage('🗄️ Setup Database') {
            steps {
                echo '📊 Setting up database schema...'
                sh '''
                    export MYSQL_PWD=${MYSQL_PASS}
                    
                    echo "Creating database if not exists..."
                    mysql -h ${MYSQL_HOST} -u${MYSQL_USER} \
                        -e "CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE};" || true

                    if [ -f vegana.sql ]; then
                        echo "Importing DB schema from vegana.sql..."
                        mysql -h ${MYSQL_HOST} -u${MYSQL_USER} ${MYSQL_DATABASE} < vegana.sql
                        echo "✅ Schema imported successfully!"
                    else
                        echo "⚠️ vegana.sql not found → skipping import"
                    fi
                    
                    unset MYSQL_PWD
                '''
            }
        }

        /* ============================================
           STAGE 5: BUILD APPLICATION
           Build Spring Boot JAR (skip tests)
           ============================================ */
        stage('🔨 Build Application') {
            steps {
                echo '🏗️ Building Spring Boot application...'
                sh '''
                    mvn clean package -DskipTests
                    echo "✅ Build completed!"
                '''
            }
        }

        /* ============================================
           STAGE 6: START SPRING BOOT APPLICATION
           Start app in background và lưu PID
           ============================================ */
        stage('🚀 Start Spring Boot Application') {
            steps {
                echo '🌐 Starting Spring Boot application...'
                script {
                    sh '''
                        echo "Starting Spring Boot in background..."
                        nohup mvn spring-boot:run \
                            -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=false -Dhibernate.hbm2ddl.auto=none" \
                            > app.log 2>&1 &
                        
                        echo $! > app.pid
                        echo "Application PID: $(cat app.pid)"
                    '''

                    sh '''
                        echo "⏳ Waiting for application to start..."
                        for i in $(seq 1 30); do
                            if curl -f http://localhost:${APP_PORT}/ >/dev/null 2>&1; then
                                echo "✅ Application started successfully!"
                                exit 0
                            fi
                            echo "Attempt $i/30: Application not ready yet, waiting..."
                            sleep 3
                        done
                        
                        echo "❌ Application failed to start. Last 50 lines of app.log:"
                        tail -50 app.log || true
                        exit 1
                    '''
                }
            }
        }

        /* ============================================
           STAGE 7: CHECK SELENIUM GRID
           Kiểm tra Selenium Hub qua network nội bộ
           ============================================ */
        stage('🌐 Check Selenium Grid') {
            steps {
                echo '🔍 Checking Selenium Grid availability...'
                sh '''
                    echo "Checking Selenium Hub via network..."
                    for i in $(seq 1 10); do
                        if curl -s http://selenium-hub:4444/wd/hub/status >/dev/null 2>&1; then
                            echo "✅ Selenium Grid is ready!"
                            curl -s http://selenium-hub:4444/wd/hub/status | head -20
                            exit 0
                        fi
                        echo "Waiting for Selenium Grid ($i/10)..."
                        sleep 2
                    done
                    echo "⚠️ Selenium Grid not available, tests will use local Chrome"
                '''
            }
        }

        /* ============================================
           STAGE 8: RUN AUTOMATION TESTS
           Chạy TestNG tests với Selenium
           ============================================ */
        stage('🧪 Run Automation Tests') {
            steps {
                echo '🎯 Running Selenium automation tests...'
                sh '''
                    mkdir -p test-output/reports test-output/screenshots test-output/logs
                    
                    export GITHUB_ACTIONS=true
                    export SELENIUM_HUB_URL=${SELENIUM_HUB_URL}
                    
                    echo "Running TestNG suite from testng.xml..."
                    mvn test -DsuiteXmlFile=src/test/resources/testng.xml || true
                    
                    echo "✅ Tests completed!"
                '''
            }
        }

        /* ============================================
           STAGE 9: ARCHIVE TEST RESULTS
           Archive tất cả reports và logs
           ============================================ */
        stage('📊 Archive Test Results') {
            steps {
                echo '📦 Archiving test results and reports...'
                script {
                    // Archive Extent Reports
                    archiveArtifacts artifacts: 'test-output/reports/**/*', allowEmptyArchive: true
                    
                    // Archive Screenshots
                    archiveArtifacts artifacts: 'test-output/screenshots/**/*', allowEmptyArchive: true
                    
                    // Archive Surefire Reports
                    archiveArtifacts artifacts: 'target/surefire-reports/**/*', allowEmptyArchive: true
                    
                    // Archive Application Logs
                    archiveArtifacts artifacts: 'app.log', allowEmptyArchive: true
                    
                    // Publish TestNG Results
                    publishTestNGResults(
                        testResultsPattern: 'target/surefire-reports/testng-results.xml',
                        reportFilesPattern: 'target/surefire-reports/**/*',
                        escapeTestDescription: false,
                        escapeExceptionMsg: false
                    )
                }
            }
        }
    }

    /* ============================================
       POST ACTIONS: CLEANUP
       Dừng ứng dụng bằng PID và cleanup
       ============================================ */
    post {
        always {
            script {
                sh '''
                    echo "🧹 Cleaning up..."
                    if [ -f app.pid ]; then
                        PID=$(cat app.pid)
                        echo "Stopping application (PID: $PID)..."
                        kill $PID 2>/dev/null || true
                        sleep 2
                        kill -9 $PID 2>/dev/null || true
                        rm -f app.pid
                    fi
                    pkill -f "spring-boot:run" || true
                    echo "✅ Cleanup completed!"
                '''
            }
        }
        success {
            echo "🎉 SUCCESS: CI/CD Pipeline Completed!"
        }
        failure {
            echo "❌ FAILURE: Check console log and artifacts"
        }
        unstable {
            echo "⚠️ UNSTABLE: Pipeline completed with warnings"
        }
    }
}
