pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
		   withMaven(
                         maven: 'maven3',
                         mavenLocalRepo: '.repository') {
	                 sh 'mvn -B -DskipTests=true clean install'
                    } 
            }
        }
        stage('Test') {
            steps {
		   withMaven(
                         maven: 'maven3',
                         mavenLocalRepo: '.repository') {
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
		   withMaven(
                         maven: 'maven3',
                         mavenLocalRepo: '.repository') {
	                 sh 'mvn deploy'
		}
            }
        }
    }
}