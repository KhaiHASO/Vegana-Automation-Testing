pipeline {
  agent any

  tools {
    jdk   'jdk17'
    maven 'maven'
  }

  environment {
    APP_PORT = '9090'                 // tránh đụng Jenkins 8080
    BASE_URL = "http://localhost:9090"
  }

  options {
    ansiColor('xterm')
    timeout(time: 40, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr: '10'))
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
        // Dùng step powershell để start + wait healthcheck, không bị lỗi $ErrorActionPreference
        powershell(label: 'Start Spring Boot & wait', returnStatus: false, script: '''
          $ErrorActionPreference = "Stop"
          Write-Host "Starting Spring Boot on port $env:APP_PORT ..."

          $mvn = "mvn"
          $log = "app.log"
          $args = @("spring-boot:run", "-Dspring-boot.run.arguments=--server.port=$env:APP_PORT")

          $p = Start-Process $mvn -ArgumentList $args -RedirectStandardOutput $log -RedirectStandardError $log -WindowStyle Hidden -PassThru
          $p.Id | Set-Content -Path app.pid -Encoding ascii
          Write-Host "Spring Boot PID: $($p.Id)"

          $deadline = (Get-Date).AddMinutes(2)
          while ((Get-Date) -lt $deadline) {
            try {
              Invoke-WebRequest -UseBasicParsing "http://localhost:$env:APP_PORT" -TimeoutSec 3 | Out-Null
              Write-Host "✅ App is UP"
              exit 0
            } catch {
              Start-Sleep -Seconds 2
            }
          }

          Write-Host "❌ App failed to start in time"
          if (Test-Path $log) { Get-Content -Tail 150 $log }
          exit 1
        ''')
      }
    }

    stage('Run UI Tests') {
      steps {
        // Nếu framework test đọc baseUrl từ system property:
        bat 'mvn test -DbaseUrl=%BASE_URL%'
      }
    }

    stage('Archive Reports') {
      steps {
        archiveArtifacts artifacts: 'test-output/**/*', allowEmptyArchive: true
        archiveArtifacts artifacts: 'target/surefire-reports/**/*', allowEmptyArchive: true
        archiveArtifacts artifacts: 'app.log', allowEmptyArchive: true
      }
    }
  }

  post {
    always {
      echo 'Stopping Spring Boot safely...'
      powershell(label: 'Stop Spring Boot', returnStatus: true, script: '''
        $ErrorActionPreference = "SilentlyContinue"
        if (Test-Path "app.pid") {
          $pid = Get-Content "app.pid" | Select-Object -First 1
          if ($pid) { Stop-Process -Id $pid -Force }
          Remove-Item -Force "app.pid"
        } else {
          # Fallback: cố gắng kill theo tên tiến trình Java chạy spring-boot:run (không đụng Jenkins)
          Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.Path -like "*maven*" -or $_.ProcessName -eq "java" } | Stop-Process -Force -ErrorAction SilentlyContinue
        }
        Write-Host "Cleanup complete."
      ''')
    }
  }
}
