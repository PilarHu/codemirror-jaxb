pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
		   withMaven(maven: 'maven3', mavenSettingsConfig: '00e92796-3fa4-4c0f-b4ee-fa441532f2f0', jdk: 'JDK14') {
	                 sh 'mvn -B -DskipTests=true clean install'
                    } 
            }
        }
        stage('Test') {
            steps {
		   withMaven(maven: 'maven3', mavenSettingsConfig: '00e92796-3fa4-4c0f-b4ee-fa441532f2f0', jdk: 'JDK14') {
                         sh 'mvn test verify'
	            }
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('Deliver') {
            steps {
		   withMaven(maven: 'maven3', mavenSettingsConfig: '00e92796-3fa4-4c0f-b4ee-fa441532f2f0', jdk: 'JDK14') {
	                 sh 'mvn deploy'
		}
            }
        }
    }
}